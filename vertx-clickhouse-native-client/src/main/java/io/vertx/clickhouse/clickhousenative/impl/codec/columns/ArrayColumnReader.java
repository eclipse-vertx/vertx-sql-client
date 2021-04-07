package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ArrayColumnReader extends ClickhouseColumnReader {

  private final ClickhouseNativeDatabaseMetadata md;
  private final ClickhouseNativeColumnDescriptor elementTypeDescr;

  private List<List<Integer>> slicesSeries;
  private Integer curDimension;
  private ClickhouseColumnReader nestedColumnReader;
  private ClickhouseColumn nestedColumn;
  private Class<?> elementClass;
  private Integer nItems;
  private boolean resliced;
  private Object statePrefix;

  private Integer curLevelSliceSize;
  private List<Integer> curLevelSlice;

  public ArrayColumnReader(int nRows, ClickhouseNativeColumnDescriptor descr, ClickhouseNativeDatabaseMetadata md) {
    super(nRows, descr);
    this.md = md;
    this.elementTypeDescr = descr.getNestedDescr();
  }

  @Override
  protected Object readStatePrefix(ClickhouseStreamDataSource in) {
    ClickhouseColumnReader statePrefixColumn = ClickhouseColumns.columnForSpec(elementTypeDescr, md).reader(0);
    if (statePrefix == null) {
      statePrefix = statePrefixColumn.readStatePrefix(in);
    }
    return statePrefix;
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (nItems == null) {
      slicesSeries = new ArrayList<>();
      curDimension = 0;
      nItems = 0;
    }
    if (statePrefix == null) {
      return null;
    }
    if (curDimension < columnDescriptor.arrayDimensionsCount()) {
      readSlices(in);
    }
    if (nestedColumnReader == null) {
      nestedColumn = ClickhouseColumns.columnForSpec(elementTypeDescr, md);
      nestedColumnReader = nestedColumn.reader(nItems);
      elementClass = nestedColumn.nullValue().getClass();
    }
    if (elementTypeDescr.isNullable()) {
      nestedColumnReader.nullsMap = nestedColumnReader.readNullsMap(in);
    }
    if (nItems > 0) {
      assert nItems == nestedColumnReader.nRows;
      if (nestedColumnReader.getClass() == LowCardinalityColumnReader.class) {
        ((LowCardinalityColumnReader) nestedColumnReader).keysSerializationVersion = LowCardinalityColumnReader.SUPPORTED_SERIALIZATION_VERSION;
      }
      if (nestedColumnReader.isPartial()) {
        nestedColumnReader.itemsArray = nestedColumnReader.readItemsAsObjects(in, elementClass);
        if (nestedColumnReader.isPartial()) {
          return null;
        }
      }
      if (elementTypeDescr.jdbcType() == JDBCType.VARCHAR
       || nestedColumnReader.getClass() == Enum8ColumnReader.class
       || nestedColumnReader.getClass() == Enum16ColumnReader.class) {
        return nestedColumnReader.itemsArray;
      }
      resliced = true;
      return resliceIntoArray((Object[]) nestedColumnReader.itemsArray, elementClass);
    }
    Object[] emptyData = (Object[]) Array.newInstance(elementClass, 0);
    if (elementTypeDescr.jdbcType() == JDBCType.VARCHAR
      || nestedColumnReader.getClass() == Enum8ColumnReader.class
      || nestedColumnReader.getClass() == Enum16ColumnReader.class) {
      return emptyData;
    }
    resliced = true;
    return resliceIntoArray(emptyData, elementClass);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    Object[] objectsArray = (Object[]) this.itemsArray;
    Object[] reslicedRet;
    if (resliced) {
      reslicedRet = objectsArray;
    } else {
      desired = maybeUnwrapArrayElementType(desired);
      Triplet<Boolean, Object[], Class<?>> maybeRecoded = asDesiredType(objectsArray, desired);
      if (maybeRecoded.left()) {
        desired = maybeRecoded.right();
      } else {
        desired = elementClass;
      }
      //TODO smagellan: reslicing for every row with master-slice can be slow (for BLOBS and Enums if recoding requested), maybe
      // 1) store resliced master-array into Phantom/Weak reference or
      // 2) split master-splice into nRows splices (1 per row)
      reslicedRet = resliceIntoArray(maybeRecoded.middle(), desired);
    }
    return reslicedRet[rowIdx];
  }

  private Class<?> maybeUnwrapArrayElementType(Class<?> desired) {
    if (desired != null) {
      while (desired.isArray() && desired != byte[].class) {
        desired = desired.getComponentType();
      }
    }
    return desired;
  }

  private Triplet<Boolean, Object[], Class<?>> asDesiredType(Object[] src, Class<?> desired) {
    if (elementTypeDescr.jdbcType() == JDBCType.VARCHAR) {
      if (desired == String.class || desired == Object.class) {
        return new Triplet<>(true, stringifyByteArrays(src, md.getStringCharset()), desired);
      }
      return new Triplet<>(false, src, desired);
    } else if (nestedColumnReader instanceof EnumColumnReader) {
      Object[] recoded = ((EnumColumnReader)nestedColumnReader).recodeValues(src, desired);
      return new Triplet<>(true, recoded, desired);
    }
    return new Triplet<>(false, src, desired);
  }

  private Object[] stringifyByteArrays(Object[] src, Charset charset) {
    String[] ret = new String[src.length];
    for (int i = 0; i < src.length; ++i) {
      Object element = src[i];
      if (element != null) {
        int lastNonZeroIdx;
        byte[] bytes = (byte[]) element;
        if (md.isRemoveTrailingZerosInFixedStrings() && elementTypeDescr.getNestedType().startsWith("FixedString")) {
          lastNonZeroIdx = ColumnUtils.getLastNonZeroPos(bytes);
        } else {
          lastNonZeroIdx = bytes.length - 1;
        }
        ret[i] = new String(bytes, 0, lastNonZeroIdx + 1, charset);
      }
    }
    return ret;
  }

  private Object[] resliceIntoArray(Object[] data, Class<?> elementClass) {
    Object[] intermData = data;
    for (int i = slicesSeries.size() - 1; i >= 0; --i) {
      List<Integer> slices = slicesSeries.get(i);
      Iterator<Map.Entry<Integer, Integer>> paired = PairedIterator.of(slices);
      Object[] newDataList = (Object[]) java.lang.reflect.Array.newInstance(intermData.getClass(), slices.size() - 1);
      int tmpSliceIdx = 0;
      while (paired.hasNext()) {
        Map.Entry<Integer, Integer> slice = paired.next();
        int newSliceSz = slice.getValue() - slice.getKey();
        Object[] reslicedArray = (Object[]) java.lang.reflect.Array.newInstance(intermData.getClass().getComponentType(), newSliceSz);
        System.arraycopy(intermData, slice.getKey(), reslicedArray, 0, newSliceSz);
        newDataList[tmpSliceIdx] = reslicedArray;
        ++tmpSliceIdx;
      }
      intermData = newDataList;
    }
    return (Object[]) intermData[0];
  }

  private void readSlices(ClickhouseStreamDataSource in) {
    if (slicesSeries.isEmpty()) {
      slicesSeries.add(Arrays.asList(0, nRows));
      curLevelSliceSize = nRows;
    }
    if (nRows == 0) {
      curDimension = columnDescriptor.arrayDimensionsCount();
      return;
    }

    long lastSliceSize = 0;
    while (curDimension < columnDescriptor.arrayDimensionsCount()) {
      if (curLevelSlice == null) {
        curLevelSlice = new ArrayList<>(curLevelSliceSize + 1);
        curLevelSlice.add(0);
      }
      if (in.readableBytes() < curLevelSliceSize * Long.BYTES) {
        return;
      }
      for (int curLevelSliceIndex = 0; curLevelSliceIndex < curLevelSliceSize; ++curLevelSliceIndex) {
        lastSliceSize = in.readLongLE();
        if (lastSliceSize > Integer.MAX_VALUE) {
          throw new IllegalStateException("nested size is too big (" + lastSliceSize + "), max " + Integer.MAX_VALUE);
        }
        curLevelSlice.add((int) lastSliceSize);
      }
      slicesSeries.add(curLevelSlice);
      curLevelSlice = null;
      curLevelSliceSize = (int) lastSliceSize;
      curDimension += 1;
    }
    nItems = (int)lastSliceSize;
  }
}

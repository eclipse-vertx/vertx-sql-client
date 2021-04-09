package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.lang.reflect.Array;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ArrayColumnReader extends ClickhouseColumnReader {
  private final ClickhouseNativeDatabaseMetadata md;
  private final ClickhouseNativeColumnDescriptor elementTypeDescr;

  private List<List<Integer>> masterSlice;
  private List<List<List<Integer>>> perRowsSlice;

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
      masterSlice = new ArrayList<>();
      curDimension = 0;
      nItems = 0;
    }
    if (statePrefix == null) {
      return null;
    }
    boolean maybeRequiresExtraEncoding = elementTypeDescr.jdbcType() == JDBCType.VARCHAR
      || elementTypeDescr.getNestedType().startsWith("Enum");
    if (curDimension < columnDescriptor.arrayDimensionsCount()) {
      if (maybeRequiresExtraEncoding) {
        readAsPerRowSlices(in);
      } else {
        readAsMasterSlice(in);
      }
      if (curDimension < columnDescriptor.arrayDimensionsCount()) {
        return null;
      }
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
        ((LowCardinalityColumnReader) nestedColumnReader).keysSerializationVersion = (Long) statePrefix;
      }
      if (nestedColumnReader.isPartial()) {
        nestedColumnReader.itemsArray = nestedColumnReader.readItems(in);
        if (nestedColumnReader.isPartial()) {
          return null;
        }
      }
      if (maybeRequiresExtraEncoding) {
        return nestedColumnReader.itemsArray;
      }
      resliced = true;
      Object[] tmp = nestedColumnReader.asObjectsArrayWithGetElement(elementClass);
      return resliceIntoArray(tmp, masterSlice, elementClass);
    }

    Object[] emptyData = nestedColumn.emptyArray();
    if (maybeRequiresExtraEncoding) {
      return emptyData;
    }
    resliced = true;
    return resliceIntoArray(emptyData, masterSlice, elementClass);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    Object[] reslicedRet;
    if (resliced) {
      reslicedRet = (Object[]) this.itemsArray;
      return reslicedRet[rowIdx];
    } else {
      desired = maybeUnwrapArrayElementType(desired);
      Class forRecode = desired;
      Function<Integer, Object> dataElementAccessor = (idx) -> nestedColumnReader.getElement(idx, forRecode);
      reslicedRet = resliceIntoArray(dataElementAccessor, perRowsSlice.get(rowIdx), desired);
      return reslicedRet;
    }
  }

  private Class<?> maybeUnwrapArrayElementType(Class<?> desired) {
    if (desired != null) {
      while (desired.isArray() && desired != byte[].class) {
        desired = desired.getComponentType();
      }
    }
    return desired;
  }

  private Object[] resliceIntoArray(Object[] data, List<List<Integer>> sliceToUse, Class<?> elementClass) {
    Object[] intermData = data;
    for (int i = sliceToUse.size() - 1; i >= 0; --i) {
      List<Integer> slices = sliceToUse.get(i);
      intermData = resliceArray(intermData, slices, intermData.getClass());
    }
    return (Object[]) intermData[0];
  }


  private Object[] resliceIntoArray(Function<Integer, Object> dataAccessor, List<List<Integer>> sliceToUse, Class<?> elementClass) {
    int i = sliceToUse.size() - 1;
    List<Integer> slices = sliceToUse.get(i);
    Object[] intermData = resliceArray(dataAccessor, slices, java.lang.reflect.Array.newInstance(elementClass, 0).getClass());

    for (i = sliceToUse.size() - 2; i >= 0; --i) {
      slices = sliceToUse.get(i);
      intermData = resliceArray(intermData, slices, intermData.getClass());
    }
    return (Object[]) intermData[0];
  }

  private Object[] resliceArray(Function<Integer, Object> dataAccessor, List<Integer> slices, Class upperClass) {
    Iterator<Map.Entry<Integer, Integer>> paired = PairedIterator.of(slices);
    Object[] newDataList = (Object[]) java.lang.reflect.Array.newInstance(upperClass, slices.size() - 1);
    int tmpSliceIdx = 0;
    while (paired.hasNext()) {
      Map.Entry<Integer, Integer> slice = paired.next();
      int newSliceSz = slice.getValue() - slice.getKey();
      Object[] reslicedArray = (Object[]) java.lang.reflect.Array.newInstance(upperClass.getComponentType(), newSliceSz);
      copyWithAccessor(dataAccessor, slice.getKey(), reslicedArray, 0, newSliceSz);

      newDataList[tmpSliceIdx] = reslicedArray;
      ++tmpSliceIdx;
    }
    return newDataList;
  }

  private void copyWithAccessor(Function<Integer, Object> srcAccessor, int srcPos, Object[] dest, int destPos, int length) {
    for (int remaining = length; remaining > 0; --remaining) {
      dest[destPos] = srcAccessor.apply(srcPos);
      ++destPos;
      ++srcPos;
    }
  }

  private Object[] resliceArray(Object[] dataElements, List<Integer> slices, Class upperClass) {
    Iterator<Map.Entry<Integer, Integer>> paired = PairedIterator.of(slices);
    Object[] newDataList = (Object[]) java.lang.reflect.Array.newInstance(upperClass, slices.size() - 1);
    int tmpSliceIdx = 0;
    while (paired.hasNext()) {
      Map.Entry<Integer, Integer> slice = paired.next();
      int newSliceSz = slice.getValue() - slice.getKey();
      Object[] reslicedArray = (Object[]) java.lang.reflect.Array.newInstance(upperClass.getComponentType(), newSliceSz);
      System.arraycopy(dataElements, slice.getKey(), reslicedArray, 0, newSliceSz);
      newDataList[tmpSliceIdx] = reslicedArray;
      ++tmpSliceIdx;
    }
    return newDataList;
  }

  private void readAsPerRowSlices(ClickhouseStreamDataSource in) {
    if (nRows == 0) {
      masterSlice = Collections.emptyList();
      perRowsSlice = Collections.emptyList();
      curDimension = columnDescriptor.arrayDimensionsCount();
      return;
    }

    perRowsSlice = new ArrayList<>(nRows);
    for (int i = 0; i < nRows; ++i) {
      perRowsSlice.add(new ArrayList<>());
    }
    curLevelSliceSize = nRows;
    while (curDimension < columnDescriptor.arrayDimensionsCount()) {
      if (in.readableBytes() < curLevelSliceSize * Long.BYTES) {
        return;
      }
      long prevSliceElement = 0;
      for (int rowIdx = 0; rowIdx < nRows; ++rowIdx) {
        int rowSliceElementsToReadAtDimension;
        if (curDimension == 0) {
          rowSliceElementsToReadAtDimension = 1;
        } else {
          List<Integer> rowSliceAtPrevDimension = perRowsSlice.get(rowIdx).get(curDimension - 1);
          rowSliceElementsToReadAtDimension = rowSliceAtPrevDimension.get(rowSliceAtPrevDimension.size() - 1) - rowSliceAtPrevDimension.get(0);
        }
        List<Integer> rowSliceAtDimension = new ArrayList<>(rowSliceElementsToReadAtDimension + 1);
        //offsets at last dimension are absolute
        boolean lastDimension = curDimension == columnDescriptor.arrayDimensionsCount() - 1;
        int firstElementInSlice = (int) prevSliceElement;
        rowSliceAtDimension.add(firstElementInSlice - (int)(lastDimension ? 0L : firstElementInSlice));
        for (int i = 0; i < rowSliceElementsToReadAtDimension; ++i) {
          prevSliceElement = in.readLongLE();
          if (prevSliceElement > Integer.MAX_VALUE) {
            throw new IllegalStateException("nested size is too big (" + prevSliceElement + "), max " + Integer.MAX_VALUE);
          }
          rowSliceAtDimension.add((int)(prevSliceElement - (lastDimension ? 0L : firstElementInSlice)));
        }
        perRowsSlice.get(rowIdx).add(rowSliceAtDimension);
      }
      ++curDimension;
      curLevelSliceSize = (int)prevSliceElement;
    }
    nItems = curLevelSliceSize;
  }

  private void readAsMasterSlice(ClickhouseStreamDataSource in) {
    if (masterSlice.isEmpty()) {
      masterSlice.add(Arrays.asList(0, nRows));
      curLevelSliceSize = nRows;
    }
    if (nRows == 0) {
      perRowsSlice = Collections.emptyList();
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
      masterSlice.add(curLevelSlice);
      curLevelSlice = null;
      curLevelSliceSize = (int) lastSliceSize;
      curDimension += 1;
    }
    nItems = (int)lastSliceSize;
  }
}

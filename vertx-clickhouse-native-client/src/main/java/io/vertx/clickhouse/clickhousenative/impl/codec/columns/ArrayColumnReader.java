package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.nio.charset.Charset;
import java.sql.JDBCType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ArrayColumnReader extends ClickhouseColumnReader {
  private static final Object[] EMPTY_ARRAY = new Object[0];

  private final ClickhouseNativeDatabaseMetadata md;
  private final ClickhouseNativeColumnDescriptor elementaryDescr;

  private Deque<Triplet<ClickhouseNativeColumnDescriptor, List<Integer>, Integer>> graphLevelDeque;
  private List<List<Integer>> slicesSeries;
  private List<Integer> curSlice;
  private Integer curDepth;
  private ClickhouseNativeColumnDescriptor curNestedColumnDescr;
  private ClickhouseColumnReader curNestedColumn;
  private Integer nItems;
  private boolean resliced;
  private Object statePrefix;
  private boolean hasFirstSlice;
  private int sliceIdxAtCurrentDepth;
  private int prevSliceSizeAtCurrentDepth = 0;
  private Triplet<ClickhouseNativeColumnDescriptor, List<Integer>, Integer> slicesAtCurrentDepth;

  public ArrayColumnReader(int nRows, ClickhouseNativeColumnDescriptor descr, ClickhouseNativeDatabaseMetadata md) {
    super(nRows, descr.copyAsNestedArray());
    this.md = md;
    this.elementaryDescr = elementaryDescr(columnDescriptor);
  }

  private ClickhouseNativeColumnDescriptor elementaryDescr(ClickhouseNativeColumnDescriptor descr) {
    ClickhouseNativeColumnDescriptor tmp = descr;
    while (tmp.isArray()) {
      tmp = tmp.getNestedDescr();
    }
    return tmp;
  }

  @Override
  protected Object readStatePrefix(ClickhouseStreamDataSource in) {
    ClickhouseColumnReader statePrefixColumn = ClickhouseColumns.columnForSpec(elementaryDescr, md).reader(0);
    if (statePrefix == null) {
      statePrefix = statePrefixColumn.readStatePrefix(in);
    }
    return statePrefix;
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (graphLevelDeque == null) {
      graphLevelDeque = new ArrayDeque<>();
      graphLevelDeque.add(new Triplet<>(columnDescriptor, Collections.singletonList(nRows), 0));
      slicesSeries = new ArrayList<>();
      curSlice = new ArrayList<>();
      curDepth = 0;
      curNestedColumnDescr = columnDescriptor.getNestedDescr();
      nItems = 0;
    }
    if (statePrefix == null) {
      return null;
    }
    readSlices(in);
    if (curNestedColumn == null) {
      curNestedColumn = ClickhouseColumns.columnForSpec(curNestedColumnDescr, md).reader(nItems);
    }
    if (curNestedColumnDescr.isNullable()) {
      curNestedColumn.nullsMap = curNestedColumn.readNullsMap(in);
    }
    if (nItems > 0) {
      assert nItems == curNestedColumn.nRows;
      if (curNestedColumn.getClass() == LowCardinalityColumnReader.class) {
        ((LowCardinalityColumnReader)curNestedColumn).keysSerializationVersion = LowCardinalityColumnReader.SUPPORTED_SERIALIZATION_VERSION;
      }
      if (curNestedColumn.isPartial()) {
        curNestedColumn.itemsArray = curNestedColumn.readItemsAsObjects(in, null);
        if (curNestedColumn.isPartial()) {
          return null;
        }
      }
      if (elementaryDescr.jdbcType() == JDBCType.VARCHAR
       || curNestedColumn.getClass() == Enum8ColumnReader.class
       || curNestedColumn.getClass() == Enum16ColumnReader.class) {
        return curNestedColumn.itemsArray;
      }
      resliced = true;
      return resliceIntoArray((Object[]) curNestedColumn.itemsArray);
    }
    resliced = true;
    return resliceIntoArray(EMPTY_ARRAY);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    Object[] objectsArray = (Object[]) this.itemsArray;
    Object[] reslicedRet = resliced ? objectsArray : resliceIntoArray(asDesiredType(objectsArray, desired));
    return reslicedRet[rowIdx];
  }

  private Object[] asDesiredType(Object[] src, Class<?> desired) {
    if (desired == String.class && elementaryDescr.jdbcType() == JDBCType.VARCHAR) {
      return stringifyByteArrays(src, md.getStringCharset());
    }
    return src;
  }

  private Object[] stringifyByteArrays(Object[] src, Charset charset) {
    Object[] ret = new Object[src.length];
    for (int i = 0; i < src.length; ++i) {
      Object element = src[i];
      if (element != null) {
        ret[i] = new String((byte[]) element, charset);
      }
    }
    return ret;
  }

  private Object[] resliceIntoArray(Object[] data) {
    Object[] intermData = data;
    for (int i = slicesSeries.size() - 1; i >= 0; --i) {
      List<Integer> slices = slicesSeries.get(i);
      Iterator<Map.Entry<Integer, Integer>> paired = PairedIterator.of(slices);
      Object[] newDataList = new Object[slices.size() - 1];
      int tmpSliceIdx = 0;
      while (paired.hasNext()) {
        Map.Entry<Integer, Integer> slice = paired.next();
        int newSliceSz = slice.getValue() - slice.getKey();
        Object[] resliced = new Object[newSliceSz];
        System.arraycopy(intermData, slice.getKey(), resliced, 0, newSliceSz);
        newDataList[tmpSliceIdx] = resliced;
        ++tmpSliceIdx;
      }
      intermData = newDataList;
    }
    return intermData;
  }

  private void readSlices(ClickhouseStreamDataSource in) {
    if (!hasFirstSlice) {
      slicesAtCurrentDepth = graphLevelDeque.remove();
      curNestedColumnDescr = slicesAtCurrentDepth.left().getNestedDescr();
      hasFirstSlice = readSlice(in, slicesAtCurrentDepth);
    }

    while (!graphLevelDeque.isEmpty() || sliceIdxAtCurrentDepth != 0) {
      if (sliceIdxAtCurrentDepth == 0) {
        slicesAtCurrentDepth = graphLevelDeque.remove();
        curNestedColumnDescr = slicesAtCurrentDepth.left().getNestedDescr();

        curDepth = slicesAtCurrentDepth.right();
        slicesSeries.add(curSlice);

        //The last element in slice is index(number) of the last
        //element in current level. On the last iteration this
        //represents number of elements in fully flattened array.
        nItems = curSlice.get(curSlice.size() - 1);
        curSlice = new ArrayList<>();
      }
      if (curNestedColumnDescr.isArray()) {
        readSlice(in, slicesAtCurrentDepth);
      }
    }
  }

  private boolean readSlice(ClickhouseStreamDataSource in, Triplet<ClickhouseNativeColumnDescriptor, List<Integer>, Integer> sliceState) {
    if (sliceIdxAtCurrentDepth == 0) {
      curSlice.add(0);
    }
    for (; sliceIdxAtCurrentDepth < sliceState.middle().size(); ++sliceIdxAtCurrentDepth) {
      int size = sliceState.middle().get(sliceIdxAtCurrentDepth);
      int nestedSizeCount = size - prevSliceSizeAtCurrentDepth;
      if (in.readableBytes() < nestedSizeCount * 8) {
        return false;
      }
      ArrayList<Integer> nestedSizes = new ArrayList<>(nestedSizeCount);
      for (int i = 0; i < nestedSizeCount; ++i) {
        long sz = in.readLongLE();
        if (sz > Integer.MAX_VALUE) {
          throw new IllegalStateException("nested size is too big (" + sz + ") max " + Integer.MAX_VALUE);
        }
        nestedSizes.add((int) sz);
      }
      curSlice.addAll(nestedSizes);
      prevSliceSizeAtCurrentDepth = size;
      graphLevelDeque.add(new Triplet<>(curNestedColumnDescr, nestedSizes, curDepth + 1));
    }
    sliceIdxAtCurrentDepth = 0;
    prevSliceSizeAtCurrentDepth = 0;
    return true;
  }
}

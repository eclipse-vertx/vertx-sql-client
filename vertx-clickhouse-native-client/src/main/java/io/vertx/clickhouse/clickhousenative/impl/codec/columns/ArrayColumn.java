package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ArrayColumn extends ClickhouseColumn {
  private static final Object[] EMPTY_ARRAY = new Object[0];

  private Deque<Triplet<ClickhouseNativeColumnDescriptor, List<Integer>, Integer>> graphLevelDeque;
  private List<List<Integer>> slicesSeries;
  private List<Integer> slices;
  private Integer curDepth;
  private ClickhouseNativeColumnDescriptor curNestedColumnDescr;
  private ClickhouseColumn curNestedColumn;
  private Integer nItems;

  public ArrayColumn(int nRows, ClickhouseNativeColumnDescriptor descr) {
    super(nRows, descr.copyAsNestedArray());
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (graphLevelDeque == null) {
      graphLevelDeque = new ArrayDeque<>();
      graphLevelDeque.add(new Triplet<>(columnDescriptor, Collections.singletonList(nRows), 0));
      slicesSeries = new ArrayList<>();
      slices = new ArrayList<>();
      curDepth = 0;
      curNestedColumnDescr = columnDescriptor.getNestedDescr();
      nItems = 0;
    }
    readSlices(in);
    if (nItems > 0) {
      if (curNestedColumn == null) {
        curNestedColumn = ClickhouseColumns.columnForSpec(curNestedColumnDescr, nItems);
      } else {
        assert nItems == curNestedColumn.nRows;
      }
      curNestedColumn.itemsArray = curNestedColumn.readItemsAsObjects(in);
      return resliceIntoArray((Object[]) curNestedColumn.itemsArray);
    }
    return resliceIntoArray(EMPTY_ARRAY);
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
    //TODO smagellan: simplify the loop
    //TODO smagellan: handle fragmented reads
    while (!graphLevelDeque.isEmpty()) {
      Triplet<ClickhouseNativeColumnDescriptor, List<Integer>, Integer> sliceState = graphLevelDeque.remove();
      curNestedColumnDescr = sliceState.left().getNestedDescr();
      Integer newDepth = sliceState.right();
      if (curDepth != newDepth.intValue()) {
        curDepth = newDepth;
        slicesSeries.add(slices);

        //The last element in slice is index(number) of the last
        //element in current level. On the last iteration this
        //represents number of elements in fully flattened array.
        nItems = slices.get(slices.size() - 1);
        if (curNestedColumnDescr.isNullable()) {
          if (curNestedColumn == null) {
            curNestedColumn = ClickhouseColumns.columnForSpec(curNestedColumnDescr, nItems);
          }
          curNestedColumn.nullsMap = curNestedColumn.readNullsMap(in);
        }
        slices = new ArrayList<>();
      }
      if (curNestedColumnDescr.isArray()) {
        slices.add(0);
        int prev = 0;
        for (int size : sliceState.middle()) {
          int nestedSizeCount = size - prev;
          ArrayList<Integer> nestedSizes = new ArrayList<>(nestedSizeCount);
          for (int i = 0; i < nestedSizeCount; ++i) {
            long sz = in.readLongLE();
            if (sz > Integer.MAX_VALUE) {
              throw new IllegalStateException("nested size is too big (" + sz + ") max " + Integer.MAX_VALUE);
            }
            nestedSizes.add((int) sz);
          }
          slices.addAll(nestedSizes);
          prev = size;
          graphLevelDeque.add(new Triplet<>(curNestedColumnDescr, nestedSizes, curDepth + 1));
        }
      }
    }
  }
}

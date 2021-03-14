package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.util.ArrayList;
import java.util.List;

public class FixedStringColumn extends ClickhouseColumn {
  private List<Object> elements;

  protected FixedStringColumn(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
    this.elements = new ArrayList<>(nRows);
  }

  @Override
  protected Object[] readItems(ClickhouseStreamDataSource in) {
    int elementSize = columnDescriptor.getElementSize();
    while (elements.size() < nRows) {
      if (in.readableBytes() < elementSize) {
        return null;
      }
      byte[] stringBytes;
      if (nullsMap == null || !nullsMap.get(elements.size())) {
        stringBytes = new byte[elementSize];
        in.readBytes(stringBytes);
      } else {
        in.skipBytes(elementSize);
        stringBytes = null;
      }
      elements.add(stringBytes);
    }
    Object[] ret = elements.toArray();
    elements = null;
    return ret;
  }

  @Override
  protected Object getElementInternal(int rowIdx) {
    return getObjectsArrayElement(rowIdx);
  }
}

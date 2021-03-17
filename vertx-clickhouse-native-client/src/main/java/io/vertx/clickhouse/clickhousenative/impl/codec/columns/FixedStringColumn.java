package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FixedStringColumn extends ClickhouseColumn {
  private final Charset charset;

  private List<Object> elements;

  protected FixedStringColumn(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor, ClickhouseNativeDatabaseMetadata md) {
    super(nRows, columnDescriptor);
    this.elements = new ArrayList<>(nRows);
    this.charset = md.getStringCharset();
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
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    Object tmp = getObjectsArrayElement(rowIdx);
    if (desired == String.class && tmp != null) {
      return new String((byte[])tmp, charset);
    }
    return tmp;
  }
}

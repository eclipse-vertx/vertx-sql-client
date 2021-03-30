package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;
import io.vertx.sqlclient.Tuple;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class StringColumnReader extends ClickhouseColumnReader {
  private Integer curStrLength;
  private List<byte[]> elements;
  private final Charset charset;

  protected StringColumnReader(int nRows, ClickhouseNativeColumnDescriptor descriptor, ClickhouseNativeDatabaseMetadata md) {
    super(nRows, descriptor);
    this.elements = new ArrayList<>(nRows);
    this.charset = md.getStringCharset();
  }

  @Override
  protected Object[] readItems(ClickhouseStreamDataSource in) {
    while (elements.size() < nRows) {
      if (curStrLength == null) {
        curStrLength = in.readULeb128();
        if (curStrLength == null) {
          return null;
        }
      }
      if (in.readableBytes() < curStrLength) {
        return null;
      }
      byte[] stringBytes;
      if (nullsMap == null || !nullsMap.get(elements.size())) {
        stringBytes = new byte[curStrLength];
        in.readBytes(stringBytes);
      } else {
        stringBytes = null;
        in.skipBytes(curStrLength);
      }
      elements.add(stringBytes);
      curStrLength = null;
    }
    Object[] ret = elements.toArray(new byte[elements.size()][]);
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

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new StringColumnWriter(data, columnDescriptor, charset, columnIndex);
  }
}

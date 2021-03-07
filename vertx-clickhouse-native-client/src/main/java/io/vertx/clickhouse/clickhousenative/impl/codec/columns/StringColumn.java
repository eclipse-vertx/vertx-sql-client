package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ByteBufUtils;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.util.ArrayList;
import java.util.List;

public class StringColumn extends ClickhouseColumn {
  private Integer curStrLength;
  private List<Object> elements;

  protected StringColumn(int nRows, ClickhouseNativeColumnDescriptor descriptor) {
    super(nRows, descriptor);
    this.elements = new ArrayList<>(nRows);
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
      byte[] stringBytes = new byte[curStrLength];
      in.readBytes(stringBytes);
      elements.add(stringBytes);
      curStrLength = null;
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

package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.codec.ByteBufUtils;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.util.ArrayList;
import java.util.List;

public class StringColumn extends ClickhouseColumn {
  private Integer curStrLength;
  private List<Object> elements;

  protected StringColumn(int nItems, ClickhouseNativeColumnDescriptor descriptor) {
    super(nItems, descriptor);
    this.elements = new ArrayList<>(nItems);
  }

  @Override
  protected Object[] readItems(ByteBuf in) {
    while (elements.size() < nItems) {
      if (curStrLength == null) {
        curStrLength = ByteBufUtils.readULeb128(in);
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
}

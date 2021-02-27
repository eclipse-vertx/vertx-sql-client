package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseServerException;

public class ClickhouseExceptionReader {
  private Integer code;
  private String name;
  private String message;
  private String stacktrace;
  private Boolean hasNested;

  public ClickhouseServerException readFrom(ByteBuf in) {
    if (code == null) {
      if (in.readableBytes() >= 4) {
        code = in.readIntLE();
      } else {
        return null;
      }
    }
    if (name == null) {
      name = ByteBufUtils.readPascalString(in);
      if (name == null) {
        return null;
      }
    }
    if (message == null) {
      message = ByteBufUtils.readPascalString(in);
      if (message == null) {
        return null;
      }
    }
    if (stacktrace == null) {
      stacktrace = ByteBufUtils.readPascalString(in);
      if (stacktrace == null) {
        return null;
      }
    }
    if (hasNested == null) {
      if (in.readableBytes() >= 1) {
        hasNested = in.readByte() != 0;
      } else {
        return null;
      }
    }
    //TODO smagellan: read nested exception
    return new ClickhouseServerException(code, name, message, stacktrace, hasNested);
  }
}

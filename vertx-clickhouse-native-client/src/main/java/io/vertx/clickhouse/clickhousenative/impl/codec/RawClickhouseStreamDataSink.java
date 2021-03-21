package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;

class RawClickhouseStreamDataSink implements ClickhouseStreamDataSink {
  private final ByteBuf sink;

  public RawClickhouseStreamDataSink(ByteBuf sink) {
    this.sink = sink;
  }

  @Override
  public void writeULeb128(int value) {
    ByteBufUtils.writeULeb128(value, sink);
  }

  @Override
  public void writeByte(int value) {
    sink.writeByte(value);
  }

  @Override
  public void writeIntLE(int value) {
    sink.writeIntLE(value);
  }

  @Override
  public void writeBytes(byte[] value) {
    sink.writeBytes(value);
  }

  @Override
  public void writeBoolean(boolean value) {
    sink.writeBoolean(value);
  }

  @Override
  public void writePascalString(String str) {
    ByteBufUtils.writePascalString(str, sink);
  }
}

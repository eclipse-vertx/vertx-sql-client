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
}

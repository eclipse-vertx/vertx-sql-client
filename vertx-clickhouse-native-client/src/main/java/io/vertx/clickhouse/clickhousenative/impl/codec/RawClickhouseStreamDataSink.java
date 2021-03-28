package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;

public class RawClickhouseStreamDataSink implements ClickhouseStreamDataSink {
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
  public void writeShortLE(int value) {
    sink.writeShortLE(value);
  }

  @Override
  public void writeLongLE(long value) {
    sink.writeLongLE(value);
  }

  @Override
  public void writeFloatLE(float value) {
    sink.writeFloatLE(value);
  }

  @Override
  public void writeDoubleLE(double value) {
    sink.writeDoubleLE(value);
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
  public void writeZero(int length) {
    sink.writeZero(length);
  }

  @Override
  public void writePascalString(String str) {
    ByteBufUtils.writePascalString(str, sink);
  }

  @Override
  public void ensureWritable(int size) {
    sink.ensureWritable(size);
  }
}

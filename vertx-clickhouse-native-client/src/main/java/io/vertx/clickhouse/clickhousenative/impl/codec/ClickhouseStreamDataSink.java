package io.vertx.clickhouse.clickhousenative.impl.codec;

public interface ClickhouseStreamDataSink {
  void writeULeb128(int value);
  void writeByte(int value);
  void writeShortLE(int value);
  void writeIntLE(int value);
  void writeLongLE(long value);
  void writeFloatLE(float value);
  void writeDoubleLE(double value);
  void writeBytes(byte[] value);
  void writeBoolean(boolean value);
  void writeZero(int length);
  void writePascalString(String value);
  void ensureWritable(int size);

  default void finish() {
  }
}

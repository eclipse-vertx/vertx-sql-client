package io.vertx.clickhouse.clickhousenative.impl.codec;

public interface ClickhouseStreamDataSink {
  void writeULeb128(int value);
  void writeByte(int value);
  void writeIntLE(int value);
  void writeBytes(byte[] value);
  void writeBoolean(boolean value);
  void writePascalString(String value);

  default void finish() {
  }
}

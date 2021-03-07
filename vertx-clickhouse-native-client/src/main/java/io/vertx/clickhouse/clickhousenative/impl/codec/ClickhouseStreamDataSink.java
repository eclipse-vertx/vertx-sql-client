package io.vertx.clickhouse.clickhousenative.impl.codec;

public interface ClickhouseStreamDataSink {
  void writeULeb128(int value);
  void writeByte(int value);
  void writeIntLE(int value);

  default void finish() {
  }
}

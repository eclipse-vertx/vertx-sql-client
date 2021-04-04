package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

public interface EnumColumnReader {
  Object[] recodeValues(Object[] src, Class desired);
}

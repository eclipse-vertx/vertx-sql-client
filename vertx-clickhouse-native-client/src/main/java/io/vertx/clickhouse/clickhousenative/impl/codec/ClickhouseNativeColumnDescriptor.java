package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.vertx.sqlclient.desc.ColumnDescriptor;

import java.sql.JDBCType;

public class ClickhouseNativeColumnDescriptor implements ColumnDescriptor {
  public static final int NOSIZE = -1;

  private final String name;
  private final String unparsedNativeType;
  private final String nativeType;
  private final boolean isArray;
  private final JDBCType jdbcType;
  private final int elementSize;
  private final boolean nullable;

  public ClickhouseNativeColumnDescriptor(String name, String unparsedNativeType, String nativeType,
                                          boolean isArray, int elementSize, JDBCType jdbcType, boolean nullable) {
    this.name = name;
    this.unparsedNativeType = unparsedNativeType;
    this.nativeType = nativeType;
    this.isArray = isArray;
    this.elementSize = elementSize;
    this.jdbcType = jdbcType;
    this.nullable = nullable;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean isArray() {
    return isArray;
  }

  @Override
  public JDBCType jdbcType() {
    return jdbcType;
  }

  public int getElementSize() {
    return elementSize;
  }

  public boolean isNullable() {
    return nullable;
  }

  @Override
  public String toString() {
    return "ClickhouseNativeColumnDescriptor{" +
      "name='" + name + '\'' +
      ", unparsedNativeType='" + unparsedNativeType + '\'' +
      ", nativeType='" + nativeType + '\'' +
      ", isArray=" + isArray +
      ", jdbcType=" + jdbcType +
      ", elementSize=" + elementSize +
      ", nullable=" + nullable +
      '}';
  }
}

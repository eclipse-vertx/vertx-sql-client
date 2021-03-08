package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.vertx.sqlclient.desc.ColumnDescriptor;

import java.math.BigInteger;
import java.sql.JDBCType;

public class ClickhouseNativeColumnDescriptor implements ColumnDescriptor {
  public static final int NOSIZE = -1;

  private final String name;
  private final String unparsedNativeType;
  private final String nativeType;
  private final JDBCType jdbcType;
  private final int elementSize;
  private final boolean isArray;
  private final boolean nullable;
  private final boolean unsigned;
  private final boolean lowCardinality;
  private final BigInteger minValue;
  private final BigInteger maxValue;

  private final Integer precision;
  private final Integer scale;

  public ClickhouseNativeColumnDescriptor(String name, String unparsedNativeType, String nativeType,
                                          boolean isArray, int elementSize, JDBCType jdbcType,
                                          boolean nullable, boolean unsigned,
                                          boolean lowCardinality, Number minValue, Number maxValue) {
    this(name, unparsedNativeType, nativeType, isArray, elementSize, jdbcType, nullable, unsigned, lowCardinality, minValue, maxValue, null, null);
  }

  public ClickhouseNativeColumnDescriptor(String name, String unparsedNativeType, String nativeType,
                                          boolean isArray, int elementSize, JDBCType jdbcType,
                                          boolean nullable, boolean unsigned,
                                          boolean lowCardinality, Number minValue, Number maxValue,
                                          Integer precision, Integer scale) {
    this.name = name;
    this.unparsedNativeType = unparsedNativeType;
    this.nativeType = nativeType;
    this.isArray = isArray;
    this.elementSize = elementSize;
    this.jdbcType = jdbcType;
    this.nullable = nullable;
    this.unsigned = unsigned;
    this.lowCardinality = lowCardinality;
    this.minValue = bi(minValue);
    this.maxValue = bi(maxValue);
    this.precision = precision;
    this.scale = scale;
  }

  private BigInteger bi(Number src) {
    if (src instanceof Byte || src instanceof Integer || src instanceof Long) {
      return BigInteger.valueOf(src.longValue());
    }
    return (BigInteger) src;
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

  public String getUnparsedNativeType() {
    return unparsedNativeType;
  }

  public int getElementSize() {
    return elementSize;
  }

  public boolean isNullable() {
    return nullable;
  }

  public boolean isUnsigned() {
    return unsigned;
  }

  public boolean isLowCardinality() {
    return lowCardinality;
  }

  public BigInteger getMinValue() {
    return minValue;
  }

  public BigInteger getMaxValue() {
    return maxValue;
  }

  public String getNativeType() {
    return nativeType;
  }

  public Integer getPrecision() {
    return precision;
  }

  public Integer getScale() {
    return scale;
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

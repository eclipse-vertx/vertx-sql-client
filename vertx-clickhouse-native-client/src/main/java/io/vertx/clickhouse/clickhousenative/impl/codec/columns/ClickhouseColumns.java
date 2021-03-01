package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.math.BigInteger;
import java.sql.JDBCType;

public class ClickhouseColumns {
  public static final String NULLABLE_PREFIX = "Nullable(";
  public static final int NULLABLE_PREFIX_LENGTH = NULLABLE_PREFIX.length();

  public static final String ARRAY_PREFIX = "Array(";
  public static final int ARRAY_PREFIX_LENGTH = ARRAY_PREFIX.length();

  public static final String LOW_CARDINALITY_PREFIX = "LowCardinality";
  public static final int LOW_CARDINALITY_PREFIX_LENGTH = LOW_CARDINALITY_PREFIX.length();

  public static final String FIXED_STRING_PREFIX = "FixedString(";
  public static final int FIXED_STRING_PREFIX_LENGTH = FIXED_STRING_PREFIX.length();

  public static ClickhouseNativeColumnDescriptor columnDescriptorForSpec(String unparsedSpec, String name) {
    String spec = unparsedSpec;
    boolean nullable = false;
    boolean isArray = false;
    boolean isLowCardinality = false;
    if (spec.startsWith(ARRAY_PREFIX)) {
      spec = spec.substring(ARRAY_PREFIX_LENGTH, spec.length() - 1);
      isArray = true;
      throw new IllegalStateException("arrays are not supported");
    }
    if (spec.startsWith(LOW_CARDINALITY_PREFIX)) {
      spec = spec.substring(LOW_CARDINALITY_PREFIX_LENGTH, spec.length() - 1);
      isLowCardinality = true;
      throw new IllegalStateException("low cardinality columns are not supported");
    }
    if (spec.startsWith(NULLABLE_PREFIX)) {
      spec = spec.substring(NULLABLE_PREFIX_LENGTH, spec.length() - 1);
      nullable = true;
    }
    return columnDescriptorForSpec(unparsedSpec, spec, name, nullable, isArray, isLowCardinality);
  }

  public static ClickhouseNativeColumnDescriptor columnDescriptorForSpec(String unparsedSpec, String spec, String name,
                                                                         boolean nullable, boolean isArray, boolean isLowCardinality) {
    boolean unsigned = spec.startsWith("U");
    if (spec.equals("UInt8") || spec.equals("Int8")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, 1, JDBCType.TINYINT, nullable, unsigned, isLowCardinality,
        unsigned ? 0 : -128, unsigned ? 255 : 127);
    } else if (spec.equals("UInt16") || spec.equals("Int16")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, 2, JDBCType.SMALLINT, nullable, unsigned, isLowCardinality,
        unsigned ? 0 : -32768, unsigned ? 65535 : 32767);
    } if (spec.equals("UInt32") || spec.equals("Int32")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec,  isArray, 4, JDBCType.INTEGER, nullable, unsigned, isLowCardinality,
        unsigned ? 0 : -2147483648L, unsigned ? 4294967295L : 2147483647L);
    } if (spec.equals("UInt64") || spec.equals("Int64")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec,  isArray, 8, JDBCType.BIGINT, nullable, unsigned, isLowCardinality,
        unsigned ? BigInteger.ZERO : new BigInteger("-9223372036854775808"), unsigned ? new BigInteger("18446744073709551615") : new BigInteger("9223372036854775807"));
    } else if (spec.equals("String")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, ClickhouseNativeColumnDescriptor.NOSIZE, JDBCType.VARCHAR,
        nullable, false, isLowCardinality, null, null);
    } else if (spec.startsWith(FIXED_STRING_PREFIX)) {
      String lengthStr = spec.substring(FIXED_STRING_PREFIX_LENGTH, spec.length() - 1);
      int bytesLength = Integer.parseInt(lengthStr);
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, bytesLength, JDBCType.VARCHAR,
        nullable, false, isLowCardinality, null, null);
    }
    throw new IllegalArgumentException("unknown spec: '" + spec + "'");
  }

  public static ClickhouseColumn columnForSpec(String name, ClickhouseNativeColumnDescriptor descr, int nRows) {
    if (descr == null) {
      throw new IllegalArgumentException("no parsed spec for column name: " + name);
    }
    JDBCType jdbcType = descr.jdbcType();
    if (descr.isArray()) {

    } else {
      if (jdbcType == JDBCType.TINYINT) {
        return new UInt8Column(nRows, descr);
      } else if (jdbcType == JDBCType.SMALLINT) {
        return new UInt16Column(nRows, descr);
      } else if (jdbcType == JDBCType.INTEGER) {
        return new UInt32Column(nRows, descr);
      } else if (jdbcType == JDBCType.BIGINT && descr.getElementSize() == 8) {
        return new UInt64Column(nRows, descr);
      } else if (jdbcType == JDBCType.VARCHAR) {
        if (descr.getElementSize() == ClickhouseNativeColumnDescriptor.NOSIZE) {
          return new StringColumn(nRows, descr);
        } else {
          return new FixedStringColumn(nRows, descr);
        }
      }
    }
    throw new IllegalArgumentException("no column type for jdbc type " + jdbcType + " (raw type: '" + descr.getUnparsedNativeType() + "')");
  }
}

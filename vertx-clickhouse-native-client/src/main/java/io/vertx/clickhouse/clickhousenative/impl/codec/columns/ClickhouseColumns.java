package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.sql.JDBCType;
import java.util.Map;

public class ClickhouseColumns {
  public static final String NULLABLE_PREFIX = "Nullable(";
  public static final int NULLABLE_PREFIX_LENGTH = NULLABLE_PREFIX.length();

  public static final String FIXED_STRING_PREFIX = "FixedString(";
  public static final int FIXED_STRING_PREFIX_LENGTH = FIXED_STRING_PREFIX.length();

  public static ClickhouseNativeColumnDescriptor columnDescriptorForSpec(String spec, String name) {
    if (spec.startsWith(NULLABLE_PREFIX)) {
      String subSpec = spec.substring(NULLABLE_PREFIX_LENGTH, spec.length() - 1);
      return columnDescriptorForSpec(spec, subSpec, name, true);
    }
    return columnDescriptorForSpec(spec, spec, name, false);
  }

  public static ClickhouseNativeColumnDescriptor columnDescriptorForSpec(String unparsedSpec, String spec, String name, boolean nullable) {
    if (spec.equals("UInt32") || spec.equals("Int32")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, false, 4, JDBCType.INTEGER, nullable, spec.startsWith("U"));
    } else if (spec.equals("UInt8") || spec.equals("Int8")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec,false, 1, JDBCType.TINYINT, nullable, spec.startsWith("U"));
    } else if (spec.equals("String")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec,false, ClickhouseNativeColumnDescriptor.NOSIZE, JDBCType.VARCHAR, nullable, false);
    } else if (spec.startsWith(FIXED_STRING_PREFIX)) {
      String lengthStr = spec.substring(FIXED_STRING_PREFIX_LENGTH, spec.length() - 1);
      int bytesLength = Integer.parseInt(lengthStr);
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec,false, bytesLength, JDBCType.VARCHAR, nullable, false);
    }
    throw new IllegalArgumentException("unknown spec: " + spec);
  }

  public static ClickhouseColumn columnForSpec(String name, Map<String, ClickhouseNativeColumnDescriptor> parsedTypes, int nItems) {
    ClickhouseNativeColumnDescriptor descr = parsedTypes.get(name);
    if (descr == null) {
      throw new IllegalArgumentException("no parsed spec for column name: " + name);
    }
    JDBCType jdbcType = descr.jdbcType();
    if (jdbcType == JDBCType.INTEGER) {
      return new UInt32Column(nItems, descr);
    } else if (jdbcType == JDBCType.TINYINT) {
      return new UInt8Column(nItems, descr);
    } else if (jdbcType == JDBCType.VARCHAR) {
      if (descr.getElementSize() == ClickhouseNativeColumnDescriptor.NOSIZE) {
        return new StringColumn(nItems, descr);
      } else {
        return new FixedStringColumn(nItems, descr);
      }
    } else {
      throw new IllegalArgumentException("no column type for jdbc type " + jdbcType);
    }
  }
}

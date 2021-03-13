package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.math.BigInteger;
import java.sql.JDBCType;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class ClickhouseColumns {
  public static final String NULLABLE_PREFIX = "Nullable(";
  public static final int NULLABLE_PREFIX_LENGTH = NULLABLE_PREFIX.length();

  public static final String ARRAY_PREFIX = "Array(";
  public static final int ARRAY_PREFIX_LENGTH = ARRAY_PREFIX.length();

  public static final String LOW_CARDINALITY_PREFIX = "LowCardinality(";
  public static final int LOW_CARDINALITY_PREFIX_LENGTH = LOW_CARDINALITY_PREFIX.length();

  public static final String FIXED_STRING_PREFIX = "FixedString(";
  public static final int FIXED_STRING_PREFIX_LENGTH = FIXED_STRING_PREFIX.length();

  public static final String DECIMAL_PREFIX = "Decimal(";
  public static final int DECIMAL_PREFIX_LENGTH = DECIMAL_PREFIX.length();

  public static final String ENUM_PREFIX = "Enum";
  public static final int ENUM_PREFIX_LENGTH = ENUM_PREFIX.length();

  public static ClickhouseNativeColumnDescriptor columnDescriptorForSpec(String unparsedSpec, String name) {
    String spec = unparsedSpec;
    if (spec.startsWith(ARRAY_PREFIX)) {
      spec = spec.substring(ARRAY_PREFIX_LENGTH, spec.length() - 1);
      ClickhouseNativeColumnDescriptor nested = columnDescriptorForSpec(spec, name);
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, true, ClickhouseNativeColumnDescriptor.NOSIZE,
        JDBCType.ARRAY, false, false, false, null, null, nested);
    }
    boolean isLowCardinality = false;
    if (spec.startsWith(LOW_CARDINALITY_PREFIX)) {
      spec = spec.substring(LOW_CARDINALITY_PREFIX_LENGTH, spec.length() - 1);
      isLowCardinality = true;
    }
    boolean nullable = false;
    if (spec.startsWith(NULLABLE_PREFIX)) {
      spec = spec.substring(NULLABLE_PREFIX_LENGTH, spec.length() - 1);
      nullable = true;
    }
    return columnDescriptorForSpec(unparsedSpec, spec, name, nullable, false, isLowCardinality);
  }

  public static ClickhouseNativeColumnDescriptor columnDescriptorForSpec(String unparsedSpec, String spec, String name,
                                                                         boolean nullable, boolean isArray,
                                                                         boolean isLowCardinality) {
    boolean unsigned = spec.startsWith("U");
    if (spec.equals("UInt8") || spec.equals("Int8")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, UInt8Column.ELEMENT_SIZE, JDBCType.TINYINT, nullable, unsigned, isLowCardinality,
        unsigned ? 0 : -128, unsigned ? 255 : 127);
    } else if (spec.equals("UInt16") || spec.equals("Int16")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, UInt16Column.ELEMENT_SIZE, JDBCType.SMALLINT, nullable, unsigned, isLowCardinality,
        unsigned ? 0 : -32768, unsigned ? 65535 : 32767);
    } if (spec.equals("UInt32") || spec.equals("Int32")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec,  isArray, UInt32Column.ELEMENT_SIZE, JDBCType.INTEGER, nullable, unsigned, isLowCardinality,
        unsigned ? 0 : -2147483648L, unsigned ? 4294967295L : 2147483647L);
    } if (spec.equals("UInt64") || spec.equals("Int64")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec,  isArray, UInt64Column.ELEMENT_SIZE, JDBCType.BIGINT, nullable, unsigned, isLowCardinality,
        unsigned ? BigInteger.ZERO : new BigInteger("-9223372036854775808"),
        unsigned ? new BigInteger("18446744073709551615") : new BigInteger("9223372036854775807"));
    } if (spec.equals("Int128")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec,  isArray, UInt128Column.ELEMENT_SIZE, JDBCType.BIGINT, nullable, false, isLowCardinality,
        new BigInteger("-170141183460469231731687303715884105728"), new BigInteger( "170141183460469231731687303715884105727"));
    } else if (spec.equals("String")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, ClickhouseNativeColumnDescriptor.NOSIZE, JDBCType.VARCHAR,
        nullable, false, isLowCardinality, null, null);
    } else if (spec.startsWith(FIXED_STRING_PREFIX)) {
      String lengthStr = spec.substring(FIXED_STRING_PREFIX_LENGTH, spec.length() - 1);
      int bytesLength = Integer.parseInt(lengthStr);
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, bytesLength, JDBCType.VARCHAR,
        nullable, false, isLowCardinality, null, null);
    } else if (spec.equals("DateTime") || spec.startsWith("DateTime(")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, DateTimeColumn.ELEMENT_SIZE,
        spec.endsWith(")") ? JDBCType.TIMESTAMP_WITH_TIMEZONE : JDBCType.TIMESTAMP, nullable, false, isLowCardinality, null, null);
    } else if (spec.equals("DateTime64") || spec.startsWith("DateTime64(")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, DateTime64Column.ELEMENT_SIZE,
        spec.endsWith(")") ? JDBCType.TIMESTAMP_WITH_TIMEZONE : JDBCType.TIMESTAMP, nullable, false, isLowCardinality, null, null);
    } else if (spec.equals("UUID")) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, UUIDColumn.ELEMENT_SIZE,
        JDBCType.OTHER, nullable, false, isLowCardinality, null, null);
    } else if (spec.startsWith(DECIMAL_PREFIX)) {
      String decimalModifiers = spec.substring(DECIMAL_PREFIX_LENGTH, spec.length() - 1);
      String[] modifiersTokens = decimalModifiers.split(",");
      int precision = Integer.parseInt(modifiersTokens[0].trim());
      int scale = Integer.parseInt(modifiersTokens[1].trim());
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, decimalSize(precision),
        JDBCType.DECIMAL, nullable, false, isLowCardinality, null, null, precision, scale);
    } else if (spec.startsWith(ENUM_PREFIX)) {
      int openBracketPos = spec.indexOf('(', ENUM_PREFIX_LENGTH);
      int enumBitsSize = Integer.parseInt(spec.substring(ENUM_PREFIX_LENGTH, openBracketPos));
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, enumBitsSize / 8,
        JDBCType.OTHER, nullable, false, isLowCardinality, null, null, null, null);
    } else if ("Nothing".equals(spec)) {
      return new ClickhouseNativeColumnDescriptor(name, unparsedSpec, spec, isArray, UInt8Column.ELEMENT_SIZE,
        JDBCType.NULL, nullable, false, isLowCardinality, null, null, null, null);
    }
    throw new IllegalArgumentException("unknown spec: '" + spec + "'");
  }

  private static int decimalSize(int precision) {
    if (precision <= Decimal32Column.MAX_PRECISION) {
      return Decimal32Column.ELEMENT_SIZE;
    } else if (precision <= Decimal64Column.MAX_PRECISION) {
      return Decimal64Column.ELEMENT_SIZE;
    } else if (precision <= Decimal128Column.MAX_PRECISION) {
      return Decimal128Column.ELEMENT_SIZE;
    } else {
      return Decimal256Column.ELEMENT_SIZE;
    }
  }

  public static ClickhouseColumn columnForSpec(ClickhouseNativeColumnDescriptor descr, int nRows) {
    if (descr.isArray()) {
      return new ArrayColumn(nRows, descr);
    }
    if (descr.isLowCardinality()) {
      return new LowCardinalityColumn(nRows, descr);
    }
    JDBCType jdbcType = descr.jdbcType();
    if (jdbcType == JDBCType.TINYINT || jdbcType == JDBCType.NULL) {
      return new UInt8Column(nRows, descr);
    } else if (jdbcType == JDBCType.SMALLINT) {
      return new UInt16Column(nRows, descr);
    } else if (jdbcType == JDBCType.INTEGER) {
      return new UInt32Column(nRows, descr);
    } else if (jdbcType == JDBCType.BIGINT) {
      if (descr.getElementSize() == UInt64Column.ELEMENT_SIZE) {
        return new UInt64Column(nRows, descr);
      } else if (descr.getElementSize() == UInt128Column.ELEMENT_SIZE) {
        return new UInt128Column(nRows, descr);
      }
    } else if (jdbcType == JDBCType.VARCHAR) {
      if (descr.getElementSize() == ClickhouseNativeColumnDescriptor.NOSIZE) {
        return new StringColumn(nRows, descr);
      } else {
        return new FixedStringColumn(nRows, descr);
      }
    } else if (jdbcType == JDBCType.TIMESTAMP || jdbcType == JDBCType.TIMESTAMP_WITH_TIMEZONE) {
      ZoneId zoneId;
      Integer precision = null;
      String nativeType = descr.getNestedType();
      if (nativeType.endsWith(")")) {
        int openBracePos = nativeType.indexOf("(");
        String dateModifiers = nativeType.substring(openBracePos + 1, nativeType.length() - 1);
        if (descr.getElementSize() == DateTime64Column.ELEMENT_SIZE) {
          String[] modifiers = dateModifiers.split(",");
          precision = Integer.parseInt(modifiers[0].trim());
          zoneId = modifiers.length == 2
            ? ZoneId.of(modifiers[1].trim())
            : ZoneId.systemDefault();
        } else {
          zoneId = ZoneId.of(dateModifiers);
        }
      } else {
        zoneId = ZoneId.systemDefault();
      }
      return precision == null ? new DateTimeColumn(nRows, descr, zoneId) : new DateTime64Column(nRows, descr, precision, zoneId);
    } else if (jdbcType == JDBCType.DECIMAL) {
      //TODO smagellan: merge into one statement after introducing column readers
      if (descr.getElementSize() == Decimal32Column.ELEMENT_SIZE) {
        return new Decimal32Column(nRows, descr);
      } else if (descr.getElementSize() == Decimal64Column.ELEMENT_SIZE) {
        return new Decimal64Column(nRows, descr);
      } else if (descr.getElementSize() == Decimal128Column.ELEMENT_SIZE) {
        return new Decimal128Column(nRows, descr);
      } else if (descr.getElementSize() == Decimal256Column.ELEMENT_SIZE) {
        return new Decimal256Column(nRows, descr);
      }
    } else if (jdbcType == JDBCType.OTHER) {
      if (descr.getNestedType().equals("UUID")) {
        return new UUIDColumn(nRows, descr);
      } else if (descr.getNestedType().startsWith(ENUM_PREFIX)) {
        Map<? extends Number, String> enumVals = parseEnumVals(descr.getNestedType());
        if (descr.getElementSize() == Enum8Column.ELEMENT_SIZE) {
          return new Enum8Column(nRows, descr, enumVals);
        } else if (descr.getElementSize() == Enum16Column.ELEMENT_SIZE) {
          return new Enum16Column(nRows, descr, enumVals);
        }
      }
    }
    throw new IllegalArgumentException("no column type for jdbc type " + jdbcType + " (raw type: '" + descr.getUnparsedNativeType() + "')");
  }


  //TODO: maybe switch to antl4
  static Map<? extends Number, String> parseEnumVals(String nativeType) {
    final boolean isByte = nativeType.startsWith("Enum8(");
    int openBracketPos = nativeType.indexOf('(');
    Map<Number, String> result = new HashMap<>();
    int lastQuotePos = -1;
    boolean gotEq = false;
    String enumElementName = null;
    int startEnumValPos = -1;
    for (int i = openBracketPos; i < nativeType.length(); ++i) {
      char ch = nativeType.charAt(i);
      if (ch == '\'') {
        if (lastQuotePos == -1) {
          lastQuotePos = i;
        } else {
          enumElementName = nativeType.substring(lastQuotePos + 1, i);
          lastQuotePos = -1;
        }
      } else if (ch == '=') {
        gotEq = true;
      } else if (gotEq) {
        if (Character.isDigit(ch)) {
          if (startEnumValPos == -1) {
            startEnumValPos = i;
          } else if (!Character.isDigit(nativeType.charAt(i + 1))) {
            int enumValue = Integer.parseInt(nativeType.substring(startEnumValPos, i + 1));
            Number key = byteOrShort(enumValue, isByte);
            result.put(key, enumElementName);
            startEnumValPos = -1;
            enumElementName = null;
            gotEq = false;
          }
        } else if (startEnumValPos != -1) {
          int enumValue = Integer.parseInt(nativeType.substring(startEnumValPos, i));
          Number key = byteOrShort(enumValue, isByte);
          result.put(key, enumElementName);
          startEnumValPos = -1;
          enumElementName = null;
          gotEq = false;
        }
      }
    }
    return result;
  }

  private static Number byteOrShort(int number, boolean isByte) {
    if (isByte) {
      return (byte) number;
    }
    return (short) number;
  }

  public static void main(String[] args) {
    ClickhouseNativeColumnDescriptor t = columnDescriptorForSpec("Array(Array(LowCardinality(Nullable(String))))", "fake");
    System.err.println(t);
  }
}

package io.vertx.mssqlclient.impl.protocol.datatype;

// NUMERIC, NUMERICN, DECIMAL, or DECIMALN.
public class NumericDataType extends MSSQLDataType {
  private final int precision;
  private final int scale;

  public NumericDataType(int id, Class<?> mappedJavaType, int precision, int scale) {
    super(id, mappedJavaType);
    this.precision = precision;
    this.scale = scale;
  }

  public int precision() {
    return precision;
  }

  public int scale() {
    return scale;
  }
}

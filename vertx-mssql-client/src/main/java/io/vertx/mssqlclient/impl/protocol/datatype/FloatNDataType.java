package io.vertx.mssqlclient.impl.protocol.datatype;

import java.sql.JDBCType;

/**
 * FLTNTYPE, Variable-Length Data type, the only valid lengths are 0x04 and 0x08, which map to 7-digit precision float and 15-digit precision float SQL data types respectively.
 */
public class FloatNDataType extends MSSQLDataType {
  public static final FloatNDataType FLT_4_DATA_TYPE = new FloatNDataType(MSSQLDataTypeId.FLTNTYPE_ID, Float.class, 4, JDBCType.REAL);
  public static final FloatNDataType FLT_8_DATA_TYPE = new FloatNDataType(MSSQLDataTypeId.FLTNTYPE_ID, Double.class, 8, JDBCType.DOUBLE);

  private final int length;

  public FloatNDataType(int id, Class<?> mappedJavaType, int length, JDBCType jdbcType) {
    super(id, mappedJavaType, jdbcType);
    this.length = length;
  }

  public int length() {
    return length;
  }

  public static FloatNDataType valueOf(int length) {
    switch (length) {
      case 4:
        return FLT_4_DATA_TYPE;
      case 8:
        return FLT_8_DATA_TYPE;
      default:
        throw new UnsupportedOperationException(String.format("SEVERE: Unsupported length=[%d] for decoding FLTNTYPE column metadata.", length));
    }
  }
}

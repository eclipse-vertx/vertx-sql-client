package io.vertx.mssqlclient.impl.protocol.datatype;

import io.vertx.core.buffer.Buffer;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FixedLenDataType extends MSSQLDataType {
  public FixedLenDataType(int id, Class<?> mappedJavaType) {
    super(id, mappedJavaType);
  }

  public static FixedLenDataType NULLTYPE = new FixedLenDataType(MSSQLDataTypeId.NULLTYPE_ID, null);
  public static FixedLenDataType INT1TYPE = new FixedLenDataType(MSSQLDataTypeId.INT1TYPE_ID, Byte.class);
  public static FixedLenDataType BITTYPE = new FixedLenDataType(MSSQLDataTypeId.BITTYPE_ID, Buffer.class);
  public static FixedLenDataType INT2TYPE = new FixedLenDataType(MSSQLDataTypeId.INT2TYPE_ID, Short.class);
  public static FixedLenDataType INT4TYPE = new FixedLenDataType(MSSQLDataTypeId.INT4TYPE_ID, Integer.class);
  public static FixedLenDataType DATETIM4TYPE = new FixedLenDataType(MSSQLDataTypeId.DATETIM4TYPE_ID, LocalDateTime.class);
  public static FixedLenDataType FLT4TYPE = new FixedLenDataType(MSSQLDataTypeId.FLT4TYPE_ID, Float.class);
  public static FixedLenDataType MONEYTYPE = new FixedLenDataType(MSSQLDataTypeId.MONEYTYPE_ID, null); //TODO
  public static FixedLenDataType DATETIMETYPE = new FixedLenDataType(MSSQLDataTypeId.DATETIMETYPE_ID, LocalDateTime.class);
  public static FixedLenDataType FLT8TYPE = new FixedLenDataType(MSSQLDataTypeId.FLT8TYPE_ID, Double.class);
  public static FixedLenDataType MONEY4TYPE = new FixedLenDataType(MSSQLDataTypeId.MONEY4TYPE_ID, null); //TODO
  public static FixedLenDataType INT8TYPE = new FixedLenDataType(MSSQLDataTypeId.INT8TYPE_ID, Long.class);

  // DATENTYPE 0 or 3 length
  public static FixedLenDataType DATENTYPE = new FixedLenDataType(MSSQLDataTypeId.DATENTYPE_ID, LocalDate.class);
}

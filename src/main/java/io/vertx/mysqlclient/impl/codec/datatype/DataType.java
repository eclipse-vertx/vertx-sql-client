package io.vertx.mysqlclient.impl.codec.datatype;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.vertx.pgclient.data.Numeric;
import io.vertx.core.buffer.Buffer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public enum DataType {
  INT1(ColumnDefinition.ColumnType.MYSQL_TYPE_TINY, Byte.class, Byte.class),
  INT2(ColumnDefinition.ColumnType.MYSQL_TYPE_SHORT, Short.class, Short.class),
  INT3(ColumnDefinition.ColumnType.MYSQL_TYPE_INT24, Integer.class, Integer.class),
  INT4(ColumnDefinition.ColumnType.MYSQL_TYPE_LONG, Integer.class, Integer.class),
  INT8(ColumnDefinition.ColumnType.MYSQL_TYPE_LONGLONG, Long.class, Long.class),
  DOUBLE(ColumnDefinition.ColumnType.MYSQL_TYPE_DOUBLE, Double.class, Double.class),
  FLOAT(ColumnDefinition.ColumnType.MYSQL_TYPE_FLOAT, Float.class, Float.class),
  NUMERIC(ColumnDefinition.ColumnType.MYSQL_TYPE_NEWDECIMAL, Numeric.class, Numeric.class), // DECIMAL
  STRING(ColumnDefinition.ColumnType.MYSQL_TYPE_STRING, Buffer.class, String.class), // CHAR, BINARY
  VARSTRING(ColumnDefinition.ColumnType.MYSQL_TYPE_VAR_STRING, Buffer.class, String.class), //VARCHAR, VARBINARY
  BLOB(ColumnDefinition.ColumnType.MYSQL_TYPE_BLOB, Buffer.class, String.class),
  DATE(ColumnDefinition.ColumnType.MYSQL_TYPE_DATE, LocalDate.class, LocalDate.class),
  TIME(ColumnDefinition.ColumnType.MYSQL_TYPE_TIME, LocalTime.class, LocalTime.class),
  DATETIME(ColumnDefinition.ColumnType.MYSQL_TYPE_DATETIME, LocalDateTime.class, LocalDateTime.class),
  NULL(ColumnDefinition.ColumnType.MYSQL_TYPE_NULL, null, null);

  private static IntObjectMap<DataType> idToDataType = new IntObjectHashMap<>();

  static {
    for (DataType dataType : values()) {
      idToDataType.put(dataType.id, dataType);
    }
  }

  public final int id;
  public final Class<?> binaryType;
  public final Class<?> textType;

  DataType(int id, Class<?> binaryType, Class<?> textType) {
    this.id = id;
    this.binaryType = binaryType;
    this.textType = textType;
  }

  public static DataType valueOf(int value) {
    DataType dataType = idToDataType.get(value);
    if (dataType == null) {
//      logger.warn("MySQL type =" + value + " not handled - using unknown type instead");
      //TODO need better handling
      return null;
    } else {
      return dataType;
    }
  }
}

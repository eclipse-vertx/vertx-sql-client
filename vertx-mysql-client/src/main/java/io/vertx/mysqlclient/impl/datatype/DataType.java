package io.vertx.mysqlclient.impl.datatype;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.sqlclient.data.Numeric;

import java.sql.JDBCType;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public enum DataType {
  INT1(ColumnDefinition.ColumnType.MYSQL_TYPE_TINY, Byte.class, Byte.class, JDBCType.TINYINT),
  INT2(ColumnDefinition.ColumnType.MYSQL_TYPE_SHORT, Short.class, Short.class, JDBCType.SMALLINT),
  INT3(ColumnDefinition.ColumnType.MYSQL_TYPE_INT24, Integer.class, Integer.class, JDBCType.INTEGER),
  INT4(ColumnDefinition.ColumnType.MYSQL_TYPE_LONG, Integer.class, Integer.class, JDBCType.INTEGER),
  INT8(ColumnDefinition.ColumnType.MYSQL_TYPE_LONGLONG, Long.class, Long.class, JDBCType.BIGINT),
  DOUBLE(ColumnDefinition.ColumnType.MYSQL_TYPE_DOUBLE, Double.class, Double.class, JDBCType.DOUBLE),
  FLOAT(ColumnDefinition.ColumnType.MYSQL_TYPE_FLOAT, Float.class, Float.class, JDBCType.REAL),
  NUMERIC(ColumnDefinition.ColumnType.MYSQL_TYPE_NEWDECIMAL, Numeric.class, Numeric.class, JDBCType.NUMERIC), // DECIMAL
  STRING(ColumnDefinition.ColumnType.MYSQL_TYPE_STRING, Buffer.class, String.class, JDBCType.VARCHAR), // CHAR, BINARY
  VARSTRING(ColumnDefinition.ColumnType.MYSQL_TYPE_VAR_STRING, Buffer.class, String.class, JDBCType.VARCHAR), //VARCHAR, VARBINARY
  TINYBLOB(ColumnDefinition.ColumnType.MYSQL_TYPE_TINY_BLOB, Buffer.class, String.class, JDBCType.BINARY),
  BLOB(ColumnDefinition.ColumnType.MYSQL_TYPE_BLOB, Buffer.class, String.class, JDBCType.BINARY),
  MEDIUMBLOB(ColumnDefinition.ColumnType.MYSQL_TYPE_MEDIUM_BLOB, Buffer.class, String.class, JDBCType.BINARY),
  LONGBLOB(ColumnDefinition.ColumnType.MYSQL_TYPE_LONG_BLOB, Buffer.class, String.class, JDBCType.BINARY),
  DATE(ColumnDefinition.ColumnType.MYSQL_TYPE_DATE, LocalDate.class, LocalDate.class, JDBCType.DATE),
  TIME(ColumnDefinition.ColumnType.MYSQL_TYPE_TIME, Duration.class, Duration.class, JDBCType.DATE),
  DATETIME(ColumnDefinition.ColumnType.MYSQL_TYPE_DATETIME, LocalDateTime.class, LocalDateTime.class, JDBCType.DATE),
  YEAR(ColumnDefinition.ColumnType.MYSQL_TYPE_YEAR, Short.class, Short.class, JDBCType.DATE),
  TIMESTAMP(ColumnDefinition.ColumnType.MYSQL_TYPE_TIMESTAMP, LocalDateTime.class, LocalDateTime.class, JDBCType.DATE),
  BIT(ColumnDefinition.ColumnType.MYSQL_TYPE_BIT, Long.class, Long.class, JDBCType.OTHER),
  JSON(ColumnDefinition.ColumnType.MYSQL_TYPE_JSON, Object.class, Object.class, JDBCType.OTHER),
  NULL(ColumnDefinition.ColumnType.MYSQL_TYPE_NULL, Object.class, Object.class, JDBCType.OTHER), // useful for mariadb prepare statement response
  UNBIND(-1, Object.class, Object.class, JDBCType.OTHER); // useful for binding param values

  private static final Logger LOGGER = LoggerFactory.getLogger(DataType.class);

  private static IntObjectMap<DataType> idToDataType = new IntObjectHashMap<>();

  static {
    for (DataType dataType : values()) {
      idToDataType.put(dataType.id, dataType);
    }
  }

  public final int id;
  public final Class<?> binaryType;
  public final Class<?> textType;
  public final JDBCType jdbcType;

  DataType(int id, Class<?> binaryType, Class<?> textType, JDBCType jdbcType) {
    this.id = id;
    this.binaryType = binaryType;
    this.textType = textType;
    this.jdbcType = jdbcType;
  }

  public static DataType valueOf(int value) {
    DataType dataType = idToDataType.get(value);
    if (dataType == null) {
      LOGGER.warn(String.format("MySQL data type Id =[%d] not handled - using string type instead", value));
      return STRING;
    } else {
      return dataType;
    }
  }
}

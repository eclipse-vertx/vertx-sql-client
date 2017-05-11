package io.vertx.pgclient.codec.decoder.message;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public enum ColumnType {
  UNKNOWN(0),
  INT2(21),
  INT2_ARRAY(1005),
  INT4(23),
  INT4_ARRAY(1007),
  INT8(20),
  INT8_ARRAY(1016),
  TEXT(25),
  TEXT_ARRAY(1009),
  NUMERIC(1700),
  NUMERIC_ARRAY(1231),
  FLOAT4(700),
  FLOAT4_ARRAY(1021),
  FLOAT8(701),
  FLOAT8_ARRAY(1022),
  BOOL(16),
  BOOL_ARRAY(1000),
  DATE(1082),
  DATE_ARRAY(1182),
  TIME(1083),
  TIME_ARRAY(1183),
  TIMETZ(1266),
  TIMETZ_ARRAY(1270),
  TIMESTAMP(1114),
  TIMESTAMP_ARRAY(1115),
  TIMESTAMPTZ(1184),
  TIMESTAMPTZ_ARRAY(1185),
  BYTEA(17),
  BYTEA_ARRAY(1001),
  VARCHAR(1043),
  VARCHAR_ARRAY(1015),
  OID(26),
  OID_ARRAY(1028),
  BPCHAR(1042),
  BPCHAR_ARRAY(1014),
  MONEY(790),
  MONEY_ARRAY(791),
  NAME(19),
  NAME_ARRAY(1003),
  BIT(1560),
  BIT_ARRAY(1561),
  VOID(2278),
  INTERVAL(1186),
  INTERVAL_ARRAY(1187),
  CHAR(18),
  CHAR_ARRAY(1002),
  VARBIT(1562),
  VARBIT_ARRAY(1563),
  UUID(2950),
  UUID_ARRAY(2951),
  XML(142),
  XML_ARRAY(143),
  POINT(600),
  BOX(603),
  JSON(114),
  JSONB(3802),
  HSTORE(33670);
  final int id;
  ColumnType(int id) {
    this.id = id;
  }
  public static ColumnType get(int id) {
    for (ColumnType type : values()) {
      if (type.id == id) {
        return type;
      }
    }
    return ColumnType.UNKNOWN;
  }
}
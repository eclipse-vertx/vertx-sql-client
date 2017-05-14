package io.vertx.pgclient.codec.decoder.message;

/**
 *
 * PostgreSQL <a href="https://github.com/postgres/postgres/blob/master/src/include/catalog/pg_type.h">object
 * identifiers (OIDs)</a> for column data types
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ColumnType {
  // 1 byte
  public static final int BOOL = 16;
  public static final int BOOL_ARRAY = 1000;
  // 2 bytes
  public static final int INT2 = 21;
  public static final int INT2_ARRAY = 1005;
  // 4 bytes
  public static final int INT4 = 23;
  public static final int INT4_ARRAY = 1007;
  // 8 bytes
  public static final int INT8 = 20;
  public static final int INT8_ARRAY = 1016;
  // 4 bytes single-precision floating point number
  public static final int FLOAT4 = 700;
  public static final int FLOAT4_ARRAY = 1021;
  // 8 bytes double-precision floating point number
  public static final int FLOAT8 = 701;
  public static final int FLOAT8_ARRAY = 1022;
  // User specified precision
  public static final int NUMERIC = 1700;
  public static final int NUMERIC_ARRAY = 1231;
  // 8 bytes double
  public static final int MONEY = 790;
  public static final int MONEY_ARRAY =791;
  // Fixed length bit string
  public static final int BIT = 1560;
  public static final int BIT_ARRAY = 1561;
  // Limited length bit string
  public static final int VARBIT = 1562;
  public static final int VARBIT_ARRAY = 1563;
  // Single length character
  public static final int CHAR = 18;
  public static final int CHAR_ARRAY = 1002;
  // Limited length string
  public static final int VARCHAR = 1043;
  public static final int VARCHAR_ARRAY = 1015;
  // Limited blank padded length string
  public static final int BPCHAR = 1042;
  public static final int BPCHAR_ARRAY = 1014;
  // Unlimited length string
  public static final int TEXT = 25;
  public static final int TEXT_ARRAY = 1009;
  // 63 bytes length string (internal type for object names)
  public static final int NAME = 19;
  public static final int NAME_ARRAY = 1003;
  // 4 bytes date (no time of day)
  public static final int DATE = 1082;
  public static final int DATE_ARRAY = 1182;
  // 8 bytes time of day (no date) without time zone
  public static final int TIME = 1083;
  public static final int TIME_ARRAY = 1183;
  // 12 bytes time of day (no date) with time zone
  public static final int TIMETZ = 1266;
  public static final int TIMETZ_ARRAY = 1270;
  // 8 bytes date and time without time zone
  public static final int TIMESTAMP = 1114;
  public static final int TIMESTAMP_ARRAY = 1115;
  // 8 bytes date and time with time zone
  public static final int TIMESTAMPTZ = 1184;
  public static final int TIMESTAMPTZ_ARRAY = 1185;
  // 16 bytes time interval
  public static final int INTERVAL = 1186;
  public static final int INTERVAL_ARRAY = 1187;
  // 1 or 4 bytes plus the actual binary string
  public static final int BYTEA = 17;
  public static final int BYTEA_ARRAY = 1001;
  // 6 bytes MAC address (XX:XX:XX:XX:XX:XX)
  public static final int MACADDR = 829;
  // 7 or 19 bytes (IPv4 and IPv6 hosts and networks)
  public static final int INET = 869;
  // 7 or 19 bytes (IPv4 and IPv6 networks)
  public static final int CIDR = 650;
  // 8 bytes MAC address (XX:XX:XX:XX:XX:XX:XX:XX)
  public static final int MACADDR8 = 774;
  // UUID
  public static final int UUID = 2950;
  public static final int UUID_ARRAY = 2951;
  // Text JSON
  public static final int JSON = 114;
  // Binary JSON
  public static final int JSONB = 3802;
  // XML
  public static final int XML = 142;
  public static final int XML_ARRAY = 143;
  // Geometric point (x, y)
  public static final int POINT = 600;
  // Geometric box (lower left, upper right)
  public static final int BOX = 603;
  public static final int HSTORE = 33670;
  // Object identifier
  public static final int OID = 26;
  public static final int OID_ARRAY = 1028;
  public static final int VOID = 2278;
  public static final int UNKNOWN = 705;
}
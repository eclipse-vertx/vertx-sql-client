package io.vertx.mysqlclient.impl.codec;

final class ColumnDefinition {
  /*
    https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_column_definition.html
   */
  private final String catalog;
  private final String schema;
  private final String table;
  private final String orgTable;
  private final String name;
  private final String orgName;
  private final int characterSet;
  private final long columnLength;
  private final DataType type;
  private final int flags;
  private final byte decimals;

  ColumnDefinition(String catalog,
                   String schema,
                   String table,
                   String orgTable,
                   String name,
                   String orgName,
                   int characterSet,
                   long columnLength,
                   DataType type,
                   int flags,
                   byte decimals) {
    this.catalog = catalog;
    this.schema = schema;
    this.table = table;
    this.orgTable = orgTable;
    this.name = name;
    this.orgName = orgName;
    this.characterSet = characterSet;
    this.columnLength = columnLength;
    this.type = type;
    this.flags = flags;
    this.decimals = decimals;
  }

  String catalog() {
    return catalog;
  }

  String schema() {
    return schema;
  }

  String table() {
    return table;
  }

  String orgTable() {
    return orgTable;
  }

  String name() {
    return name;
  }

  String orgName() {
    return orgName;
  }

  int characterSet() {
    return characterSet;
  }

  long columnLength() {
    return columnLength;
  }

  DataType type() {
    return type;
  }

  int flags() {
    return flags;
  }

  byte decimals() {
    return decimals;
  }

  @Override
  public String toString() {
    return "ColumnDefinition{" +
      "catalog='" + catalog + '\'' +
      ", schema='" + schema + '\'' +
      ", table='" + table + '\'' +
      ", orgTable='" + orgTable + '\'' +
      ", name='" + name + '\'' +
      ", orgName='" + orgName + '\'' +
      ", characterSet=" + characterSet +
      ", columnLength=" + columnLength +
      ", type=" + type +
      ", flags=" + flags +
      ", decimals=" + decimals +
      '}';
  }

  /*
    Type of column definition
    https://dev.mysql.com/doc/dev/mysql-server/latest/binary__log__types_8h.html#aab0df4798e24c673e7686afce436aa85
   */
  static final class ColumnType {
    static final int MYSQL_TYPE_DECIMAL = 0x00;
    static final int MYSQL_TYPE_TINY = 0x01;
    static final int MYSQL_TYPE_SHORT = 0x02;
    static final int MYSQL_TYPE_LONG = 0x03;
    static final int MYSQL_TYPE_FLOAT = 0x04;
    static final int MYSQL_TYPE_DOUBLE = 0x05;
    static final int MYSQL_TYPE_NULL = 0x06;
    static final int MYSQL_TYPE_TIMESTAMP = 0x07;
    static final int MYSQL_TYPE_LONGLONG = 0x08;
    static final int MYSQL_TYPE_INT24 = 0x09;
    static final int MYSQL_TYPE_DATE = 0x0A;
    static final int MYSQL_TYPE_TIME = 0x0B;
    static final int MYSQL_TYPE_DATETIME = 0x0C;
    static final int MYSQL_TYPE_YEAR = 0x0D;
    static final int MYSQL_TYPE_VARCHAR = 0x0F;
    static final int MYSQL_TYPE_BIT = 0x10;
    static final int MYSQL_TYPE_JSON = 0xF5;
    static final int MYSQL_TYPE_NEWDECIMAL = 0xF6;
    static final int MYSQL_TYPE_ENUM = 0xF7;
    static final int MYSQL_TYPE_SET = 0xF8;
    static final int MYSQL_TYPE_TINY_BLOB = 0xF9;
    static final int MYSQL_TYPE_MEDIUM_BLOB = 0xFA;
    static final int MYSQL_TYPE_LONG_BLOB = 0xFB;
    static final int MYSQL_TYPE_BLOB = 0xFC;
    static final int MYSQL_TYPE_VAR_STRING = 0xFD;
    static final int MYSQL_TYPE_STRING = 0xFE;
    static final int MYSQL_TYPE_GEOMETRY = 0xFF;

    /*
      Internal to MySQL Server
     */
    private static final int MYSQL_TYPE_NEWDATE = 0x0E;
    private static final int MYSQL_TYPE_TIMESTAMP2 = 0x11;
    private static final int MYSQL_TYPE_DATETIME2 = 0x12;
    private static final int MYSQL_TYPE_TIME2 = 0x13;
  }

  /*
    https://dev.mysql.com/doc/dev/mysql-server/latest/group__group__cs__column__definition__flags.html
   */
  static final class ColumnDefinitionFlags {
    static final int NOT_NULL_FLAG = 0x00000001;
    static final int PRI_KEY_FLAG = 0x00000002;
    static final int UNIQUE_KEY_FLAG = 0x00000004;
    static final int MULTIPLE_KEY_FLAG = 0x00000008;
    static final int BLOB_FLAG = 0x00000010;
    static final int UNSIGNED_FLAG = 0x00000020;
    static final int ZEROFILL_FLAG = 0x00000040;
    static final int BINARY_FLAG = 0x00000080;
    static final int ENUM_FLAG = 0x00000100;
    static final int AUTO_INCREMENT_FLAG = 0x00000200;
    static final int TIMESTAMP_FLAG = 0x00000400;
    static final int SET_FLAG = 0x00000800;
    static final int NO_DEFAULT_VALUE_FLAG = 0x00001000;
    static final int ON_UPDATE_NOW_FLAG = 0x00002000;
    static final int NUM_FLAG = 0x00008000;
    static final int PART_KEY_FLAG = 0x00004000;
    static final int GROUP_FLAG = 0x00008000;
    static final int UNIQUE_FLAG = 0x00010000;
    static final int BINCMP_FLAG = 0x00020000;
    static final int GET_FIXED_FIELDS_FLAG = 0x00040000;
    static final int FIELD_IN_PART_FUNC_FLAG = 0x00080000;
    static final int FIELD_IN_ADD_INDEX = 0x00100000;
    static final int FIELD_IS_RENAMED = 0x00200000;
    static final int FIELD_FLAGS_STORAGE_MEDIA = 22;
    static final int FIELD_FLAGS_STORAGE_MEDIA_MASK = 3 << FIELD_FLAGS_STORAGE_MEDIA;
    static final int FIELD_FLAGS_COLUMN_FORMAT = 24;
    static final int FIELD_FLAGS_COLUMN_FORMAT_MASK = 3 << FIELD_FLAGS_COLUMN_FORMAT;
    static final int FIELD_IS_DROPPED = 0x04000000;
    static final int EXPLICIT_NULL_FLAG = 0x08000000;
    static final int FIELD_IS_MARKED = 0x10000000;
  }
}

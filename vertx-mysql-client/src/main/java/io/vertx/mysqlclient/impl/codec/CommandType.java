package io.vertx.mysqlclient.impl.codec;

final class CommandType {
  /*
    https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_field_list.html
   */
  static final byte COM_QUIT = 0x01;
  static final byte COM_INIT_DB = 0x02;
  static final byte COM_QUERY = 0x03;
  static final byte COM_STATISTICS = 0x09;
  static final byte COM_DEBUG = 0x0D;
  static final byte COM_PING = 0x0E;
  static final byte COM_CHANGE_USER = 0x11;
  static final byte COM_RESET_CONNECTION = 0x1F;
  static final byte COM_SET_OPTION = 0x1B;

  // Prepared Statements
  static final byte COM_STMT_PREPARE = 0x16;
  static final byte COM_STMT_EXECUTE = 0x17;
  static final byte COM_STMT_FETCH = 0x1C;
  static final byte COM_STMT_CLOSE = 0x19;
  static final byte COM_STMT_RESET = 0x1A;
  static final byte COM_STMT_SEND_LONG_DATA = 0x18;

  /*
    Deprecated commands
   */
  @Deprecated
  static final byte COM_FIELD_LIST = 0x04;
  @Deprecated
  static final byte COM_REFRESH = 0x07;
  @Deprecated
  static final byte COM_PROCESS_INFO = 0x0A;
  @Deprecated
  static final byte COM_PROCESS_KILL = 0x0C;
}

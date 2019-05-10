package io.vertx.myclient.impl.protocol;

public final class CommandType {
  /*
    https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_field_list.html
   */
  public static final byte COM_QUIT = 0x01;
  public static final byte COM_INIT_DB = 0x02;
  public static final byte COM_QUERY = 0x03;
  public static final byte COM_STATISTICS = 0x08;
  public static final byte COM_DEBUG = 0x0D;
  public static final byte COM_PING = 0x0E;
  public static final byte COM_CHANGE_USER = 0x11;
  public static final byte COM_RESET_CONNECTION = 0x1F;
  public static final byte COM_SET_OPTION = 0x1A;

  // Prepared Statements
  public static final byte COM_STMT_PREPARE = 0x16;
  public static final byte COM_STMT_EXECUTE = 0x17;
  public static final byte COM_STMT_FETCH = 0x1C;
  public static final byte COM_STMT_CLOSE = 0x19;
  public static final byte COM_STMT_RESET = 0x1A;
  public static final byte COM_STMT_SEND_LONG_DATA = 0x18;

  /*
    Deprecated commands
   */
  @Deprecated
  public static final byte COM_FIELD_LIST = 0x04;
  @Deprecated
  public static final byte COM_REFRESH = 0x07;
  @Deprecated
  public static final byte COM_PROCESS_INFO = 0x0A;
  @Deprecated
  public static final byte COM_PROCESS_KILL = 0x0C;
}

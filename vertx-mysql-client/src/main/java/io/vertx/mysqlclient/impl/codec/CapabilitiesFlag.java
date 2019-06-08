package io.vertx.mysqlclient.impl.codec;

/**
 * Values for the capabilities flag bitmask used by Client/Server Protocol.
 * More information can be found in <a href="https://dev.mysql.com/doc/internals/en/capability-flags.html">MySQL Internals Manual</a> and
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/8.0.4/group__group__cs__capabilities__flags.html">MySQL Source Code Documentation</a>.
 */
final class CapabilitiesFlag {
  /*
   The capability flags are used by the client and server to indicate which features they support and want to use.
   */
  static final int CLIENT_LONG_PASSWORD = 0x00000001;
  static final int CLIENT_FOUND_ROWS = 0x00000002;
  static final int CLIENT_LONG_FLAG = 0x00000004;
  static final int CLIENT_CONNECT_WITH_DB = 0x00000008;
  static final int CLIENT_NO_SCHEMA = 0x00000010;
  static final int CLIENT_COMPRESS = 0x00000020;
  static final int CLIENT_ODBC = 0x00000040;
  static final int CLIENT_LOCAL_FILES = 0x00000080;
  static final int CLIENT_IGNORE_SPACE = 0x00000100;
  static final int CLIENT_PROTOCOL_41 = 0x00000200;
  static final int CLIENT_INTERACTIVE = 0x00000400;
  static final int CLIENT_SSL = 0x00000800;
  static final int CLIENT_IGNORE_SIGPIPE = 0x00001000;
  static final int CLIENT_TRANSACTIONS = 0x00002000;
  static final int CLIENT_MULTI_STATEMENTS = 0x00010000;
  static final int CLIENT_MULTI_RESULTS = 0x00020000;
  static final int CLIENT_PS_MULTI_RESULTS = 0x00040000;
  static final int CLIENT_PLUGIN_AUTH = 0x00080000;
  static final int CLIENT_CONNECT_ATTRS = 0x00100000;
  static final int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 0x00200000;
  static final int CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS = 0x00400000;
  static final int CLIENT_SESSION_TRACK = 0x00800000;
  static final int CLIENT_DEPRECATE_EOF = 0x01000000;
  static final int CLIENT_OPTIONAL_RESULTSET_METADATA = 0x02000000;
  static final int CLIENT_REMEMBER_OPTIONS = 0x80000000;

  /*
    Deprecated flags
   */
  @Deprecated
  static final int CLIENT_RESERVED = 0x00004000;
  @Deprecated
  // CLIENT_RESERVED2
  static final int CLIENT_SECURE_CONNECTION = 0x00008000;
  @Deprecated
  static final int CLIENT_VERIFY_SERVER_CERT = 0x40000000;
}

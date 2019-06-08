package io.vertx.mysqlclient.impl.codec;

/**
 * MySQL Packets.
 */
final class Packets {
  static final int OK_PACKET_HEADER = 0x00;
  static final int EOF_PACKET_HEADER = 0xFE;
  static final int ERROR_PACKET_HEADER = 0xFF;
  static final int PACKET_PAYLOAD_LENGTH_LIMIT = 0xFFFFFF;

  static final class OkPacket {

    private final long affectedRows;
    private final long lastInsertId;
    private final int serverStatusFlags;
    private final int numberOfWarnings;
    private final String statusInfo;
    private final String sessionStateInfo;

    OkPacket(long affectedRows, long lastInsertId, int serverStatusFlags, int numberOfWarnings, String statusInfo, String sessionStateInfo) {
      this.affectedRows = affectedRows;
      this.lastInsertId = lastInsertId;
      this.serverStatusFlags = serverStatusFlags;
      this.numberOfWarnings = numberOfWarnings;
      this.statusInfo = statusInfo;
      this.sessionStateInfo = sessionStateInfo;
    }

    long affectedRows() {
      return affectedRows;
    }

    long lastInsertId() {
      return lastInsertId;
    }

    int serverStatusFlags() {
      return serverStatusFlags;
    }

    int numberOfWarnings() {
      return numberOfWarnings;
    }

    String statusInfo() {
      return statusInfo;
    }

    String sessionStateInfo() {
      return sessionStateInfo;
    }
  }

  static final class EofPacket {

    private final int numberOfWarnings;
    private final int serverStatusFlags;

    EofPacket(int numberOfWarnings, int serverStatusFlags) {
      this.numberOfWarnings = numberOfWarnings;
      this.serverStatusFlags = serverStatusFlags;
    }

    int numberOfWarnings() {
      return numberOfWarnings;
    }

    int serverStatusFlags() {
      return serverStatusFlags;
    }
  }

  static final class ServerStatusFlags {
    /*
      https://dev.mysql.com/doc/dev/mysql-server/latest/mysql__com_8h.html#a1d854e841086925be1883e4d7b4e8cad
     */

    static final int SERVER_STATUS_IN_TRANS = 0x0001;
    static final int SERVER_STATUS_AUTOCOMMIT = 0x0002;
    static final int SERVER_MORE_RESULTS_EXISTS = 0x0008;
    static final int SERVER_STATUS_NO_GOOD_INDEX_USED = 0x0010;
    static final int SERVER_STATUS_NO_INDEX_USED = 0x0020;
    static final int SERVER_STATUS_CURSOR_EXISTS = 0x0040;
    static final int SERVER_STATUS_LAST_ROW_SENT = 0x0080;
    static final int SERVER_STATUS_DB_DROPPED = 0x0100;
    static final int SERVER_STATUS_NO_BACKSLASH_ESCAPES = 0x0200;
    static final int SERVER_STATUS_METADATA_CHANGED = 0x0400;
    static final int SERVER_QUERY_WAS_SLOW = 0x0800;
    static final int SERVER_PS_OUT_PARAMS = 0x1000;
    static final int SERVER_STATUS_IN_TRANS_READONLY = 0x2000;
    static final int SERVER_SESSION_STATE_CHANGED = 0x4000;
  }
}

package io.vertx.mysqlclient.impl.protocol.backend;

public final class OkPacket {
  public static final int OK_PACKET_HEADER = 0x00;

  private final long affectedRows;
  private final long lastInsertId;
  private final int serverStatusFlags;
  private final int numberOfWarnings;
  private final String statusInfo;
  private final String sessionStateInfo;

  public OkPacket(long affectedRows, long lastInsertId, int serverStatusFlags, int numberOfWarnings, String statusInfo, String sessionStateInfo) {
    this.affectedRows = affectedRows;
    this.lastInsertId = lastInsertId;
    this.serverStatusFlags = serverStatusFlags;
    this.numberOfWarnings = numberOfWarnings;
    this.statusInfo = statusInfo;
    this.sessionStateInfo = sessionStateInfo;
  }

  public long affectedRows() {
    return affectedRows;
  }

  public long lastInsertId() {
    return lastInsertId;
  }

  public int serverStatusFlags() {
    return serverStatusFlags;
  }

  public int numberOfWarnings() {
    return numberOfWarnings;
  }

  public String statusInfo() {
    return statusInfo;
  }

  public String sessionStateInfo() {
    return sessionStateInfo;
  }
}

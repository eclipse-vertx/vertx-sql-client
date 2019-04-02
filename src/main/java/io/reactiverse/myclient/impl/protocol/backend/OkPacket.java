package io.reactiverse.myclient.impl.protocol.backend;

public final class OkPacket {
  public static final int OK_PACKET_HEADER = 0x00;

  private final long affectedRows;
  private final long lastInsertId;
  private final int serverStatusFlags;
  private final int numberOfWarnings;
  private final String info;
  //TODO Session info track support?

  public OkPacket(long affectedRows, long lastInsertId, int serverStatusFlags, int numberOfWarnings, String info) {
    this.affectedRows = affectedRows;
    this.lastInsertId = lastInsertId;
    this.serverStatusFlags = serverStatusFlags;
    this.numberOfWarnings = numberOfWarnings;
    this.info = info;
  }

  public long getAffectedRows() {
    return affectedRows;
  }

  public long getLastInsertId() {
    return lastInsertId;
  }

  public int getServerStatusFlags() {
    return serverStatusFlags;
  }

  public int getNumberOfWarnings() {
    return numberOfWarnings;
  }

  public String getInfo() {
    return info;
  }
}

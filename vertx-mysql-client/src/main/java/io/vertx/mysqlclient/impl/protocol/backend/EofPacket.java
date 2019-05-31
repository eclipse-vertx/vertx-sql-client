package io.vertx.mysqlclient.impl.protocol.backend;

public final class EofPacket {
  public static final int EOF_PACKET_HEADER = 0xFE;

  private final int numberOfWarnings;
  private final int serverStatusFlags;

  public EofPacket(int numberOfWarnings, int serverStatusFlags) {
    this.numberOfWarnings = numberOfWarnings;
    this.serverStatusFlags = serverStatusFlags;
  }

  public int getNumberOfWarnings() {
    return numberOfWarnings;
  }

  public int getServerStatusFlags() {
    return serverStatusFlags;
  }
}

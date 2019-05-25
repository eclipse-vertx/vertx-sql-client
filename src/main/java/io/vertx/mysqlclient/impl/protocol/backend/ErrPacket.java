package io.vertx.mysqlclient.impl.protocol.backend;

public final class ErrPacket {
  public static final int ERROR_PACKET_HEADER = 0xFF;

  private final int errorCode;
  private final String sqlStateMarker;
  private final String sqlState;
  private final String errorMessage;

  public ErrPacket(int errorCode, String sqlStateMarker, String sqlState, String errorMessage) {
    this.errorCode = errorCode;
    this.sqlStateMarker = sqlStateMarker;
    this.sqlState = sqlState;
    this.errorMessage = errorMessage;
  }

  public int errorCode() {
    return errorCode;
  }

  public String sqlStateMarker() {
    return sqlStateMarker;
  }

  public String sqlState() {
    return sqlState;
  }

  public String errorMessage() {
    return errorMessage;
  }
}

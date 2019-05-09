package io.vertx.myclient.impl.protocol.backend;

import io.vertx.pgclient.PgException;

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

  public int getErrorCode() {
    return errorCode;
  }

  public String getSqlStateMarker() {
    return sqlStateMarker;
  }

  public String getSqlState() {
    return sqlState;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public PgException toException() {
    return new PgException(errorMessage, null, "" + errorCode, null);
  }

}

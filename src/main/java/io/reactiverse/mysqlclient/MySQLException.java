package io.reactiverse.mysqlclient;

public class MySQLException extends RuntimeException {
  private final int errorCode;
  private final String errorMessage;

  public MySQLException(int errorCode, String errorMessage) {
    super(errorMessage);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}

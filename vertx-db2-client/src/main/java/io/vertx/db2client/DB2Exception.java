package io.vertx.db2client;

/**
 * A {@link RuntimeException} signals that an error occurred.
 */
public class DB2Exception extends RuntimeException {
  private static final long serialVersionUID = 4249056398546361175L;
  
  private final int errorCode;
  private final String sqlState;

  public DB2Exception(String message, int errorCode, String sqlState) {
    super(message);
    this.errorCode = errorCode;
    this.sqlState = sqlState;
  }

  /**
   * Get the error code in the error message sent from MySQL server.
   *
   * @return the error code
   */
  public int getErrorCode() {
    return errorCode;
  }

  /**
   * Get the SQL state in the error message sent from MySQL server.
   *
   * @return the SQL state
   */
  public String getSqlState() {
    return sqlState;
  }

  /**
   * Get the error message in the error message sent from MySQL server.
   *
   * @return the error message
   */
  @Override
  public String getMessage() {
    return super.getMessage();
  }
}

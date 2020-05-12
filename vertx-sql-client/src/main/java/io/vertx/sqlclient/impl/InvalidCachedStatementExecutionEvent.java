package io.vertx.sqlclient.impl;

/**
 * An event signals when an invalid cached prepared statement is executed and gets an error response.
 */
public class InvalidCachedStatementExecutionEvent {
  private final PreparedStatement preparedStatement;

  public InvalidCachedStatementExecutionEvent(PreparedStatement preparedStatement) {
    this.preparedStatement = preparedStatement;
  }

  public PreparedStatement preparedStatement() {
    return preparedStatement;
  }
}

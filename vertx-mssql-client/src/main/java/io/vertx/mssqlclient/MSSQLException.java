package io.vertx.mssqlclient;

/**
 * A {@link RuntimeException} signals that an error occurred.
 */
public class MSSQLException extends RuntimeException {
  private final int number;
  private final byte state;
  private final byte severity;
  private final String serverName;
  private final String procedureName;
  private final int lineNumber;

  public MSSQLException(int number, byte state, byte severity, String message, String serverName, String procedureName, int lineNumber) {
    super(message);
    this.number = number;
    this.state = state;
    this.severity = severity;
    this.serverName = serverName;
    this.procedureName = procedureName;
    this.lineNumber = lineNumber;
  }

  public int number() {
    return number;
  }

  public byte state() {
    return state;
  }

  public byte severity() {
    return severity;
  }

  public String serverName() {
    return serverName;
  }

  public String procedureName() {
    return procedureName;
  }

  public int lineNumber() {
    return lineNumber;
  }
}

package io.vertx.clickhouse.clickhousenative.impl;

public class ClickhouseServerException extends RuntimeException {
  private final int code;
  private final String name;
  private final String message;
  private final String stacktrace;

  private ClickhouseServerException(Integer code, String name, String message, String stacktrace, ClickhouseServerException cause, boolean unused) {
    super(message, cause, false, true);
    this.code = code;
    this.name = name;
    this.message = message;
    this.stacktrace = stacktrace;
  }

  private ClickhouseServerException(Integer code, String name, String message, String stacktrace, ClickhouseServerException cause) {
    super(message, cause, false, false);
    this.code = code;
    this.name = name;
    this.message = message;
    this.stacktrace = stacktrace;
  }

  public static ClickhouseServerException build(Integer code, String name, String message, String stacktrace, ClickhouseServerException cause, boolean first) {
    if (first) {
      return new ClickhouseServerException(code, name, message, stacktrace, cause, first);
    }
    return new ClickhouseServerException(code, name, message, stacktrace, cause);
  }

  public int getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public String getServerStacktrace() {
    return stacktrace;
  }
}

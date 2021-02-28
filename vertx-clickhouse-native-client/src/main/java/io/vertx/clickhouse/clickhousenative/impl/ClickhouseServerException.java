package io.vertx.clickhouse.clickhousenative.impl;

public class ClickhouseServerException extends RuntimeException {
  private final int code;
  private final String name;
  private final String message;
  private final String stacktrace;

  public ClickhouseServerException(Integer code, String name, String message, String stacktrace, boolean hasNested) {
    super(message);
    this.code = code;
    this.name = name;
    this.message = message;
    this.stacktrace = stacktrace;
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

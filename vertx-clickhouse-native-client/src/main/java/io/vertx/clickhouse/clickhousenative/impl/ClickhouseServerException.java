package io.vertx.clickhouse.clickhousenative.impl;

public class ClickhouseServerException extends RuntimeException {
  private final Integer code;
  private final String name;
  private final String message;
  private final String stacktrace;

  public ClickhouseServerException(Integer code, String name, String message, String stacktrace) {
    super(message);
    this.code = code;
    this.name = name;
    this.message = message;
    this.stacktrace = stacktrace;
  }

  public Integer getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public String getStacktrace() {
    return stacktrace;
  }
}

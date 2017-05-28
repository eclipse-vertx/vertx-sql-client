package com.julienviet.pgclient;

import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgException extends RuntimeException {

  private final ErrorResponse error;

  public PgException(ErrorResponse error) {
    super(error.getMessage());
    this.error = error;
  }

  public String getSeverity() {
    return error.getSeverity();
  }

  public String getCode() {
    return error.getCode();
  }
}

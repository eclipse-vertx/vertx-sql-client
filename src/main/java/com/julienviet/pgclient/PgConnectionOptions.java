package com.julienviet.pgclient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionOptions {

  public static final int DEFAULT_PIPELINING_LIMIT = 1;

  private int pipeliningLimit = DEFAULT_PIPELINING_LIMIT;

  public PgConnectionOptions() {
  }

  public PgConnectionOptions(PgConnectionOptions other) {
    pipeliningLimit = other.pipeliningLimit;
  }

  public int getPipeliningLimit() {
    return pipeliningLimit;
  }

  public PgConnectionOptions setPipeliningLimit(int pipeliningLimit) {
    if (pipeliningLimit < 1) {
      throw new IllegalArgumentException();
    }
    this.pipeliningLimit = pipeliningLimit;
    return this;
  }

}

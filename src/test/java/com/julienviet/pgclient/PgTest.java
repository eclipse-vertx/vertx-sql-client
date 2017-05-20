package com.julienviet.pgclient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgTest extends PgTestBase {

  public PgTest() {
    super(PostgresClient::connect);
  }
}

package com.julienviet.pgclient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionTest extends PgConnectionTestBase {

  public PgConnectionTest() {
    super(PgClient::connect);
  }
}

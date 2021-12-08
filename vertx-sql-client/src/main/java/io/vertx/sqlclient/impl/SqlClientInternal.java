package io.vertx.sqlclient.impl;

import io.vertx.core.Handler;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.spi.Driver;

public interface SqlClientInternal extends SqlClient {

  /**
   * @return the client driver
   */
  Driver driver();

  /**
   * Experimental API not yet exposed.
   *
   * <p> Execute the code {@code block} with a client that defers the flush of queries after its execution.
   *
   * @param block the block to execute
   */
  void group(Handler<SqlClient> block);

}

package io.vertx.sqlclient.impl;

import io.vertx.core.Handler;
import io.vertx.sqlclient.SqlClient;

public interface SqlClientInternal extends SqlClient {

  /**
   * Append a parameter placeholder in the {@code query}.
   *
   * <p>The index starts at {@code 0}.
   *
   * <ul>
   *   <li>When {@code index == current} indicates it is a new parameter and therefore the same
   *    * value should be returned.</li>
   *   <li>When {@code index < current} indicates the builder wants to reuse a parameter.
   *   The implementation can either return the same value to indicate the parameter can be reused or
   *   return the next index to use (which is shall be the {@code current} value</li>
   * </ul>
   *
   * @param queryBuilder the builder to append to
   * @param index the parameter placeholder index
   * @return the index at which the parameter placeholder could be added
   */
  int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current);

  /**
   * Experimental API not yet exposed.
   *
   * <p> Execute the code {@code block} with a client that defers the flush of queries after its execution.
   *
   * @param block the block to execute
   */
  void group(Handler<SqlClient> block);

}

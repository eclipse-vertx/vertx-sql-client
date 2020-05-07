package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.SqlClient;

public interface SqlClientInternal extends SqlClient {

  /**
   * Append a query placeholder in the {@code query}.
   *
   * <p>The index starts at {@code 0}
   *
   * @param queryBuilder the builder to append to
   * @param index the placeholder index
   */
  int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current);

}

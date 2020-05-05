package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.SqlClient;

public interface SqlClientInternal extends SqlClient {

  /**
   * Append a query place holder in the {@code query}.
   *
   * <p>The index starts at {@code 0}
   *
   * @param queryBuilder the builder to append to
   * @param index the place holder index
   */
  int appendQueryPlaceHolder(StringBuilder queryBuilder, int index, int current);

}

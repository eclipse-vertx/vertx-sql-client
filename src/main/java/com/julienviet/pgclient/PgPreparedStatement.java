package com.julienviet.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A postgres prepared statement.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PgPreparedStatement {

  /**
   * @return create a query from this statement with no arguments
   */
  default PgQuery query() {
    return query(Collections.emptyList());
  }

  /**
   * @param param1 the first argument of the query
   * @return create a query from this statement with one argument
   */
  default PgQuery query(Object param1) {
    return query(Collections.singletonList(param1));
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @return create a query from this statement with two arguments
   */
  default PgQuery query(Object param1, Object param2) {
    return query(Arrays.asList(param1, param2));
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   * @return create a query from this statement with three arguments
   */
  default PgQuery query(Object param1, Object param2, Object param3) {
    return query(Arrays.asList(param1, param2, param3));
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   * @param param4 the fourth argument of the query
   * @return create a query from this statement with four arguments
   */
  default PgQuery query(Object param1, Object param2, Object param3, Object param4) {
    return query(Arrays.asList(param1, param2, param3, param4));
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   * @param param4 the fourth argument of the query
   * @param param5 the fifth argument of the query
   * @return create a query from this statement with five arguments
   */
  default PgQuery query(Object param1, Object param2, Object param3, Object param4, Object param5) {
    return query(Arrays.asList(param1, param2, param3, param4, param5));
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   * @param param4 the fourth argument of the query
   * @param param5 the fifth argument of the query
   * @param param6 the sixth argument of the query
   * @return create a query from this statement with six arguments
   */
  default PgQuery query(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
    return query(Arrays.asList(param1, param2, param3, param4, param5, param6));
  }

  /**
   * @param params the list of arguments
   * @return create a query from this statement with a variable list of arguments
   */
  PgQuery query(List<Object> params);

  /**
   * Create a new batch.
   *
   * @return the batch
   */
  PgBatch batch();

  /**
   * Close the prepared statement and release its resources.
   */
  void close();

  /**
   * Like {@link #close()} but notifies the {@code completionHandler} when it's closed.
   */
  void close(Handler<AsyncResult<Void>> completionHandler);

}

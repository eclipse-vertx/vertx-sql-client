package com.julienviet.pgclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * The binding of a {@link PgPreparedStatement} and a set of parameters.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgQuery {

  /**
   * Set the query result fetch size.
   * <p/>
   * The default fetch size is {@code 0} which means that all rows will be fetched at once.
   * <p/>
   *
   * @param size how many items should be fetched at once
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgQuery fetch(int size);

  /**
   * Executes the query, the {@code handler} will be called when the result set is available or
   * if an error occurs.
   * <p/>
   * Setting a fetch size positive value {@code N } asks Postgres to send at most {@code N} rows.
   * When the query is executed the returned result set {@link PgResultSet#isComplete()} tells wether
   * there is remaining data to fetch or not.
   * <p/>
   * Additional unfetched data shall be fetch using extra calls {@code execute} until the result set is complete.
   * <p/>
   * A query can be executed multiple times but not concurrently, i.e it is safe to execute a query
   * when the result set has been provided and there is no pending cursor associated with the query.
   *
   * @param handler the handler to call back
   */
  void execute(Handler<AsyncResult<PgResultSet>> handler);

  /**
   * When the query is managing a cursor, the corresponding resources is released. You don't need
   * to close the query when the query was executed.
   */
  void close();

  void close(Handler<AsyncResult<Void>> completionHandler);

}

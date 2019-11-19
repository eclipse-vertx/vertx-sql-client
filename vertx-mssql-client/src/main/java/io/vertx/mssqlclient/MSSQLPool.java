package io.vertx.mssqlclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.impl.ContextInternal;
import io.vertx.mssqlclient.impl.MSSQLPoolImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.sqlclient.*;

import java.util.List;
import java.util.stream.Collector;

/**
 * A pool of SQL Server connections.
 */
@VertxGen
public interface MSSQLPool extends Pool {
  /**
   * Create a connection pool to the SQL server configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param connectOptions the options for the connection
   * @param poolOptions the options for creating the pool
   * @return the connection pool
   */
  static MSSQLPool pool(MSSQLConnectOptions connectOptions, PoolOptions poolOptions) {
    if (Vertx.currentContext() != null) {
      throw new IllegalStateException("Running in a Vertx context => use MSSQLPool#pool(Vertx, MSSQLConnectOptions, PoolOptions) instead");
    }
    VertxOptions vertxOptions = new VertxOptions();
    Vertx vertx = Vertx.vertx(vertxOptions);
    return new MSSQLPoolImpl((ContextInternal) vertx.getOrCreateContext(), true, connectOptions, poolOptions);
  }

  /**
   * Like {@link #pool(MSSQLConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static MSSQLPool pool(Vertx vertx, MSSQLConnectOptions connectOptions, PoolOptions poolOptions) {
    return new MSSQLPoolImpl((ContextInternal) vertx.getOrCreateContext(), false, connectOptions, poolOptions);
  }

  @Override
  MSSQLPool preparedQuery(String s, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> MSSQLPool preparedQuery(String s, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  MSSQLPool query(String s, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> MSSQLPool query(String s, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  MSSQLPool preparedQuery(String s, Tuple tuple, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> MSSQLPool preparedQuery(String s, Tuple tuple, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  MSSQLPool preparedBatch(String s, List<Tuple> list, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> MSSQLPool preparedBatch(String s, List<Tuple> list, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);
}

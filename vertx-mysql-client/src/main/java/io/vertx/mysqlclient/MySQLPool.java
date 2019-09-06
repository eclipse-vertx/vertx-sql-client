package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.mysqlclient.impl.MySQLPoolImpl;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.stream.Collector;

import static io.vertx.mysqlclient.MySQLConnectOptions.*;

/**
 * A pool of MySQL connections.
 */
@VertxGen
public interface MySQLPool extends Pool {

  /**
   * Like {@link #pool(String, PoolOptions)} with a default {@code poolOptions}.
   */
  static MySQLPool pool(String connectionUri) {
    return pool(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #pool(MySQLConnectOptions, PoolOptions)} with {@code connectOptions} build from {@code connectionUri}.
   */
  static MySQLPool pool(String connectionUri, PoolOptions poolOptions) {
    return pool(fromUri(connectionUri), poolOptions);
  }

  /**
   * Like {@link #pool(Vertx, String,PoolOptions)} with a default {@code poolOptions}..
   */
  static MySQLPool pool(Vertx vertx, String connectionUri) {
    return pool(vertx, fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #pool(Vertx, MySQLConnectOptions, PoolOptions)} with {@code connectOptions} build from {@code connectionUri}.
   */
  static MySQLPool pool(Vertx vertx, String connectionUri, PoolOptions poolOptions) {
    return pool(vertx, fromUri(connectionUri), poolOptions);
  }

  /**
   * Create a connection pool to the MySQL server configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param connectOptions the options for the connection
   * @param poolOptions the options for creating the pool
   * @return the connection pool
   */
  static MySQLPool pool(MySQLConnectOptions connectOptions, PoolOptions poolOptions) {
    if (Vertx.currentContext() != null) {
      throw new IllegalStateException("Running in a Vertx context => use MySQLPool#pool(Vertx, MySQLConnectOptions, PoolOptions) instead");
    }
    VertxOptions vertxOptions = new VertxOptions();
    Vertx vertx = Vertx.vertx(vertxOptions);
    return new MySQLPoolImpl(vertx.getOrCreateContext(), true, connectOptions, poolOptions);
  }

  /**
   * Like {@link #pool(MySQLConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static MySQLPool pool(Vertx vertx, MySQLConnectOptions connectOptions, PoolOptions poolOptions) {
    return new MySQLPoolImpl(vertx.getOrCreateContext(), false, connectOptions, poolOptions);
  }

  @Override
  MySQLPool preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> MySQLPool preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  MySQLPool query(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> MySQLPool query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  MySQLPool preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> MySQLPool preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  MySQLPool preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> MySQLPool preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);
}

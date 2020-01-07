package io.vertx.db2client;

import java.util.List;
import java.util.stream.Collector;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.db2client.impl.DB2PoolImpl;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;

/**
 * A pool of DB2 connections.
 */
@VertxGen
public interface DB2Pool extends Pool {

  /**
   * Like {@link #pool(String, PoolOptions)} with a default {@code poolOptions}.
   */
  static DB2Pool pool(String connectionUri) {
    return pool(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #pool(DB2ConnectOptions, PoolOptions)} with {@code connectOptions} build from {@code connectionUri}.
   */
  static DB2Pool pool(String connectionUri, PoolOptions poolOptions) {
    return pool(new DB2ConnectOptions(connectionUri), poolOptions);
  }

  /**
   * Like {@link #pool(Vertx, String,PoolOptions)} with a default {@code poolOptions}..
   */
  static DB2Pool pool(Vertx vertx, String connectionUri) {
    return pool(vertx, new DB2ConnectOptions(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #pool(Vertx, DB2ConnectOptions, PoolOptions)} with {@code connectOptions} build from {@code connectionUri}.
   */
  static DB2Pool pool(Vertx vertx, String connectionUri, PoolOptions poolOptions) {
    return pool(vertx, new DB2ConnectOptions(connectionUri), poolOptions);
  }

  /**
   * Create a connection pool to the DB2 server configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param connectOptions the options for the connection
   * @param poolOptions the options for creating the pool
   * @return the connection pool
   */
  static DB2Pool pool(DB2ConnectOptions connectOptions, PoolOptions poolOptions) {
    if (Vertx.currentContext() != null) {
      throw new IllegalStateException("Running in a Vertx context => use MySQLPool#pool(Vertx, MySQLConnectOptions, PoolOptions) instead");
    }
    VertxOptions vertxOptions = new VertxOptions();
    vertxOptions.setBlockedThreadCheckInterval(1000 * 60 * 60); // TODO @AGG only for debugging purposes
    Vertx vertx = Vertx.vertx(vertxOptions);
    return new DB2PoolImpl(vertx.getOrCreateContext(), true, connectOptions, poolOptions);
  }

  /**
   * Like {@link #pool(DB2ConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static DB2Pool pool(Vertx vertx, DB2ConnectOptions connectOptions, PoolOptions poolOptions) {
    return new DB2PoolImpl(vertx.getOrCreateContext(), false, connectOptions, poolOptions);
  }

  @Override
  DB2Pool preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> DB2Pool preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  DB2Pool query(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> DB2Pool query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  DB2Pool preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> DB2Pool preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  DB2Pool preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> DB2Pool preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);
}

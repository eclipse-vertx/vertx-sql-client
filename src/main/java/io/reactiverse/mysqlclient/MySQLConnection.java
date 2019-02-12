package io.reactiverse.mysqlclient;

import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.stream.Collector;

@OriginAPI
public interface MySQLConnection extends MySQLClient {
  @Override
  MySQLConnection query(String sql, Handler<AsyncResult<PgRowSet>> handler);

  @Override
  <R> MySQLConnection query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  @Override
  MySQLConnection ping(Handler<AsyncResult<Void>> handler);

  @Override
  MySQLConnection preparedQuery(String sql, Handler<AsyncResult<PgRowSet>> handler);

  @Override
  <R> MySQLConnection preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  @Override
  MySQLConnection preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgRowSet>> handler);

  @Override
  <R> MySQLConnection preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  MySQLConnection prepare(String sql, Handler<AsyncResult<MySQLPreparedQuery>> handler);

  MySQLConnection closeHandler(Handler<Void> closeHandler);

  void close();
}

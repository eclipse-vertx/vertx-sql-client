package io.reactiverse.mysqlclient;

import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
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

  MySQLConnection closeHandler(Handler<Void> closeHandler);

  void close();
}

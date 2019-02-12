package io.reactiverse.mysqlclient;

import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.impl.ArrayTuple;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.stream.Collector;

@OriginAPI
public interface MySQLPreparedQuery {
  default MySQLPreparedQuery execute(Handler<AsyncResult<PgRowSet>> handler) {
    return execute(ArrayTuple.EMPTY, handler);
  }

  default <R> MySQLPreparedQuery execute(Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    return execute(ArrayTuple.EMPTY, collector, handler);
  }

  MySQLPreparedQuery execute(Tuple args, Handler<AsyncResult<PgRowSet>> handler);

  <R> MySQLPreparedQuery execute(Tuple args, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  void close();

  void close(Handler<AsyncResult<Void>> completionHandler);
}

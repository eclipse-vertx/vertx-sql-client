package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.MySQLPreparedQuery;
import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.stream.Collector;

public class MySQLPreparedQueryImpl implements MySQLPreparedQuery {
  private MySQLPreparedStatement mySQLPreparedStatement;

  public MySQLPreparedQueryImpl(MySQLPreparedStatement mySQLPreparedStatement) {
    this.mySQLPreparedStatement = mySQLPreparedStatement;
  }

  @Override
  public MySQLPreparedQuery execute(Tuple args, Handler<AsyncResult<PgRowSet>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <R> MySQLPreparedQuery execute(Tuple args, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    throw new UnsupportedOperationException();
  }
}

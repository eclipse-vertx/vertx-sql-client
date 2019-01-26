package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.ImplReusable;
import io.reactiverse.mysqlclient.MySQLClient;
import io.reactiverse.mysqlclient.impl.codec.encoder.QueryCommand;
import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.function.Function;
import java.util.stream.Collector;

@ImplReusable
public abstract class MySQLClientBase<C extends MySQLClient> implements MySQLClient, CommandScheduler {
  @Override
  public C query(String sql, Handler<AsyncResult<PgRowSet>> handler) {
    return query(sql, false, MySQLRowSetImpl.FACTORY, MySQLRowSetImpl.COLLECTOR, handler);
  }

  @Override
  public <R> C query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    return query(sql, true, MySQLResultImpl::new, collector, handler);
  }

  private <R1, R2 extends MySQLResultBase<R1, R2>, R3 extends PgResult<R1>> C query(
    String sql,
    boolean singleton,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Handler<AsyncResult<R3>> handler) {
    MySQLResultBuilder<R1, R2, R3> b = new MySQLResultBuilder<>(factory, handler);
    schedule(new QueryCommand<>(sql, singleton, collector, b), b);
    return (C) this;
  }
}

package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.ImplReusable;
import io.reactiverse.mysqlclient.MySQLClient;
import io.reactiverse.mysqlclient.impl.codec.encoder.PreparedStatementExecuteCommand;
import io.reactiverse.mysqlclient.impl.codec.encoder.PreparedStatementPrepareCommand;
import io.reactiverse.mysqlclient.impl.codec.encoder.QueryCommand;
import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.impl.ArrayTuple;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.List;
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


  @Override
  public C preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgRowSet>> handler) {
    return preparedQuery(sql, arguments, false, MySQLRowSetImpl.FACTORY, MySQLRowSetImpl.COLLECTOR, handler);
  }

  @Override
  public <R> C preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    return preparedQuery(sql, arguments, true, MySQLResultImpl::new, collector, handler);
  }

  private <R1, R2 extends MySQLResultBase<R1, R2>, R3 extends PgResult<R1>> C preparedQuery(
    String sql,
    Tuple arguments,
    boolean singleton,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Handler<AsyncResult<R3>> handler) {
    schedule(new PreparedStatementPrepareCommand(sql), cr -> {
      if (cr.succeeded()) {
        MySQLPreparedStatement ps = cr.result();
        String msg = ps.prepare((List<Object>) arguments);
        if (msg != null) {
          handler.handle(Future.failedFuture(msg));
        } else {
          MySQLResultBuilder<R1, R2, R3> b = new MySQLResultBuilder<>(factory, handler);
          cr.scheduler.schedule(new PreparedStatementExecuteCommand<>(ps, arguments, singleton, collector, b), b);
        }
      } else {
        handler.handle(Future.failedFuture(cr.cause()));
      }
    });
    return (C) this;
  }

  @Override
  public C preparedQuery(String sql, Handler<AsyncResult<PgRowSet>> handler) {
    return preparedQuery(sql, ArrayTuple.EMPTY, handler);
  }

  @Override
  public <R> C preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    return preparedQuery(sql, ArrayTuple.EMPTY, collector, handler);
  }
}

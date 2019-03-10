/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.reactiverse.sqlclient.impl;

import io.reactiverse.sqlclient.impl.command.CommandScheduler;
import io.reactiverse.sqlclient.impl.command.ExtendedBatchQueryCommand;
import io.reactiverse.sqlclient.impl.command.ExtendedQueryCommand;
import io.reactiverse.sqlclient.impl.command.PrepareStatementCommand;
import io.reactiverse.sqlclient.impl.command.SimpleQueryCommand;
import io.reactiverse.sqlclient.SqlResult;
import io.reactiverse.sqlclient.RowSet;
import io.reactiverse.sqlclient.Row;
import io.reactiverse.sqlclient.SqlClient;
import io.reactiverse.sqlclient.Tuple;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

public abstract class SqlClientBase<C extends SqlClient> implements SqlClient, CommandScheduler {

  @Override
  public C query(String sql, Handler<AsyncResult<RowSet>> handler) {
    return query(sql, false, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, handler);
  }

  @Override
  public <R> C query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler) {
    return query(sql, true, SqlResultImpl::new, collector, handler);
  }

  private <R1, R2 extends SqlResultBase<R1, R2>, R3 extends SqlResult<R1>> C query(
    String sql,
    boolean singleton,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Handler<AsyncResult<R3>> handler) {
    SqlResultBuilder<R1, R2, R3> b = new SqlResultBuilder<>(factory, handler);
    schedule(new SimpleQueryCommand<>(sql, singleton, collector, b), b);
    return (C) this;
  }

  @Override
  public C preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet>> handler) {
    return preparedQuery(sql, arguments, false, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, handler);
  }

  @Override
  public <R> C preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler) {
    return preparedQuery(sql, arguments, true, SqlResultImpl::new, collector, handler);
  }

  private <R1, R2 extends SqlResultBase<R1, R2>, R3 extends SqlResult<R1>> C preparedQuery(
    String sql,
    Tuple arguments,
    boolean singleton,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Handler<AsyncResult<R3>> handler) {
    schedule(new PrepareStatementCommand(sql), cr -> {
      if (cr.succeeded()) {
        PreparedStatement ps = cr.result();
        String msg = ps.prepare((List<Object>) arguments);
        if (msg != null) {
          handler.handle(Future.failedFuture(msg));
        } else {
          SqlResultBuilder<R1, R2, R3> b = new SqlResultBuilder<>(factory, handler);
          cr.scheduler.schedule(new ExtendedQueryCommand<>(ps, arguments, singleton, collector, b), b);
        }
      } else {
        handler.handle(Future.failedFuture(cr.cause()));
      }
    });
    return (C) this;
  }

  @Override
  public C preparedQuery(String sql, Handler<AsyncResult<RowSet>> handler) {
    return preparedQuery(sql, ArrayTuple.EMPTY, handler);
  }

  @Override
  public <R> C preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler) {
    return preparedQuery(sql, ArrayTuple.EMPTY, collector, handler);
  }

  @Override
  public C preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet>> handler) {
    return preparedBatch(sql, batch, false, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, handler);
  }

  @Override
  public <R> C preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler) {
    return preparedBatch(sql, batch, true, SqlResultImpl::new, collector, handler);
  }

  private <R1, R2 extends SqlResultBase<R1, R2>, R3 extends SqlResult<R1>> C preparedBatch(
    String sql,
    List<Tuple> batch,
    boolean singleton,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Handler<AsyncResult<R3>> handler) {
    schedule(new PrepareStatementCommand(sql), cr -> {
      if (cr.succeeded()) {
        PreparedStatement ps = cr.result();
        for  (Tuple args : batch) {
          String msg = ps.prepare((List<Object>) args);
          if (msg != null) {
            handler.handle(Future.failedFuture(msg));
            return;
          }
        }
        SqlResultBuilder<R1, R2, R3> b = new SqlResultBuilder<>(factory, handler);
        cr.scheduler.schedule(new ExtendedBatchQueryCommand<>(
          ps,
          batch,
          singleton,
          collector,
          b), b);
      } else {
        handler.handle(Future.failedFuture(cr.cause()));
      }
    });
    return (C) this;
  }
}

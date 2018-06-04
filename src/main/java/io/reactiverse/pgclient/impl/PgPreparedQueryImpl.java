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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PgPreparedQueryImpl implements PgPreparedQuery {

  private final Connection conn;
  private final PreparedStatement ps;
  private final AtomicBoolean closed = new AtomicBoolean();

  PgPreparedQueryImpl(Connection conn, PreparedStatement ps) {
    this.conn = conn;
    this.ps = ps;
  }

  @Override
  public PgPreparedQuery execute(Tuple args, Handler<AsyncResult<PgRowSet>> handler) {
    return execute(args, PgRowSetImpl.FACTORY, PgRowSetImpl.COLLECTOR, handler);
  }

  @Override
  public <R> PgPreparedQuery execute(Tuple args, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    PgResultBuilder<R, PgResultImpl<R>, PgResult<R>> b = new PgResultBuilder<>(PgResultImpl::new, handler);
    return execute(args, PgResultImpl::new, collector, handler);
  }

  private <R1, R2 extends PgResultBase<R1, R2>, R3 extends PgResult<R1>> PgPreparedQuery execute(
    Tuple args,
    Function<R1, R2> factory, Collector<Row, ?, R1> collector,
    Handler<AsyncResult<R3>> handler) {
    PgResultBuilder<R1, R2, R3> b = new PgResultBuilder<>(factory, handler);
    return execute(args, 0, null, false, collector, b, b);
  }

  <A, R> PgPreparedQuery execute(Tuple args,
                                 int fetch,
                                 String portal,
                                 boolean suspended,
                                 Collector<Row, A, R> collector,
                                 QueryResultHandler<R> resultHandler,
                                 Handler<AsyncResult<Boolean>> handler) {
    String msg = ps.prepare((List<Object>) args);
    if (msg != null) {
      throw new IllegalArgumentException(msg);
    }
    conn.schedule(new ExtendedQueryCommand<>(
      ps,
      args,
      fetch,
      portal,
      suspended,
      collector,
      resultHandler,
      handler));
    return this;
  }

  @Override
  public PgCursor cursor(Tuple args) {
    String msg = ps.prepare((List<Object>) args);
    if (msg != null) {
      throw new IllegalArgumentException(msg);
    }
    return new PgCursorImpl(this, args);
  }

  @Override
  public void close() {
    close(ar -> {
    });
  }

  public PgPreparedQuery batch(List<Tuple> argsList, Handler<AsyncResult<PgRowSet>> handler) {
    return batch(argsList, PgRowSetImpl.FACTORY, PgRowSetImpl.COLLECTOR, handler);
  }

  @Override
  public <R> PgPreparedQuery batch(List<Tuple> argsList, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    return batch(argsList, PgResultImpl::new, collector, handler);
  }

  private <R1, R2 extends PgResultBase<R1, R2>, R3 extends PgResult<R1>> PgPreparedQuery batch(List<Tuple> argsList, Function<R1, R2> factory, Collector<Row, ?, R1> collector, Handler<AsyncResult<R3>> handler) {
    for  (Tuple args : argsList) {
      String msg = ps.prepare((List<Object>) args);
      if (msg != null) {
        throw new IllegalArgumentException(msg);
      }
    }
    PgResultBuilder<R1, R2, R3> b = new PgResultBuilder<>(factory, handler);
    conn.schedule(new ExtendedBatchQueryCommand<>(ps, argsList.iterator(), collector, b, b));
    return this;
  }

  @Override
  public PgStream<Row> createStream(int fetch, Tuple args) {
    return new PgCursorStreamImpl(this, fetch, args);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    if (closed.compareAndSet(false, true)) {
      conn.schedule(new CloseStatementCommand(completionHandler));
    } else {
      completionHandler.handle(Future.failedFuture("Already closed"));
    }
  }

  void closePortal(String portal, Handler<AsyncResult<Void>> handler) {
    conn.schedule(new ClosePortalCommand(portal, handler));
  }

}

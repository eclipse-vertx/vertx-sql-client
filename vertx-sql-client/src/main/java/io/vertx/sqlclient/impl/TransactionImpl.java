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
package io.vertx.sqlclient.impl;

import io.vertx.core.impl.ContextInternal;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.QueryCommandBase;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;
import io.vertx.sqlclient.RowSet;
import io.vertx.core.*;

import java.util.ArrayDeque;
import java.util.Deque;

public class TransactionImpl extends SqlConnectionBase<TransactionImpl> implements Transaction {

  private static final int ST_BEGIN = 0;
  private static final int ST_PENDING = 1;
  private static final int ST_PROCESSING = 2;
  private static final int ST_COMPLETED = 3;

  private final Handler<Void> disposeHandler;
  private Deque<ScheduledCommand<?>> pending = new ArrayDeque<>();
  private Handler<Void> failedHandler;
  private int status = ST_BEGIN;

  public TransactionImpl(ContextInternal context, Connection conn, Handler<Void> disposeHandler) {
    super(context, conn);
    this.disposeHandler = disposeHandler;
    ScheduledCommand<Boolean> b = doQuery("BEGIN", this::afterBegin);
    doSchedule(b.cmd, b.handler);
  }

  static class ScheduledCommand<R> {
    final CommandBase<R> cmd;
    final Handler<AsyncResult<R>> handler;
    ScheduledCommand(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
      this.cmd = cmd;
      this.handler = handler;
    }
  }

  private <R> void doSchedule(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
    conn.schedule(cmd, context.promise(handler));
  }

  private synchronized void afterBegin(AsyncResult<?> ar) {
    if (ar.succeeded()) {
      status = ST_PENDING;
    } else {
      status = ST_COMPLETED;
    }
    checkPending();
  }

  private boolean isComplete(CommandBase<?> cmd) {
    if (cmd instanceof QueryCommandBase<?>) {
      String sql = ((QueryCommandBase) cmd).sql().trim();
      return sql.equalsIgnoreCase("COMMIT") || sql.equalsIgnoreCase("ROLLBACK");
    }
    return false;
  }

  private synchronized void checkPending() {
    switch (status) {
      case ST_BEGIN:
        break;
      case ST_PENDING: {
        ScheduledCommand<?> cmd = pending.poll();
        if (cmd != null) {
          Handler h = cmd.handler;
          if (isComplete(cmd.cmd)) {
            status = ST_COMPLETED;
          } else {
            h = wrap(h);
            status = ST_PROCESSING;
          }
          doSchedule(cmd.cmd, h);
        }
        break;
      }
      case ST_PROCESSING:
        break;
      case ST_COMPLETED: {
        if (pending.size() > 0) {
          VertxException err = new VertxException("Transaction already completed");
          ScheduledCommand<?> cmd;
          while ((cmd = pending.poll()) != null) {
            cmd.cmd.fail(err);
          }
        }
        break;
      }
    }
  }

  @Override
  public <R> void schedule(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
    schedule__(cmd, handler);
  }

  public <R> void schedule__(ScheduledCommand<R> b) {
    synchronized (this) {
      pending.add(b);
    }
    checkPending();
  }

  public <R> void schedule__(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
    schedule__(new ScheduledCommand<>(cmd, handler));
  }

  private <T> Handler<AsyncResult<T>> wrap(Handler<AsyncResult<T>> handler) {
    return ar -> {
      synchronized (TransactionImpl.this) {
        status = ST_PENDING;
        if (ar.failed()) {
          // We won't recover from this so rollback
          ScheduledCommand<?> c;
          while ((c = pending.poll()) != null) {
            c.handler.handle(Future.failedFuture("Rollback exception"));
          }
          Handler<Void> h = failedHandler;
          if (h != null) {
            context.runOnContext(h);
          }
          schedule__(doQuery("ROLLBACK", ar2 -> {
            disposeHandler.handle(null);
            handler.handle(ar);
          }));
        } else {
          handler.handle(ar);
          checkPending();
        }
      }
    };
  }

  @Override
  public void commit() {
    commit(null);
  }

  public void commit(Handler<AsyncResult<Void>> handler) {
    switch (status) {
      case ST_BEGIN:
      case ST_PENDING:
      case ST_PROCESSING:
        schedule__(doQuery("COMMIT", ar -> {
          disposeHandler.handle(null);
          if (handler != null) {
            if (ar.succeeded()) {
              handler.handle(Future.succeededFuture());
            } else {
              handler.handle(Future.failedFuture(ar.cause()));
            }
          }
        }));
        break;
      case ST_COMPLETED:
        if (handler != null) {
          handler.handle(Future.failedFuture("Transaction already completed"));
        }
        break;
    }
  }

  @Override
  public void rollback() {
    rollback(null);
  }

  public void rollback(Handler<AsyncResult<Void>> handler) {
    schedule__(doQuery("ROLLBACK", ar -> {
      disposeHandler.handle(null);
      if (handler != null) {
        handler.handle(ar.mapEmpty());
      }
    }));
  }

  @Override
  public void close() {
    rollback();
  }

  @Override
  public io.vertx.sqlclient.Transaction abortHandler(Handler<Void> handler) {
    failedHandler = handler;
    return this;
  }

  private ScheduledCommand<Boolean> doQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler) {
    SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> b = new SqlResultBuilder<>(RowSetImpl.FACTORY, handler);
    SimpleQueryCommand<RowSet<Row>> cmd = new SimpleQueryCommand<>(sql, false, RowSetImpl.COLLECTOR, b);
    return new ScheduledCommand<>(cmd, b);
  }
}

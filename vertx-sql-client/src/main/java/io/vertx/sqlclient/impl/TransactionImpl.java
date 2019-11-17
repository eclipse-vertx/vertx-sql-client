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
  private Deque<CommandBase<?>> pending = new ArrayDeque<>();
  private Handler<Void> failedHandler;
  private int status = ST_BEGIN;

  public TransactionImpl(Context context, Connection conn, Handler<Void> disposeHandler) {
    super(context, conn);
    this.disposeHandler = disposeHandler;
    doSchedule(doQuery("BEGIN", this::afterBegin));
  }

  private void doSchedule(CommandBase<?> cmd) {
    if (context == Vertx.currentContext()) {
      conn.schedule(cmd);
    } else {
      context.runOnContext(v -> conn.schedule(cmd));
    }
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
        CommandBase<?> cmd = pending.poll();
        if (cmd != null) {
          if (isComplete(cmd)) {
            status = ST_COMPLETED;
          } else {
            wrap(cmd);
            status = ST_PROCESSING;
          }
          doSchedule(cmd);
        }
        break;
      }
      case ST_PROCESSING:
        break;
      case ST_COMPLETED: {
        if (pending.size() > 0) {
          VertxException err = new VertxException("Transaction already completed");
          CommandBase<?> cmd;
          while ((cmd = pending.poll()) != null) {
            cmd.fail(err);
          }
        }
        break;
      }
    }
  }

  @Override
  public <R> void schedule(CommandBase<R> cmd, Handler<CommandResponse<R>> handler) {
    cmd.handler = cr -> {
      if (cr.toAsyncResult().succeeded()) {
        cr.scheduler = this;
      }
      handler.handle(cr);
    };
    schedule(cmd);
  }

  public void schedule(CommandBase<?> cmd) {
    synchronized (this) {
      pending.add(cmd);
    }
    checkPending();
  }

  private <T> void wrap(CommandBase<T> cmd) {
    Handler<? super CommandResponse<T>> handler = cmd.handler;
    cmd.handler = ar -> {
      synchronized (TransactionImpl.this) {
        status = ST_PENDING;
        if (ar.toAsyncResult().failed()) {
          // We won't recover from this so rollback
          CommandBase<?> c;
          while ((c = pending.poll()) != null) {
            c.fail(new RuntimeException("rollback exception"));
          }
          Handler<Void> h = failedHandler;
          if (h != null) {
            context.runOnContext(h);
          }
          schedule(doQuery("ROLLBACK", ar2 -> {
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
        schedule(doQuery("COMMIT", ar -> {
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
    schedule(doQuery("ROLLBACK", ar -> {
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

  private CommandBase doQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler) {
    SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> b = new SqlResultBuilder<>(RowSetImpl.FACTORY, handler);
    SimpleQueryCommand<RowSet<Row>> cmd = new SimpleQueryCommand<>(sql, false, RowSetImpl.COLLECTOR, b);
    cmd.handler = ar -> b.handle(ar.toAsyncResult());
    return cmd;
  }
}

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
import io.reactiverse.pgclient.impl.codec.TxStatus;
import io.vertx.core.*;
import io.vertx.core.impl.NoStackTraceThrowable;

import java.util.ArrayDeque;
import java.util.Deque;

class Transaction extends PgClientBase<Transaction> implements PgTransaction {

  private static final int ST_BEGIN = 0;
  private static final int ST_PENDING = 1;
  private static final int ST_PROCESSING = 2;
  private static final int ST_COMPLETED = 3;

  private final Context context;
  private final Handler<Void> disposeHandler;
  private Connection conn;
  private Deque<CommandBase<?>> pending = new ArrayDeque<>();
  private Handler<Void> failedHandler;
  private int status = ST_BEGIN;

  Transaction(Context context, Connection conn, Handler<Void> disposeHandler) {
    this.context = context;
    this.disposeHandler = disposeHandler;
    this.conn = conn;
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

  public void schedule(CommandBase<?> cmd) {
    synchronized (this) {
      pending.add(cmd);
    }
    checkPending();
  }

  private <T> void wrap(CommandBase<T> cmd) {
    Handler<? super CommandResponse<T>> handler = cmd.handler;
    cmd.handler = ar -> {
      synchronized (Transaction.this) {
        status = ST_PENDING;
        if (ar.txStatus() == TxStatus.FAILED) {
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
    schedule(doQuery("COMMIT", ar -> {
      disposeHandler.handle(null);
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    }));
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
  public PgTransaction abortHandler(Handler<Void> handler) {
    failedHandler = handler;
    return this;
  }

  private CommandBase doQuery(String sql, Handler<AsyncResult<PgRowSet>> handler) {
    PgResultBuilder<PgRowSet, PgRowSetImpl, PgRowSet> b = new PgResultBuilder<>(PgRowSetImpl.FACTORY, handler);
    return new SimpleQueryCommand<>(sql, false, PgRowSetImpl.COLLECTOR, b, b);
  }
}

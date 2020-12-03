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

import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.QueryCommandBase;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;
import io.vertx.core.*;

import java.util.ArrayDeque;
import java.util.Deque;

public class TransactionImpl extends SqlConnectionBase<TransactionImpl> implements Transaction {

  private static final int ST_BEGIN = 0;
  private static final int ST_PENDING = 1;
  private static final int ST_PROCESSING = 2;
  private static final int ST_COMPLETED = 3;

  private final Handler<Void> disposeHandler;
  private final Deque<CommandBase<?>> pending = new ArrayDeque<>();
  private Handler<Void> abortHandler;
  private int status = ST_BEGIN;

  public TransactionImpl(Context context, Connection conn, Handler<Void> disposeHandler) {
    super(context, conn);
    this.disposeHandler = disposeHandler;
    doSchedule(wrapCommandHandler(createQueryCommand("BEGIN", this::afterBegin)));
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

  private void checkPending() {
    while (true) {
      CommandBase<?> cmd;
      synchronized (this) {
        switch (status) {
          case ST_PENDING:
            cmd = pending.poll();
            if (cmd != null) {
              status = ST_PROCESSING;
              doSchedule(cmd);
            }
            return;
          case ST_COMPLETED:
            cmd = pending.poll();
            if (cmd == null) {
              return;
            }
            break;
          default:
            return;
        }
      }
      VertxException err = new VertxException("Transaction already completed", false);
      cmd.fail(err);
    }
  }

  @Override
  public <R> void schedule(CommandBase<R> cmd, Handler<? super CommandResponse<R>> handler) {
    cmd.handler = handler;
    schedule(cmd);
  }

  public void schedule(CommandBase<?> cmd) {
    wrapCommandHandler(cmd);
    synchronized (this) {
      pending.add(cmd);
    }
    checkPending();
  }

  @Override
  public void commit() {
    commit(null);
  }

  public void commit(Handler<AsyncResult<Void>> handler) {
    synchronized (this) {
      pending.add(createQueryCommand("COMMIT", ar -> {
        tryComplete();
        if (handler != null) {
          handler.handle(ar.mapEmpty());
        }
      }));
    }
    checkPending();
  }

  @Override
  public void rollback() {
    rollback(null);
  }

  public void rollback(Handler<AsyncResult<Void>> completionHandler) {
    synchronized (this) {
      CommandBase<?> cmd = createQueryCommand("ROLLBACK", ar -> {
        if (tryComplete()) {
          Handler<Void> handler;
          synchronized (TransactionImpl.this) {
            handler = abortHandler;
          }
          if (handler != null) {
            handler.handle(null);
          }
        }
        if (completionHandler != null) {
          completionHandler.handle(ar.mapEmpty());
        }
      });
      pending.addFirst(cmd);
    }
    checkPending();
  }

  private boolean tryComplete() {
    synchronized (this) {
      if (status == ST_COMPLETED) {
        return false;
      }
      status = ST_COMPLETED;
    }
    disposeHandler.handle(null);
    checkPending();
    return true;
  }


  @Override
  public void close() {
    rollback();
  }

  @Override
  public synchronized io.vertx.sqlclient.Transaction abortHandler(Handler<Void> handler) {
    abortHandler = handler;
    return this;
  }

  private <T> CommandBase<T> wrapCommandHandler(CommandBase<T> cmd) {
    Handler<? super CommandResponse<T>> handler = cmd.handler;
    cmd.handler = ar -> {
      synchronized (TransactionImpl.this) {
        if (status == ST_PROCESSING) {
          status = ST_PENDING;
        }
      }
      if (ar.toAsyncResult().failed()) {
        rollback(a -> handler.handle(ar));
      } else {
        handler.handle(ar);
        checkPending();
      }
    };
    return cmd;
  }

  private CommandBase<?> createQueryCommand(String sql, Handler<AsyncResult<?>> handler) {
    SimpleQueryCommand<Void> cmd = new SimpleQueryCommand<>(
      sql,
      false,
      autoCommit(),
      QueryCommandBase.NULL_COLLECTOR,
      QueryResultHandler.NOOP_HANDLER);
    cmd.handler = handler;
    return cmd;
  }

  @Override
  boolean autoCommit() {
    return false;
  }
}

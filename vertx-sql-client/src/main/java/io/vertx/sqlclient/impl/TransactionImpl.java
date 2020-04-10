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

import java.util.ArrayDeque;
import java.util.Deque;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.VertxException;
import io.vertx.core.impl.ContextInternal;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.TransactionRollbackException;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.TxCommand;

public class TransactionImpl extends SqlConnectionBase<TransactionImpl> implements Transaction {

  private static final TxCommand<Void> ROLLBACK = new TxCommand<>(TxCommand.Kind.ROLLBACK, null);
  private static final TxCommand<Void> COMMIT = new TxCommand<>(TxCommand.Kind.COMMIT, null);

  private static final int ST_BEGIN = 0;
  private static final int ST_PENDING = 1;
  private static final int ST_PROCESSING = 2;
  private static final int ST_COMPLETED = 3;

  private final Handler<Void> disposeHandler;
  private Deque<ScheduledCommand<?>> pending = new ArrayDeque<>();
  private Handler<Void> failedHandler;
  private int status = ST_BEGIN;
  private final Promise<Void> promise;
  private final Future<Void> future;

  TransactionImpl(ContextInternal context, Connection conn, Handler<Void> disposeHandler) {
    super(context, conn);
    this.disposeHandler = disposeHandler;
    this.promise = context.promise();
    this.future = promise.future();
    ScheduledCommand<Transaction> b = doQuery(new TxCommand<>(TxCommand.Kind.BEGIN, this), context.promise(this::afterBegin));
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

  @Override
  protected <T> Promise<T> promise() {
    return context.promise();
  }

  @Override
  protected <T> Promise<T> promise(Handler<AsyncResult<T>> handler) {
    return context.promise(handler);
  }

  private <R> void doSchedule(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
    conn.schedule(cmd, context.promise(handler));
  }

  private <R> void wrapAndSchedule(ScheduledCommand<R> scheduled) {
    CommandBase<R> cmd = scheduled.cmd;
    if (isComplete(cmd)) {
      status = ST_COMPLETED;
      doSchedule(cmd, ar -> {
        if (ar.succeeded()) {
          if (cmd == COMMIT) {
            promise.tryComplete();
          } else {
            promise.tryFail(TransactionRollbackException.INSTANCE);
          }
        } else {
          promise.tryFail(ar.cause());
        }
        scheduled.handler.handle(ar);
      });
    } else {
      status = ST_PROCESSING;
      doSchedule(cmd, wrap(scheduled.handler));
    }
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
          schedule__(doQuery(ROLLBACK, context.promise(ar2 -> {
            disposeHandler.handle(null);
            handler.handle(ar);
          })));
        } else {
          handler.handle(ar);
          checkPending();
        }
      }
    };
  }

  private synchronized void afterBegin(AsyncResult<Transaction> ar) {
    if (ar.succeeded()) {
      status = ST_PENDING;
    } else {
      status = ST_COMPLETED;
    }
    checkPending();
  }

  private static boolean isComplete(CommandBase<?> cmd) {
    if (cmd instanceof TxCommand) {
      TxCommand txCmd = (TxCommand) cmd;
      return txCmd.kind == TxCommand.Kind.COMMIT || txCmd.kind == TxCommand.Kind.ROLLBACK;
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
          wrapAndSchedule(cmd);
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
  public <R> void schedule(CommandBase<R> cmd, Promise<R> handler) {
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

  @Override
  public Future<Void> commit() {
    switch (status) {
      case ST_BEGIN:
      case ST_PENDING:
      case ST_PROCESSING:
        Promise<Void> promise = context.promise();
        schedule__(doQuery(COMMIT, promise));
        Future<Void> fut = promise.future();
        fut.onComplete(ar -> disposeHandler.handle(null));
        return fut.mapEmpty();
      case ST_COMPLETED:
        return context.failedFuture("Transaction already completed");
      default:
        throw new IllegalStateException();
    }
  }

  public void commit(Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = commit();
    if (handler != null) {
      fut.onComplete(handler);
    }
  }

  @Override
  public Future<Void> rollback() {
    if (status == ST_COMPLETED) {
      return context.failedFuture("Transaction already completed");
    } else {
      Promise<Void> promise = context.promise();
      schedule__(doQuery(ROLLBACK, promise));
      Future<Void> fut = promise.future().mapEmpty();
      fut.onComplete(ar -> disposeHandler.handle(null));
      return fut;
    }
  }

  public void rollback(Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = rollback();
    if (handler != null) {
      fut.onComplete(handler);
    }
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

  private <R> ScheduledCommand<R> doQuery(TxCommand<R> cmd, Promise<R> handler) {
    return new ScheduledCommand<>(cmd, handler);
  }

  @Override
  boolean autoCommit() {
    return false;
  }

  @Override
  public Future<Void> result() {
    return future;
  }
}

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
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.TransactionRollbackException;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.TxCommand;

class TransactionImpl implements Transaction {

  private static final int ST_BEGIN = 0;
  private static final int ST_PENDING = 1;
  private static final int ST_PROCESSING = 2;
  private static final int ST_COMPLETED = 3;

  private final ContextInternal context;
  private final Connection connection;
  private final Deque<CommandBase<?>> pending = new ArrayDeque<>();
  private int status = ST_BEGIN;
  private final Promise<Void> completion;

  TransactionImpl(ContextInternal context, Connection connection) {
    this.context = context;
    this.connection = connection;
    this.completion = context.promise();
  }

  Future<Transaction> begin() {
    PromiseInternal<Transaction> promise = context.promise(this::afterBegin);
    TxCommand<Transaction> begin = new TxCommand<>(TxCommand.Kind.BEGIN, this);
    begin.handler = wrap(promise);
    execute(begin);
    return promise.future();
  }

  private <R> void execute(CommandBase<R> cmd) {
    connection.schedule(cmd, context.promise(cmd.handler));
  }

  private <T> Handler<AsyncResult<T>> wrap(Promise<T> handler) {
    return ar -> {
      synchronized (TransactionImpl.this) {
        if (status == ST_PROCESSING) {
          status = ST_PENDING;
        }
      }
      if (ar.failed()) {
        // We won't recover from this so rollback
        rollback(a -> {
          handler.handle(ar);
        });
      } else {
        handler.handle(ar);
        checkPending();
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

  private void checkPending() {
    while (true) {
      CommandBase<?> cmd;
      synchronized (this) {
        switch (status) {
          case ST_PENDING:
            cmd = pending.poll();
            if (cmd != null) {
              status = ST_PROCESSING;
              execute(cmd);
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

  public <R> void schedule(CommandBase<R> cmd, Promise<R> handler) {
    cmd.handler = wrap(handler);
    schedule(cmd);
  }

  public <R> void schedule(CommandBase<R> b) {
    synchronized (this) {
      pending.add(b);
    }
    checkPending();
  }

  @Override
  public Future<Void> commit() {
    Promise<Void> promise = context.promise();
    CommandBase<Void> commit = txCommand(TxCommand.Kind.COMMIT, promise);
    schedule(commit);
    return promise.future();
  }

  public void commit(Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = commit();
    if (handler != null) {
      fut.onComplete(handler);
    }
  }

  @Override
  public Future<Void> rollback() {
    Promise<Void> promise = context.promise();
    TxCommand<Void> rollback = txCommand(TxCommand.Kind.ROLLBACK, promise);
    synchronized (this) {
      pending.addFirst(rollback);
    }
    checkPending();
    return promise.future();
  }

  public void rollback(Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = rollback();
    if (handler != null) {
      fut.onComplete(handler);
    }
  }

  private TxCommand<Void> txCommand(TxCommand.Kind kind, Promise<Void> promise) {
    TxCommand<Void> cmd = new TxCommand<>(kind, null);
    cmd.handler = ar -> {
      tryComplete(kind);
      promise.handle(ar);
    };
    return cmd;
  }

  private void tryComplete(TxCommand.Kind kind) {
    synchronized (this) {
      if (status == ST_COMPLETED) {
        return;
      }
      status = ST_COMPLETED;
    }
    switch (kind) {
      case COMMIT:
        completion.complete();
        break;
      case ROLLBACK:
        completion.fail(TransactionRollbackException.INSTANCE);
        break;
    }
    checkPending();
  }

  @Override
  public void completion(Handler<AsyncResult<Void>> handler) {
    completion.future().onComplete(handler);
  }

  @Override
  public Future<Void> completion() {
    return completion.future();
  }
}

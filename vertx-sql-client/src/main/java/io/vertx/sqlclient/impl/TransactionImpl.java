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

import io.vertx.core.*;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.TransactionRollbackException;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.spi.protocol.TxCommand;

public class TransactionImpl implements Transaction {

  private final ContextInternal context;
  private final Connection connection;
  private final Promise<TxCommand.Kind> completion;
  private final Handler<Void> endHandler;
  private int pendingQueries;
  private boolean ended;
  private boolean failed;
  private TxCommand<?> endCommand;

  public TransactionImpl(ContextInternal context, Handler<Void> endHandler, Connection connection) {
    this.context = context;
    this.connection = connection;
    this.completion = context.promise();
    this.endHandler = endHandler;
  }

  public Future<Transaction> begin() {
    PromiseInternal<Transaction> promise = context.promise();
    TxCommand<Transaction> begin = new TxCommand<>(TxCommand.Kind.BEGIN, this);
    scheduleInternal(begin, wrap(begin, promise));
    return promise.future();
  }

  public void fail() {
    failed = true;
  }

  private <R> void execute(CommandBase<R> cmd, Completable<R> handler) {
    connection.schedule(cmd, handler);
  }

  private <T> Completable<T> wrap(CommandBase<?> cmd, Completable<T> handler) {
    return (res, err) -> {
      synchronized (TransactionImpl.this) {
        pendingQueries--;
      }
      checkEnd();
      handler.complete(res, err);
    };
  }

  public <R> void schedule(CommandBase<R> cmd, Completable<R> handler) {
    if (!scheduleInternal(cmd, wrap(cmd, handler))) {
      handler.fail("Transaction already completed");
    }
  }

  public <R> boolean scheduleInternal(CommandBase<R> b, Completable<R> handler) {
    synchronized (this) {
      if (ended) {
        return false;
      }
      pendingQueries++;
    }
    execute(b, handler);
    return true;
  }

  private void checkEnd() {
    TxCommand<Void> cmd;
    Completable<Void> handler;
    synchronized (this) {
      if (pendingQueries > 0 || !ended || endCommand != null) {
        return;
      }
      TxCommand.Kind kind = failed ? TxCommand.Kind.ROLLBACK : TxCommand.Kind.COMMIT;
      cmd = new TxCommand<>(kind, null);
      handler = (res, err) -> {
        if (err == null) {
          completion.complete(kind);
        } else {
          completion.fail(err);
        }
      };
      endCommand = cmd;
    }
    endHandler.handle(null);
    execute(cmd, handler);
  }

  private Future<TxCommand.Kind> end(boolean rollback) {
    synchronized (this) {
      if (endCommand != null) {
        return context.failedFuture("Transaction already complete");
      }
      ended = true;
      failed |= rollback;
    }
    checkEnd();
    return completion.future();
  }

  @Override
  public Future<Void> commit() {
    return end(false).flatMap(k -> {
      if (k == TxCommand.Kind.COMMIT) {
        return Future.succeededFuture();
      } else {
        return Future.failedFuture(TransactionRollbackException.INSTANCE);
      }
    });
  }

  public void commit(Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = commit();
    if (handler != null) {
      fut.onComplete(handler);
    }
  }

  @Override
  public Future<Void> rollback() {
    return end(true).mapEmpty();
  }

  public void rollback(Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = rollback();
    if (handler != null) {
      fut.onComplete(handler);
    }
  }

  @Override
  public Future<Void> completion() {
    return completion.future().flatMap(k -> {
      if (k == TxCommand.Kind.COMMIT) {
        return Future.succeededFuture();
      } else {
        return Future.failedFuture(TransactionRollbackException.INSTANCE);
      }
    });
  }
}

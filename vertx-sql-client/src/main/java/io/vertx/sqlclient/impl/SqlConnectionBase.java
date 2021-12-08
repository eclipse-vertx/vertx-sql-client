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
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;
import io.vertx.core.*;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.spi.Driver;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SqlConnectionBase<C extends SqlConnectionBase<C>> extends SqlClientBase implements SqlConnectionInternal {

  private volatile Handler<Throwable> exceptionHandler;
  private volatile Handler<Void> closeHandler;
  protected TransactionImpl tx;
  protected final ContextInternal context;
  protected final ConnectionFactory factory;
  protected final Connection conn;

  public SqlConnectionBase(ContextInternal context, ConnectionFactory factory, Connection conn, Driver driver, QueryTracer tracer, ClientMetrics metrics) {
    super(driver, tracer, metrics);
    this.context = context;
    this.factory = factory;
    this.conn = conn;
  }

  public ConnectionFactory factory() {
    return factory;
  }

  public Connection unwrap() {
    return conn;
  }

  public C prepare(String sql, PrepareOptions options, Handler<AsyncResult<PreparedStatement>> handler) {
    Future<PreparedStatement> fut = prepare(sql, options);
    if (handler != null) {
      fut.onComplete(handler);
    }
    return (C)this;
  }

  public Future<PreparedStatement> prepare(String sql, PrepareOptions options) {
    return schedule(context, new PrepareStatementCommand(sql, options, true))
      .compose(
      cr -> Future.succeededFuture(PreparedStatementImpl.create(conn, tracer, metrics, context, cr, autoCommit())),
      err -> {
        if (conn.isIndeterminatePreparedStatementError(err)) {
          return Future.succeededFuture(PreparedStatementImpl.create(conn, tracer, metrics, context, options, sql, autoCommit()));
        } else {
          return Future.failedFuture(err);
        }
      });
  }

  public C prepare(String sql, Handler<AsyncResult<PreparedStatement>> handler) {
    return prepare(sql, null, handler);
  }

  public Future<PreparedStatement> prepare(String sql) {
    return prepare(sql, (PrepareOptions) null);
  }

  @Override
  protected ContextInternal context() {
    return context;
  }

  @Override
  protected <T> PromiseInternal<T> promise() {
    return context.promise();
  }

  @Override
  protected <T> PromiseInternal<T> promise(Handler<AsyncResult<T>> handler) {
    return context.promise(handler);
  }

  @Override
  public void handleClosed() {
    Handler<Void> handler = closeHandler;
    if (handler != null) {
      context.emit(handler);
    }
  }

  @Override
  public <R> Future<R> schedule(ContextInternal context, CommandBase<R> cmd) {
    if (tx != null) {
      // TODO
      Promise<R> promise = context.promise();
      tx.schedule(cmd, promise);
      return promise.future();
    } else {
      return conn.schedule(context, cmd);
    }
  }

  @Override
  public void handleException(Throwable err) {
    Handler<Throwable> handler = exceptionHandler;
    if (handler != null) {
      context.emit(err, handler);
    } else {
      err.printStackTrace();
    }
  }

  @Override
  public boolean isSSL() {
    return conn.isSsl();
  }

  @Override
  public DatabaseMetadata databaseMetadata() {
    return conn.getDatabaseMetaData();
  }

  @Override
  public C closeHandler(Handler<Void> handler) {
    closeHandler = handler;
    return (C) this;
  }

  @Override
  public C exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return (C) this;
  }

  @Override
  public Future<Transaction> begin() {
    if (tx != null) {
      throw new IllegalStateException();
    }
    tx = new TransactionImpl(context, v -> tx = null, conn);
    return tx.begin();
  }

  @Override
  boolean autoCommit() {
    return tx == null;
  }

  @Override
  public void begin(Handler<AsyncResult<Transaction>> handler) {
    Future<Transaction> fut = begin();
    fut.onComplete(handler);
  }

  public void handleEvent(Object event) {
  }

  @Override
  public Future<Void> close() {
    Promise<Void> promise = promise();
    close(promise);
    return promise.future();
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    close(promise(handler));
  }

  private void close(Promise<Void> promise) {
    context.execute(promise, p -> {
      if (tx != null) {
        tx.rollback(ar -> conn.close(this, p));
        tx = null;
      } else {
        conn.close(this, p);
      }
    });
  }
}

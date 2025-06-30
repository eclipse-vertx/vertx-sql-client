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

package io.vertx.sqlclient.internal;

import io.vertx.core.*;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.PreparedStatementBase;
import io.vertx.sqlclient.impl.TransactionImpl;
import io.vertx.sqlclient.internal.command.CommandBase;
import io.vertx.sqlclient.internal.command.PrepareStatementCommand;
import io.vertx.sqlclient.internal.command.QueryCommandBase;
import io.vertx.sqlclient.impl.pool.SqlConnectionPool;
import io.vertx.sqlclient.impl.tracing.QueryReporter;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.spi.Driver;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SqlConnectionBase<C extends SqlConnectionBase<C>> extends SqlClientBase implements SqlConnectionInternal, Closeable, Connection.Holder {

  private volatile Handler<Throwable> exceptionHandler;
  private volatile Handler<Void> closeHandler;
  private volatile boolean closeFactoryAfterUsage;
  protected TransactionImpl tx;
  protected final ContextInternal context;
  protected final ConnectionFactory factory;
  protected final Connection conn;

  public SqlConnectionBase(ContextInternal context, ConnectionFactory factory, Connection conn, Driver driver) {
    super(driver);
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
    Promise<io.vertx.sqlclient.internal.PreparedStatement> promise = context.promise();
    schedule(new PrepareStatementCommand(sql, options, true), promise);
    return promise.future()
      .compose(
      cr -> Future.succeededFuture(PreparedStatementBase.create(conn, context, cr, autoCommit())),
      err -> {
        if (conn.isIndeterminatePreparedStatementError(err)) {
          return Future.succeededFuture(PreparedStatementBase.create(conn, context, options, sql, autoCommit()));
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
  public void handleClosed() {
    Handler<Void> handler = closeHandler;
    if (handler != null) {
      context.emit(handler);
    }
  }

  @Override
  public <R> void schedule(CommandBase<R> cmd, Completable<R> handler) {
    if (tx != null) {
      // TODO
      tx.schedule(cmd, handler);
    } else {
      QueryReporter queryReporter;
      VertxTracer tracer = context.owner().tracer();
      ClientMetrics metrics = conn.metrics();
      if (!(conn instanceof SqlConnectionPool.PooledConnection) && cmd instanceof QueryCommandBase && (tracer != null || metrics != null)) {
        queryReporter = new QueryReporter(tracer, metrics, context, (QueryCommandBase<?>) cmd, conn);
        queryReporter.before();
        conn
          .schedule(cmd, (res, err) -> {
            queryReporter.after(res, err);
            handler.complete(res, err);
          });
      } else {
        conn.schedule(cmd, handler);
      }
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
  public Transaction transaction() {
    return tx;
  }

  @Override
  protected boolean autoCommit() {
    return tx == null;
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
  public void close(Completable<Void> completion) {
    if (closeFactoryAfterUsage) {
      Completable<Void> next = completion;
      completion = (res, err) -> {
        try {
          next.complete(res, err);
        } finally {
          factory.close((res2, err2) -> {});
        }
      };
    }
    doClose(completion);
    if (closeFactoryAfterUsage) {
      context.removeCloseHook(this);
    }
  }

  private void doClose(Completable<Void> promise) {
    context.execute(promise, p -> {
      if (tx != null) {
        tx.rollback(ar -> conn.close(this, p));
        tx = null;
      } else {
        conn.close(this, p);
      }
    });
  }

  protected static Future<SqlConnection> prepareForClose(ContextInternal ctx, Future<SqlConnection> future) {
    return future.andThen(ar -> {
      if (ar.succeeded()) {
        prepareForClose(ctx, (SqlConnectionBase<?>) ar.result());
      }
    });
  }

  protected static void prepareForClose(ContextInternal ctx, SqlConnectionBase<?> base) {
    base.closeFactoryAfterUsage = true;
    ctx.addCloseHook(base);
  }
}

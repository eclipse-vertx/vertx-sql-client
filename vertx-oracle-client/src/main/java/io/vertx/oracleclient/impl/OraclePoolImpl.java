/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl;

import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.OraclePool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.SqlClientBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

import java.util.function.Function;

public class OraclePoolImpl extends SqlClientBase<OraclePoolImpl> implements OraclePool {

  private final OracleConnectionFactory factory;
  private final VertxInternal vertx;
  private Closeable onClose;

  public static OraclePoolImpl create(VertxInternal vertx, boolean closeVertx,
    OracleConnectOptions connectOptions,
    PoolOptions poolOptions, QueryTracer tracer) {
    VertxMetrics vertxMetrics = vertx.metricsSPI();
    @SuppressWarnings("rawtypes") ClientMetrics metrics = vertxMetrics != null ?
      vertxMetrics.createClientMetrics(connectOptions.getSocketAddress(), "sql",
        connectOptions.getMetricsName()) :
      null;

    OracleConnectionFactory factory = new OracleConnectionFactory(vertx, connectOptions, poolOptions, tracer, metrics);
    OraclePoolImpl pool = new OraclePoolImpl(vertx, factory, metrics, tracer);
    if (closeVertx) {
      pool.onClose(completion -> vertx.close());
    } else {
      ContextInternal ctx = vertx.getContext();
      if (ctx != null) {
        ctx.addCloseHook(completion -> pool.close().onComplete(completion));
      } else {
        vertx.addCloseHook(completion -> pool.close().onComplete(completion));
      }
    }
    return pool;
  }

  public OraclePoolImpl(VertxInternal vertx, OracleConnectionFactory factory, ClientMetrics metrics,
    QueryTracer tracer) {
    super(tracer, metrics);
    this.factory = factory;
    this.vertx = vertx;
  }

  private void onClose(Closeable closeable) {
    this.onClose = closeable;
  }

  @Override
  protected ContextInternal context() {
    return vertx.getOrCreateContext();
  }

  @Override
  public void getConnection(Handler<AsyncResult<SqlConnection>> handler) {
    getConnection().onComplete(handler);
  }

  @Override
  public Future<SqlConnection> getConnection() {
    ContextInternal ctx = vertx.getOrCreateContext();
    return getConnectionInternal(ctx);
  }

  private Future<SqlConnection> getConnectionInternal(ContextInternal ctx) {
    return factory.connect(ctx)
      .map(c -> {
        SqlConnectionImpl<?> connection = new SqlConnectionImpl<>(ctx, factory, c, tracer, metrics);
        c.init(connection);
        return connection;
      });
  }

  @Override
  public Pool connectHandler(Handler<SqlConnection> handler) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public Pool connectionProvider(
    Function<Context, Future<SqlConnection>> provider) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  protected <T> PromiseInternal<T> promise() {
    return vertx.promise();
  }

  @Override
  protected <T> PromiseInternal<T> promise(Handler<AsyncResult<T>> handler) {
    return vertx.promise(handler);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    Promise<Void> promise = Promise.promise();
    factory.close(promise);
    promise.future().onComplete(handler).compose(x -> {
      Promise<Void> p = Promise.promise();
      if (onClose != null) {
        onClose.close(p);
      } else {
        p.complete();
      }
      return p.future();
    });
  }

  @Override
  public Future<Void> close() {
    final Promise<Void> promise = vertx.promise();
    factory.close(promise);

    return promise.future().compose(x -> {
      Promise<Void> p = Promise.promise();
      if (onClose != null) {
        onClose.close(p);
      } else {
        p.complete();
      }
      return p.future();
    });
  }

  @Override
  public <R> Future<R> schedule(ContextInternal contextInternal, CommandBase<R> commandBase) {
    ContextInternal ctx = vertx.getOrCreateContext();
    return getConnectionInternal(ctx)
      .flatMap(conn -> ((SqlConnectionImpl<?>) conn).schedule(ctx, commandBase).eventually(r -> conn.close()));
  }
}

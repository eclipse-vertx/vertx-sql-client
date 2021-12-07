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
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.oracleclient.OraclePool;
import io.vertx.oracleclient.spi.OracleDriver;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.SqlClientBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

import java.util.function.Function;

public class OraclePoolImpl extends SqlClientBase<OraclePoolImpl> implements OraclePool, Closeable {

  private final OracleConnectionFactory factory;
  private final VertxInternal vertx;
  private final CloseFuture closeFuture;

  public OraclePoolImpl(VertxInternal vertx, OracleConnectionFactory factory, ClientMetrics metrics, QueryTracer tracer, CloseFuture closeFuture) {
    super(OracleDriver.INSTANCE, tracer, metrics);
    this.factory = factory;
    this.vertx = vertx;
    this.closeFuture = closeFuture;
  }

  @Override
  public void close(Promise<Void> completion) {
    factory.close(completion);
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
        SqlConnectionImpl<?> connection = new SqlConnectionImpl<>(ctx, factory, c, OracleDriver.INSTANCE, tracer, metrics);
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
    closeFuture.close(context().promise(handler));
  }

  @Override
  public Future<Void> close() {
    return closeFuture.close();
  }

  @Override
  public <R> Future<R> schedule(ContextInternal contextInternal, CommandBase<R> commandBase) {
    ContextInternal ctx = vertx.getOrCreateContext();
    return getConnectionInternal(ctx)
      .flatMap(conn -> ((SqlConnectionImpl<?>) conn).schedule(ctx, commandBase).eventually(r -> conn.close()));
  }
}

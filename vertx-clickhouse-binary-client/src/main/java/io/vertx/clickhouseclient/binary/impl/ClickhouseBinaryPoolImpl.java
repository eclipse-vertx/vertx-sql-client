/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouseclient.binary.impl;

import io.vertx.clickhouseclient.binary.ClickhouseBinaryConnectOptions;
import io.vertx.clickhouseclient.binary.ClickhouseBinaryPool;
import io.vertx.clickhouseclient.binary.spi.ClickhouseBinaryDriver;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ClickhouseBinaryPoolImpl extends PoolBase<ClickhouseBinaryPoolImpl> implements ClickhouseBinaryPool {

  public static ClickhouseBinaryPoolImpl create(VertxInternal vertx,
                                                List<? extends SqlConnectOptions> servers, PoolOptions poolOptions) {
    VertxInternal vx;
    vx = maybeCreateVertx(vertx);
    ClickhouseBinaryConnectOptions baseConnectOptions = ClickhouseBinaryConnectOptions.wrap(servers.get(0));
    QueryTracer tracer = vx.tracer() == null ? null : new QueryTracer(vx.tracer(), baseConnectOptions);
    VertxMetrics vertxMetrics = vx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(baseConnectOptions.getSocketAddress(),
      "sql", baseConnectOptions.getMetricsName()) : null;
    ClickhouseBinaryPoolImpl pool = new ClickhouseBinaryPoolImpl(vx, poolOptions, baseConnectOptions, null, tracer, metrics);
    pool.init();
    ClickhouseBinaryDriver driver = new ClickhouseBinaryDriver();
    List<ConnectionFactory> lst = servers.stream().map(options -> driver.createConnectionFactory(vx, options)).collect(Collectors.toList());
    ConnectionFactory factory = ConnectionFactory.roundRobinSelector(lst);
    pool.connectionProvider(factory::connect);
    CloseFuture closeFuture = pool.closeFuture();
    registerCleanupHook(vertx == null, vx, closeFuture);
    return pool;
  }

  private static void registerCleanupHook(boolean closeVertx, VertxInternal vx, CloseFuture closeFuture) {
    if (closeVertx) {
      closeFuture.future().onComplete(ar -> vx.close());
    } else {
      ContextInternal ctx = vx.getContext();
      if (ctx != null) {
        ctx.addCloseHook(closeFuture);
      } else {
        vx.addCloseHook(closeFuture);
      }
    }
  }

  private static VertxInternal maybeCreateVertx(VertxInternal vertx) {
    VertxInternal vx;
    if (vertx == null) {
      if (Vertx.currentContext() != null) {
        throw new IllegalStateException(
          "Running in a Vertx context => use ClickhouseBinaryPool#pool(Vertx, ClickhouseBinaryConnectOptions, PoolOptions) instead");
      }
      vx = (VertxInternal) Vertx.vertx();
    } else {
      vx = vertx;
    }
    return vx;
  }

  private ClickhouseBinaryPoolImpl(VertxInternal vertx, PoolOptions poolOptions, ClickhouseBinaryConnectOptions baseConnectOptions, Supplier<Future<SqlConnectOptions>> connectOptionsProvider, QueryTracer tracer, ClientMetrics metrics) {
    super(vertx, baseConnectOptions, connectOptionsProvider, tracer, metrics, 1, poolOptions);
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, ConnectionFactory factory, Connection conn) {
    return new ClickhouseBinaryConnectionImpl(factory, context, conn, tracer, metrics);
  }

  @Override
  public ClickhouseBinaryPool connectHandler(Handler<SqlConnection> handler) {
    return (ClickhouseBinaryPool) super.connectHandler(handler);
  }
}

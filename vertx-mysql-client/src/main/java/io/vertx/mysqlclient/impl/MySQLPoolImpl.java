/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.impl;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.mysqlclient.spi.MySQLDriver;
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

public class MySQLPoolImpl extends PoolBase<MySQLPoolImpl> implements MySQLPool {

  public static MySQLPoolImpl create(VertxInternal vertx, List<? extends SqlConnectOptions> servers, PoolOptions poolOptions) {
    MySQLConnectOptions baseConnectOptions = MySQLConnectOptions.wrap(servers.get(0));
    VertxInternal vx;
    if (vertx == null) {
      if (Vertx.currentContext() != null) {
        throw new IllegalStateException("Running in a Vertx context => use MySQLPool#pool(Vertx, MySQLConnectOptions, PoolOptions) instead");
      }
      VertxOptions vertxOptions = new VertxOptions();
      if (baseConnectOptions.isUsingDomainSocket()) {
        vertxOptions.setPreferNativeTransport(true);
      }
      vx = (VertxInternal) Vertx.vertx(vertxOptions);
    } else {
      vx = vertx;
    }
    QueryTracer tracer = vx.tracer() == null ? null : new QueryTracer(vx.tracer(), baseConnectOptions);
    VertxMetrics vertxMetrics = vx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(baseConnectOptions.getSocketAddress(), "sql", baseConnectOptions.getMetricsName()) : null;
    MySQLPoolImpl pool = new MySQLPoolImpl(vx, baseConnectOptions, null, tracer, metrics, poolOptions);
    pool.init();
    MySQLDriver driver = new MySQLDriver();
    List<ConnectionFactory> lst = servers.stream().map(options -> driver.createConnectionFactory(vx, options)).collect(Collectors.toList());
    ConnectionFactory factory = ConnectionFactory.roundRobinSelector(lst);
    pool.connectionProvider(factory::connect);
    CloseFuture closeFuture = pool.closeFuture();
    closeFuture.add(factory);
    if (vertx == null) {
      closeFuture.future().onComplete(ar -> vx.close());
    } else {
      ContextInternal ctx = vx.getContext();
      if (ctx != null) {
        ctx.addCloseHook(closeFuture);
      } else {
        vx.addCloseHook(closeFuture);
      }
    }
    return pool;
  }

  private MySQLPoolImpl(VertxInternal vertx, MySQLConnectOptions baseConnectOptions, Supplier<Future<SqlConnectOptions>> connectOptionsProvider, QueryTracer tracer, ClientMetrics metrics, PoolOptions poolOptions) {
    super(vertx, baseConnectOptions, connectOptionsProvider, tracer, metrics, 1, poolOptions);
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, ConnectionFactory factory, Connection conn) {
    return new MySQLConnectionImpl(context, factory, conn, tracer, metrics);
  }

  @Override
  public MySQLPool connectHandler(Handler<SqlConnection> handler) {
    return (MySQLPool) super.connectHandler(handler);
  }
}

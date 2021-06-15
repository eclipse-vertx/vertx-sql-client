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
import io.vertx.sqlclient.PoolConfig;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class MySQLPoolImpl extends PoolBase<MySQLPoolImpl> implements MySQLPool {

  public static MySQLPoolImpl create(VertxInternal vertx, PoolConfig config) {
    MySQLConnectOptions connectOptions = MySQLConnectOptions.wrap(config.determineConnectOptions());
    VertxInternal vx;
    if (vertx == null) {
      if (Vertx.currentContext() != null) {
        throw new IllegalStateException("Running in a Vertx context => use MySQLPool#pool(Vertx, MySQLConnectOptions, PoolOptions) instead");
      }
      VertxOptions vertxOptions = new VertxOptions();
      if (connectOptions.isUsingDomainSocket()) {
        vertxOptions.setPreferNativeTransport(true);
      }
      vx = (VertxInternal) Vertx.vertx(vertxOptions);
    } else {
      vx = vertx;
    }
    QueryTracer tracer = vx.tracer() == null ? null : new QueryTracer(vx.tracer(), connectOptions);
    VertxMetrics vertxMetrics = vx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(connectOptions.getSocketAddress(), "sql", connectOptions.getMetricsName()) : null;
    MySQLPoolImpl pool = new MySQLPoolImpl(vx, new MySQLConnectionFactory(vx, connectOptions), tracer, metrics, config.options(), config.connectHandler());
    pool.init();
    CloseFuture closeFuture = pool.closeFuture();
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

  private final MySQLConnectionFactory factory;

  private MySQLPoolImpl(VertxInternal vertx, MySQLConnectionFactory factory, QueryTracer tracer, ClientMetrics metrics, PoolOptions poolOptions, Handler<SqlConnection> connectHandler) {
    super(vertx, factory, tracer, metrics, 1, poolOptions, connectHandler);
    this.factory = factory;
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection conn) {
    return new MySQLConnectionImpl(factory, context, conn, tracer, metrics);
  }
}

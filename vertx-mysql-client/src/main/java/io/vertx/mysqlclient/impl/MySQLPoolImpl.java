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

import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class MySQLPoolImpl extends PoolBase<MySQLPoolImpl> implements MySQLPool {

  public static MySQLPoolImpl create(ContextInternal context, boolean closeVertx, MySQLConnectOptions connectOptions, PoolOptions poolOptions) {
    QueryTracer tracer = context.tracer() == null ? null : new QueryTracer(context.tracer(), connectOptions);
    VertxMetrics vertxMetrics = context.owner().metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(connectOptions.getSocketAddress(), "sql", connectOptions.getMetricsName()) : null;
    EventLoopContext eventLoopContext = ConnectionFactory.asEventLoopContext(context);
    MySQLPoolImpl pool = new MySQLPoolImpl(eventLoopContext, new MySQLConnectionFactory(eventLoopContext, connectOptions), tracer, metrics, poolOptions);
    CloseFuture closeFuture = pool.closeFuture();
    if (closeVertx) {
      closeFuture.onComplete(ar -> context.owner().close());
    } else {
      context.addCloseHook(closeFuture);
    }
    return pool;
  }

  private final MySQLConnectionFactory factory;

  private MySQLPoolImpl(EventLoopContext context, MySQLConnectionFactory factory, QueryTracer tracer, ClientMetrics metrics, PoolOptions poolOptions) {
    super(context, factory, tracer, metrics, poolOptions);
    this.factory = factory;
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection conn) {
    return new MySQLConnectionImpl(factory, context, conn, tracer, metrics);
  }
}

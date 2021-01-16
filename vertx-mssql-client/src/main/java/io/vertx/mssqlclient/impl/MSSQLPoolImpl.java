/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl;

import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class MSSQLPoolImpl extends PoolBase<MSSQLPoolImpl> implements MSSQLPool {

  public static MSSQLPoolImpl create(VertxInternal vertx, boolean closeVertx, MSSQLConnectOptions connectOptions, PoolOptions poolOptions) {
    QueryTracer tracer = vertx.tracer() == null ? null : new QueryTracer(vertx.tracer(), connectOptions);
    VertxMetrics vertxMetrics = vertx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(connectOptions.getSocketAddress(), "sql", connectOptions.getMetricsName()) : null;
    MSSQLPoolImpl pool = new MSSQLPoolImpl(vertx, new MSSQLConnectionFactory(vertx, connectOptions), tracer, metrics, poolOptions);
    pool.init();
    CloseFuture closeFuture = pool.closeFuture();
    if (closeVertx) {
      closeFuture.future().onComplete(ar -> vertx.close());
    } else {
      ContextInternal ctx = vertx.getContext();
      if (ctx != null) {
        ctx.addCloseHook(closeFuture);
      } else {
        vertx.addCloseHook(closeFuture);
      }
    }
    return pool;
  }

  private final MSSQLConnectionFactory connectionFactory;

  private MSSQLPoolImpl(VertxInternal vertx, MSSQLConnectionFactory factory, QueryTracer tracer, ClientMetrics metrics, PoolOptions poolOptions) {
    super(vertx, factory, tracer, metrics, 1, poolOptions);
    this.connectionFactory = factory;
  }

  @Override
  public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
    queryBuilder.append('@').append('P').append(1 + index);
    return index;
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection connection) {
    return new MSSQLConnectionImpl(connectionFactory, context, connection, tracer, metrics);
  }
}

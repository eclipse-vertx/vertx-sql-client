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

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;

import java.util.function.Supplier;

public class MSSQLPoolImpl extends PoolBase<MSSQLPoolImpl> implements MSSQLPool {

  public MSSQLPoolImpl(VertxInternal vertx, MSSQLConnectOptions baseConnectOptions, Supplier<Future<SqlConnectOptions>> connectOptionsProvider, QueryTracer tracer, ClientMetrics metrics, PoolOptions poolOptions, CloseFuture closeFuture) {
    super(vertx, baseConnectOptions, connectOptionsProvider, tracer, metrics, 1, poolOptions, closeFuture);
  }

  @Override
  public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
    queryBuilder.append('@').append('P').append(1 + index);
    return index;
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, ConnectionFactory factory, Connection connection) {
    return new MSSQLConnectionImpl(context, factory, connection, tracer, metrics);
  }

  @Override
  public MSSQLPool connectHandler(Handler<SqlConnection> handler) {
    return (MSSQLPool) super.connectHandler(handler);
  }
}

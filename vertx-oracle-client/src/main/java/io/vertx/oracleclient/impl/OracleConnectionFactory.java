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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.datasource.OracleDataSource;

import java.util.concurrent.CompletionStage;

import static io.vertx.oracleclient.impl.OracleDatabaseHelper.createDataSource;

public class OracleConnectionFactory implements ConnectionFactory {

  private final OracleConnectOptions options;
  private final OracleDataSource datasource;
  private final PoolOptions poolOptions;
  private final VertxInternal vertx;
  private final QueryTracer tracer;
  private final ClientMetrics metrics;

  public OracleConnectionFactory(VertxInternal vertx, OracleConnectOptions options,
    PoolOptions poolOptions, QueryTracer tracer, ClientMetrics metrics) {
    this.vertx = vertx;
    this.options = options;
    this.poolOptions = poolOptions;
    this.datasource = createDataSource(options);
    this.tracer = tracer;
    this.metrics = metrics;
  }

  public OracleConnectOptions options() {
    return options;
  }

  public Future<Connection> connect(ContextInternal context) {
    CompletionStage<OracleConnection> stage = Helper
      .getOrHandleSQLException(() -> datasource.createConnectionBuilder().buildAsyncOracle());
    return Helper.contextualize(stage, context)
      .map(c -> new CommandHandler(context, options, c));
  }

  public void close(Promise<Void> promise) {
    promise.complete();
  }

  @Override
  public Future<SqlConnection> connect(Context context) {
    ContextInternal ic  = (ContextInternal) context;
    return connect(ic)
      .map(c -> {
        SqlConnectionImpl connection = new SqlConnectionImpl(ic, this, c, tracer, metrics);
        c.init(connection);
        return connection;
      });
  }
}

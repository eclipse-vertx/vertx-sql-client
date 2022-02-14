/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
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
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.datasource.OracleDataSource;

import java.sql.SQLException;

import static io.vertx.oracleclient.impl.OracleDatabaseHelper.createDataSource;

public class OracleConnectionFactory implements ConnectionFactory {

  private final OracleConnectOptions options;
  private final OracleDataSource datasource;

  public OracleConnectionFactory(VertxInternal vertx, OracleConnectOptions options) {
    this.options = options;
    this.datasource = createDataSource(options);
  }

  @Override
  public void close(Promise<Void> promise) {
    promise.complete();
  }

  @Override
  public Future<SqlConnection> connect(Context context) {
    ContextInternal ctx = (ContextInternal) context;
    QueryTracer tracer = ctx.tracer() == null ? null : new QueryTracer(ctx.tracer(), options);
    return context.<OracleConnection>executeBlocking(prom -> {
      try {
        prom.complete(datasource.createConnectionBuilder().build());
      } catch (SQLException e) {
        prom.fail(e);
      }
    }).map(ora -> {
      CommandHandler conn = new CommandHandler((ContextInternal) context, options, ora);
      OracleConnectionImpl msConn = new OracleConnectionImpl(ctx, this, conn, tracer, null);
      conn.init(msConn);
      return msConn;
    });
  }
}

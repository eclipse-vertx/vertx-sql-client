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
import io.vertx.core.json.JsonObject;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.datasource.OracleDataSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.vertx.oracleclient.impl.Helper.executeBlocking;
import static io.vertx.oracleclient.impl.OracleDatabaseHelper.createDataSource;

public class OracleConnectionFactory implements ConnectionFactory<OracleConnectOptions> {

  private final Supplier<OracleConnectOptions> options;
  private final Map<JsonObject, OracleDataSource> datasources;

  public OracleConnectionFactory(VertxInternal vertx, Supplier<OracleConnectOptions> options) {
    this.options = options;
    this.datasources = new HashMap<>();
  }

  @Override
  public void close(Promise<Void> promise) {
    promise.complete();
  }

  private OracleDataSource getDatasource(SqlConnectOptions options) {
    JsonObject key = options.toJson();
    OracleDataSource datasource;
    synchronized (this) {
      datasource = datasources.get(key);
      if (datasource == null) {
        datasource = createDataSource((OracleConnectOptions) options);
        datasources.put(key, datasource);
      }
    }
    return datasource;
  }

  @Override
  public Future<SqlConnection> connect(Context context, OracleConnectOptions options) {
    OracleDataSource datasource = getDatasource(options);
    ContextInternal ctx = (ContextInternal) context;
    QueryTracer tracer = ctx.tracer() == null ? null : new QueryTracer(ctx.tracer(), options);
    return executeBlocking(context, () -> {
      OracleConnection orac = datasource.createConnectionBuilder().build();
      OracleMetadata metadata = new OracleMetadata(orac.getMetaData());
      OracleJdbcConnection conn = new OracleJdbcConnection(ctx, options, orac, metadata);
      OracleConnectionImpl msConn = new OracleConnectionImpl(ctx, this, conn, tracer, null);
      conn.init(msConn);
      return msConn;
    });
  }
}

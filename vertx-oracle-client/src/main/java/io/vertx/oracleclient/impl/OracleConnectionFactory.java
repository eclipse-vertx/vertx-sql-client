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

import io.vertx.core.Completable;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.spi.ConnectionFactory;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.datasource.OracleDataSource;

import java.util.HashMap;
import java.util.Map;

import static io.vertx.oracleclient.impl.Helper.executeBlocking;
import static io.vertx.oracleclient.impl.OracleDatabaseHelper.createDataSource;

public class OracleConnectionFactory implements ConnectionFactory<OracleConnectOptions> {

  private final Map<JsonObject, OracleDataSource> datasources;

  public OracleConnectionFactory(VertxInternal vertx) {
    this.datasources = new HashMap<>();
  }

  @Override
  public void close(Completable<Void> promise) {
    promise.succeed();
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
    VertxMetrics vertxMetrics = ((VertxInternal)context.owner()).metrics();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(options.getSocketAddress(), "sql", options.getMetricsName()) : null;
    ContextInternal ctx = (ContextInternal) context;
    return executeBlocking(context, () -> {
      OracleConnection orac = datasource.createConnectionBuilder().build();
      OracleMetadata metadata = new OracleMetadata(orac.getMetaData());
      OracleJdbcConnection conn = new OracleJdbcConnection(ctx, metrics, options, orac, metadata);
      OracleConnectionImpl msConn = new OracleConnectionImpl(ctx, this, conn);
      conn.init(msConn);
      return msConn;
    });
  }
}

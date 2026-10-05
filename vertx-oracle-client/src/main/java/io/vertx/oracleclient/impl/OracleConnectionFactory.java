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
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.spi.connection.ConnectionFactory;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.datasource.OracleDataSource;

import java.util.HashMap;
import java.util.Map;

import static io.vertx.core.internal.ContextInternal.EXECUTE_BLOCKING_PREFER_VIRTUAL_THREAD;
import static io.vertx.oracleclient.impl.OracleDatabaseHelper.createDataSource;

public class OracleConnectionFactory implements ConnectionFactory<OracleConnectOptions> {

  private static final Logger log = LoggerFactory.getLogger(OracleConnectionFactory.class);

  private final Map<JsonObject, OracleDataSource> datasources;

  public OracleConnectionFactory() {
    this.datasources = new HashMap<>();
  }

  @Override
  public Future<Void> close() {
    return Future.succeededFuture();
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
  public Future<Connection> connect(Context context, OracleConnectOptions options) {
    ContextInternal ctx = (ContextInternal) context;
    OracleDataSource datasource = getDatasource(options);
    VertxInternal vertx = ctx.owner();
    VertxMetrics vertxMetrics = vertx.metrics();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(options.getSocketAddress(), "sql", options.getMetricsName()) : null;
    int executeBlockingFlags = computeExecuteBlockingFlags(vertx, options);
    return ctx.executeBlocking(() -> {
      OracleConnection orac = datasource.createConnectionBuilder().build();
      OracleMetadata metadata = new OracleMetadata(orac.getMetaData());
      return new OracleJdbcConnection(ctx, metrics, options, orac, metadata, executeBlockingFlags);
    }, executeBlockingFlags);
  }

  private static int computeExecuteBlockingFlags(VertxInternal vertx, OracleConnectOptions options) {
    boolean useVirtualThreads = options.getUseVirtualThreads();
    if (useVirtualThreads) {
      if (vertx.isVirtualThreadAvailable()) {
        return EXECUTE_BLOCKING_PREFER_VIRTUAL_THREAD;
      }
      log.warn("Virtual threads requested but not available on this JVM, falling back to platform threads");
    }
    return 0;
  }
}

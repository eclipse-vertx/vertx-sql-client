/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
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
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.SingletonSupplier;
import io.vertx.sqlclient.impl.metrics.ClientMetricsProvider;
import io.vertx.sqlclient.impl.metrics.DynamicClientMetricsProvider;
import io.vertx.sqlclient.impl.metrics.SingleServerClientMetricsProvider;
import io.vertx.sqlclient.spi.ConnectionFactory;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.datasource.OracleDataSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.vertx.oracleclient.impl.Helper.executeBlocking;
import static io.vertx.oracleclient.impl.OracleDatabaseHelper.createDataSource;

public class OracleConnectionFactory implements ConnectionFactory {

  private final Supplier<? extends Future<? extends SqlConnectOptions>> options;
  private final Map<JsonObject, OracleDataSource> datasources;
  private final ClientMetricsProvider clientMetricsProvider;

  public OracleConnectionFactory(VertxInternal vertx, Supplier<? extends Future<? extends SqlConnectOptions>> options) {
    VertxMetrics metrics = vertx.metricsSPI();
    ClientMetricsProvider clientMetricsProvider;
    if (metrics != null) {
      if (options instanceof SingletonSupplier) {
        SqlConnectOptions option = (SqlConnectOptions) ((SingletonSupplier) options).unwrap();
        ClientMetrics<?, ?, ?, ?> clientMetrics = metrics.createClientMetrics(option.getSocketAddress(), "sql", option.getMetricsName());
        clientMetricsProvider = new SingleServerClientMetricsProvider(clientMetrics);
      } else {
        clientMetricsProvider = new DynamicClientMetricsProvider(metrics);
      }
    } else {
      clientMetricsProvider = null;
    }
    this.clientMetricsProvider = clientMetricsProvider;
    this.options = options;
    this.datasources = new HashMap<>();
  }

  @Override
  public ClientMetricsProvider metricsProvider() {
    return clientMetricsProvider;
  }

  @Override
  public void close(Promise<Void> promise) {
    if (clientMetricsProvider != null) {
      clientMetricsProvider.close(promise);
    } else {
      promise.complete();
    }
  }

  @Override
  public Future<SqlConnection> connect(Context context) {
    return connect(context, options.get());
  }

  private OracleDataSource getDatasource(SqlConnectOptions options) {
    JsonObject key = options.toJson();
    OracleDataSource datasource;
    synchronized (this) {
      datasource = datasources.get(key);
      if (datasource == null) {
        datasource = createDataSource(OracleConnectOptions.wrap(options));
        datasources.put(key, datasource);
      }
    }
    return datasource;
  }

  @Override
  public Future<SqlConnection> connect(Context context, SqlConnectOptions options) {
    OracleDataSource datasource = getDatasource(options);
    ClientMetrics metrics = clientMetricsProvider != null ? clientMetricsProvider.metricsFor(options) : null;
    ContextInternal ctx = (ContextInternal) context;
    return executeBlocking(context, () -> {
      OracleConnection orac = datasource.createConnectionBuilder().build();
      OracleMetadata metadata = new OracleMetadata(orac.getMetaData());
      OracleJdbcConnection conn = new OracleJdbcConnection(ctx, metrics, OracleConnectOptions.wrap(options), orac, metadata);
      OracleConnectionImpl msConn = new OracleConnectionImpl(ctx, this, conn);
      conn.init(msConn);
      return msConn;
    });
  }
}

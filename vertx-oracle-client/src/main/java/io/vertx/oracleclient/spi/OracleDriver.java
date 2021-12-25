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
package io.vertx.oracleclient.spi;

import io.vertx.core.Vertx;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.impl.OracleConnectionFactory;
import io.vertx.oracleclient.impl.OracleConnectionUriParser;
import io.vertx.oracleclient.impl.OraclePoolImpl;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

import java.util.List;

public class OracleDriver implements Driver {

  private static final String SHARED_CLIENT_KEY = "__vertx.shared.oracleclient";

  public static final OracleDriver INSTANCE = new OracleDriver();

  @Override
  public Pool newPool(Vertx vertx, List<? extends SqlConnectOptions> databases,
                      PoolOptions options, CloseFuture closeFuture) {
    VertxInternal vx = (VertxInternal) vertx;
    ContextInternal context = vx.getOrCreateContext();
    OracleConnectOptions database = wrap(databases.get(0));
    QueryTracer tracer = context.tracer() == null ? null : new QueryTracer(vx.tracer(), database);
    VertxMetrics vertxMetrics = vx.metricsSPI();
    @SuppressWarnings("rawtypes") ClientMetrics metrics = vertxMetrics != null ?
      vertxMetrics.createClientMetrics(database.getSocketAddress(), "sql",
        database.getMetricsName()) :
      null;
    OracleConnectionFactory factory;
    if (options.isShared()) {
      factory = vx.createSharedClient(SHARED_CLIENT_KEY, options.getName(), closeFuture, cf -> {
        OracleConnectionFactory connectionFactory = new OracleConnectionFactory(vx, database, options, tracer, metrics);
        cf.add(connectionFactory);
        return connectionFactory;
      });
    } else {
      factory = new OracleConnectionFactory(vx, database, options, tracer, metrics);
      closeFuture.add(factory);
    }
    return new OraclePoolImpl(vx, factory, metrics, tracer, closeFuture);
  }

  @Override
  public ConnectionFactory createConnectionFactory(Vertx vertx,
    SqlConnectOptions database) {
    OracleConnectOptions options = wrap(database);
    VertxInternal vi = (VertxInternal) vertx;
    VertxMetrics vertxMetrics = vi.metricsSPI();
    @SuppressWarnings("rawtypes") ClientMetrics metrics = vertxMetrics != null ?
      vertxMetrics.createClientMetrics(database.getSocketAddress(), "sql",
        database.getMetricsName()) :
      null;
    QueryTracer tracer = new QueryTracer(vi.tracer(), database);
    return new OracleConnectionFactory(vi, options, new PoolOptions(), tracer, metrics);
  }

  @Override
  public OracleConnectOptions parseConnectionUri(String uri) {
    JsonObject conf = OracleConnectionUriParser.parse(uri, false);
    return conf == null ? null : new OracleConnectOptions(conf);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof OracleConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  private static OracleConnectOptions wrap(SqlConnectOptions options) {
    if (options instanceof OracleConnectOptions) {
      return (OracleConnectOptions) options;
    } else {
      return new OracleConnectOptions(options);
    }
  }

}

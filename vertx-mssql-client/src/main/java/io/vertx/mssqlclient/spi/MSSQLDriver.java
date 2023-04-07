/*
 * Copyright (C) 2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.mssqlclient.spi;

import io.vertx.core.Vertx;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.impl.MSSQLConnectionFactory;
import io.vertx.mssqlclient.impl.MSSQLConnectionImpl;
import io.vertx.mssqlclient.impl.MSSQLConnectionUriParser;
import io.vertx.mssqlclient.impl.MSSQLPoolImpl;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolImpl;
import io.vertx.sqlclient.impl.SqlConnectionInternal;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

import java.util.function.Supplier;

public class MSSQLDriver implements Driver<MSSQLConnectOptions> {

  private static final String SHARED_CLIENT_KEY = "__vertx.shared.mssqlclient";

  public static final MSSQLDriver INSTANCE = new MSSQLDriver();

  @Override
  public Pool newPool(Vertx vertx, Supplier<MSSQLConnectOptions> databases, PoolOptions options, CloseFuture closeFuture) {
    VertxInternal vx = (VertxInternal) vertx;
    PoolImpl pool;
    if (options.isShared()) {
      pool = vx.createSharedClient(SHARED_CLIENT_KEY, options.getName(), closeFuture, cf -> newPoolImpl(vx, databases, options, cf));
    } else {
      pool = newPoolImpl(vx, databases, options, closeFuture);
    }
    return new MSSQLPoolImpl(vx, closeFuture, pool);
  }

  private PoolImpl newPoolImpl(VertxInternal vertx, Supplier<MSSQLConnectOptions> databases, PoolOptions options, CloseFuture closeFuture) {
    MSSQLConnectOptions baseConnectOptions = MSSQLConnectOptions.wrap(databases.get());
    QueryTracer tracer = vertx.tracer() == null ? null : new QueryTracer(vertx.tracer(), baseConnectOptions);
    VertxMetrics vertxMetrics = vertx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(baseConnectOptions.getSocketAddress(), "sql", baseConnectOptions.getMetricsName()) : null;
    PoolImpl pool = new PoolImpl(vertx, this, tracer, metrics, 1, options, null, null, closeFuture);
    ConnectionFactory<MSSQLConnectOptions> factory = createConnectionFactory(vertx, databases);
    pool.connectionProvider(factory::connect);
    pool.init();
    closeFuture.add(factory);
    return pool;
  }

  @Override
  public MSSQLConnectOptions parseConnectionUri(String uri) {
    JsonObject conf = MSSQLConnectionUriParser.parse(uri, false);
    return conf == null ? null : new MSSQLConnectOptions(conf);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof MSSQLConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  @Override
  public ConnectionFactory<MSSQLConnectOptions> createConnectionFactory(Vertx vertx, MSSQLConnectOptions database) {
    return new MSSQLConnectionFactory((VertxInternal) vertx, () -> MSSQLConnectOptions.wrap(database));
  }

  @Override
  public ConnectionFactory<MSSQLConnectOptions> createConnectionFactory(Vertx vertx, Supplier<MSSQLConnectOptions> database) {
    return new MSSQLConnectionFactory((VertxInternal) vertx, () -> MSSQLConnectOptions.wrap(database.get()));
  }

  @Override
  public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
    queryBuilder.append('@').append('P').append(1 + index);
    return index;
  }

  @Override
  public SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory<MSSQLConnectOptions> factory, Connection conn, QueryTracer tracer, ClientMetrics metrics) {
    return new MSSQLConnectionImpl(context, factory, conn, tracer, metrics);
  }
}

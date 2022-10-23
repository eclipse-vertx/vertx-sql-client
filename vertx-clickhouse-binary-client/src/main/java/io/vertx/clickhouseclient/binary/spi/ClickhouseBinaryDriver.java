/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouseclient.binary.spi;

import io.vertx.clickhouseclient.binary.ClickhouseBinaryConnectOptions;
import io.vertx.clickhouseclient.binary.impl.ClickhouseBinaryConnectionFactory;
import io.vertx.clickhouseclient.binary.impl.ClickhouseBinaryConnectionUriParser;
import io.vertx.clickhouseclient.binary.impl.ClickhouseBinaryPoolImpl;
import io.vertx.core.Vertx;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.PoolImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

import java.util.List;
import java.util.stream.Collectors;

public class ClickhouseBinaryDriver implements Driver {
  private static final String SHARED_CLIENT_KEY = "__vertx.shared.clickhousebinaryclient";
  public static final ClickhouseBinaryDriver INSTANCE = new ClickhouseBinaryDriver();

  @Override
  public ClickhouseBinaryPoolImpl newPool(Vertx vertx, List<? extends SqlConnectOptions> databases, PoolOptions options, CloseFuture closeFuture) {
    VertxInternal vx = (VertxInternal) vertx;
    PoolImpl pool;
    if (options.isShared()) {
      pool = vx.createSharedClient(SHARED_CLIENT_KEY, options.getName(), closeFuture, cf -> newPoolImpl(vx, databases, options, cf));
    } else {
      pool = newPoolImpl(vx, databases, options, closeFuture);
    }
    return new ClickhouseBinaryPoolImpl(vx, closeFuture, pool);
  }

  private PoolImpl newPoolImpl(VertxInternal vertx, List<? extends SqlConnectOptions> databases, PoolOptions options, CloseFuture closeFuture) {
    ClickhouseBinaryConnectOptions baseConnectOptions = ClickhouseBinaryConnectOptions.wrap(databases.get(0));
    QueryTracer tracer = vertx.tracer() == null ? null : new QueryTracer(vertx.tracer(), baseConnectOptions);
    VertxMetrics vertxMetrics = vertx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(baseConnectOptions.getSocketAddress(),
      "sql", baseConnectOptions.getMetricsName()) : null;
    PoolImpl pool = new PoolImpl(vertx, this, tracer, metrics, 1, options, null,  null, closeFuture);
    List<ConnectionFactory> lst = databases.stream().map(o -> createConnectionFactory(vertx, o)).collect(Collectors.toList());
    ConnectionFactory factory = ConnectionFactory.roundRobinSelector(lst);
    pool.connectionProvider(factory::connect);
    pool.init();
    closeFuture.add(factory);
    return pool;
  }

  @Override
  public ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database) {
    return new ClickhouseBinaryConnectionFactory((VertxInternal) vertx, ClickhouseBinaryConnectOptions.wrap(database));
  }

  @Override
  public SqlConnectOptions parseConnectionUri(String uri) {
    JsonObject conf = ClickhouseBinaryConnectionUriParser.parse(uri, false);
    return conf == null ? null : new ClickhouseBinaryConnectOptions(conf);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof ClickhouseBinaryConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }
}

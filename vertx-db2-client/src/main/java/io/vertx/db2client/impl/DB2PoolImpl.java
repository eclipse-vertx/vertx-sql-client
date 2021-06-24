/*
 * Copyright (C) 2019,2020 IBM Corporation
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
package io.vertx.db2client.impl;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Pool;
import io.vertx.db2client.spi.DB2Driver;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DB2PoolImpl extends PoolBase<DB2PoolImpl> implements DB2Pool {

  public static DB2PoolImpl create(VertxInternal vertx, boolean pipelined, List<? extends SqlConnectOptions> servers, PoolOptions poolOptions) {
    DB2ConnectOptions baseConnectOptions = DB2ConnectOptions.wrap(servers.get(0));
    VertxInternal vx;
    if (vertx == null) {
      if (Vertx.currentContext() != null) {
        throw new IllegalStateException(
          "Running in a Vertx context => use DB2Pool#pool(Vertx, DB2ConnectOptions, PoolOptions) instead");
      }
      vx = (VertxInternal) Vertx.vertx();
    } else {
      vx = vertx;
    }
    QueryTracer tracer = vx.tracer() == null ? null : new QueryTracer(vx.tracer(), baseConnectOptions);
    VertxMetrics vertxMetrics = vx.metricsSPI();
    int pipeliningLimit = pipelined ? baseConnectOptions.getPipeliningLimit() : 1;
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(baseConnectOptions.getSocketAddress(), "sql", baseConnectOptions.getMetricsName()) : null;
    DB2PoolImpl pool = new DB2PoolImpl(vx, pipeliningLimit, poolOptions, baseConnectOptions, null, tracer, metrics);
    pool.init();
    DB2Driver driver = new DB2Driver();
    List<ConnectionFactory> lst = servers.stream().map(options -> driver.createConnectionFactory(vx, options)).collect(Collectors.toList());
    ConnectionFactory factory = ConnectionFactory.roundRobinSelector(lst);
    pool.connectionProvider(factory::connect);
    CloseFuture closeFuture = pool.closeFuture();
    closeFuture.add(factory);
    if (vertx == null) {
      closeFuture.future().onComplete(ar -> vx.close());
    } else {
      ContextInternal ctx = vx.getContext();
      if (ctx != null) {
        ctx.addCloseHook(closeFuture);
      } else {
        vx.addCloseHook(closeFuture);
      }
    }
    return pool;
  }

  private DB2PoolImpl(VertxInternal vertx, int pipeliningLimit, PoolOptions poolOptions, DB2ConnectOptions baseConnectOptions, Supplier<Future<SqlConnectOptions>> connectOptionsProvider, QueryTracer tracer, ClientMetrics metrics) {
    super(vertx, baseConnectOptions, connectOptionsProvider, tracer, metrics, pipeliningLimit, poolOptions);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, ConnectionFactory factory, Connection conn) {
    return new DB2ConnectionImpl(context, factory, conn, tracer, metrics);
  }

  @Override
  public DB2Pool connectHandler(Handler<SqlConnection> handler) {
    return (DB2Pool) super.connectHandler(handler);
  }
}

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

import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class DB2PoolImpl extends PoolBase<DB2PoolImpl> implements DB2Pool {

  public static DB2PoolImpl create(VertxInternal vertx, boolean closeVertx, boolean pipelined, DB2ConnectOptions connectOptions,
                                   PoolOptions poolOptions) {
    QueryTracer tracer = vertx.tracer() == null ? null : new QueryTracer(vertx.tracer(), connectOptions);
    VertxMetrics vertxMetrics = vertx.metricsSPI();
    int pipeliningLimit = pipelined ? connectOptions.getPipeliningLimit() : 1;
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(connectOptions.getSocketAddress(), "sql", connectOptions.getMetricsName()) : null;
    DB2PoolImpl pool = new DB2PoolImpl(vertx, pipeliningLimit, poolOptions, new DB2ConnectionFactory(vertx, connectOptions), tracer, metrics);
    pool.init();
    CloseFuture closeFuture = pool.closeFuture();
    if (closeVertx) {
      closeFuture.future().onComplete(ar -> vertx.close());
    } else {
      ContextInternal ctx = vertx.getContext();
      if (ctx != null) {
        ctx.addCloseHook(closeFuture);
      } else {
        vertx.addCloseHook(closeFuture);
      }
    }
    return pool;
  }

  private final DB2ConnectionFactory factory;

  private DB2PoolImpl(VertxInternal vertx, int pipeliningLimit, PoolOptions poolOptions, DB2ConnectionFactory factory, QueryTracer tracer, ClientMetrics metrics) {
    super(vertx, factory, tracer, metrics, pipeliningLimit, poolOptions);
    this.factory = factory;
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection conn) {
    return new DB2ConnectionImpl(factory, context, conn, tracer, metrics);
  }
}

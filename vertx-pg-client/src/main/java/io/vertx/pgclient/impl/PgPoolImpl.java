/*
 * Copyright (C) 2017 Julien Viet
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
 *
 */

package io.vertx.pgclient.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.pgclient.*;
import io.vertx.sqlclient.PoolConfig;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

import java.util.function.Supplier;

/**
 * Todo :
 *
 * - handle timeout when acquiring a connection
 * - for per statement pooling, have several physical connection and use the less busy one to avoid head of line blocking effect
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class PgPoolImpl extends PoolBase<PgPoolImpl> implements PgPool {

  public static PgPoolImpl create(final VertxInternal vertx, boolean pipelined, PoolConfig config) {
    PgConnectOptions baseConnectOptions = PgConnectOptions.wrap(config.baseConnectOptions());
    VertxInternal vx;
    if (vertx == null) {
      if (Vertx.currentContext() != null) {
        throw new IllegalStateException("Running in a Vertx context => use PgPool#pool(Vertx, PgConnectOptions, PoolOptions) instead");
      }
      VertxOptions vertxOptions = new VertxOptions();
      if (baseConnectOptions.isUsingDomainSocket()) {
        vertxOptions.setPreferNativeTransport(true);
      }
      vx = (VertxInternal) Vertx.vertx(vertxOptions);
    } else {
      vx = vertx;
    }
    Handler<SqlConnection> connectHook = config.connectHandler();
    PoolOptions poolOptions = config.options();
    QueryTracer tracer = vx.tracer() == null ? null : new QueryTracer(vx.tracer(), baseConnectOptions);
    VertxMetrics vertxMetrics = vx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(baseConnectOptions.getSocketAddress(), "sql", baseConnectOptions.getMetricsName()) : null;
    int pipeliningLimit = pipelined ? baseConnectOptions.getPipeliningLimit() : 1;
    PgPoolImpl pool = new PgPoolImpl(vx, baseConnectOptions, config.connectOptionsProvider(), tracer, metrics, pipeliningLimit, poolOptions, connectHook);
    pool.init();
    CloseFuture closeFuture = pool.closeFuture();
    vx.addCloseHook(closeFuture);
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

  private PgPoolImpl(VertxInternal vertx, PgConnectOptions baseConnectOptions, Supplier<SqlConnectOptions> connectOptionsProvider, QueryTracer tracer, ClientMetrics metrics, int pipeliningLimit, PoolOptions poolOptions, Handler<SqlConnection> connectHook) {
    super(vertx, baseConnectOptions, connectOptionsProvider, new PgConnectionFactory(vertx, baseConnectOptions), tracer, metrics, pipeliningLimit, poolOptions, connectHook);
  }

  @Override
  public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
    queryBuilder.append('$').append(1 + index);
    return index;
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection conn) {
    return new PgConnectionImpl((PgConnectionFactory) connectionFactory(), context, conn, tracer, metrics);
  }
}

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

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.pgclient.*;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

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

  public static PgPoolImpl create(boolean pipelined, PgConnectOptions connectOptions, PoolOptions poolOptions) {
    if (Vertx.currentContext() != null) {
      throw new IllegalStateException("Running in a Vertx context => use PgPool#pool(Vertx, PgConnectOptions, PoolOptions) instead");
    }
    VertxOptions vertxOptions = new VertxOptions();
    if (connectOptions.isUsingDomainSocket()) {
      vertxOptions.setPreferNativeTransport(true);
    }
    VertxInternal vertx = (VertxInternal) Vertx.vertx(vertxOptions);
    return create(vertx, true, pipelined, connectOptions, poolOptions);
  }

  public static PgPoolImpl create(VertxInternal vertx, boolean closeVertx, boolean pipelined, PgConnectOptions connectOptions, PoolOptions poolOptions) {
    QueryTracer tracer = vertx.tracer() == null ? null : new QueryTracer(vertx.tracer(), connectOptions);
    VertxMetrics vertxMetrics = vertx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(connectOptions.getSocketAddress(), "sql", connectOptions.getMetricsName()) : null;
    int pipeliningLimit = pipelined ? connectOptions.getPipeliningLimit() : 1;
    PgPoolImpl pool = new PgPoolImpl(vertx, new PgConnectionFactory(vertx, connectOptions), tracer, metrics, pipeliningLimit, poolOptions);
    pool.init();
    CloseFuture closeFuture = pool.closeFuture();
    vertx.addCloseHook(closeFuture);
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

  private final PgConnectionFactory factory;

  private PgPoolImpl(VertxInternal vertx, PgConnectionFactory factory, QueryTracer tracer, ClientMetrics metrics, int pipeliningLimit, PoolOptions poolOptions) {
    super(vertx, factory, tracer, metrics, pipeliningLimit, poolOptions);
    this.factory = factory;
  }

  @Override
  public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
    queryBuilder.append('$').append(1 + index);
    return index;
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection conn) {
    return new PgConnectionImpl(factory, context, conn, tracer, metrics);
  }
}

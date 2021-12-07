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
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Pool;
import io.vertx.db2client.spi.DB2Driver;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;

import java.util.function.Supplier;

public class DB2PoolImpl extends PoolBase<DB2PoolImpl> implements DB2Pool {

  public DB2PoolImpl(VertxInternal vertx, int pipeliningLimit, PoolOptions poolOptions, DB2ConnectOptions baseConnectOptions, Supplier<Future<SqlConnectOptions>> connectOptionsProvider, QueryTracer tracer, ClientMetrics metrics, CloseFuture closeFuture) {
    super(vertx, DB2Driver.INSTANCE, baseConnectOptions, connectOptionsProvider, tracer, metrics, pipeliningLimit, poolOptions, closeFuture);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected DB2ConnectionImpl wrap(ContextInternal context, ConnectionFactory factory, Connection conn) {
    return new DB2ConnectionImpl(context, factory, conn, tracer, metrics);
  }

  @Override
  public DB2Pool connectHandler(Handler<SqlConnection> handler) {
    return (DB2Pool) super.connectHandler(handler);
  }
}

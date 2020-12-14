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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Connection;
import io.vertx.db2client.impl.command.PingCommand;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class DB2ConnectionImpl extends SqlConnectionImpl<DB2ConnectionImpl> implements DB2Connection {

  public static Future<DB2Connection> connect(Vertx vertx, DB2ConnectOptions options) {
    ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();
    DB2ConnectionFactory client;
    try {
      client = new DB2ConnectionFactory(ConnectionFactory.asEventLoopContext(ctx), options);
    } catch (Exception e) {
      return ctx.failedFuture(e);
    }
    ctx.addCloseHook(client);
    QueryTracer tracer = ctx.tracer() == null ? null : new QueryTracer(ctx.tracer(), options);
    Promise<Connection> promise = ctx.promise();
    client.connect(promise);
    return promise.future()
      .map(conn -> {
        DB2ConnectionImpl db2Connection = new DB2ConnectionImpl(client, ctx, conn, tracer, null);
        conn.init(db2Connection);
        return db2Connection;
      });
  }

  public DB2ConnectionImpl(DB2ConnectionFactory factory, ContextInternal context, Connection conn, QueryTracer tracer, ClientMetrics metrics) {
    super(context, conn, tracer, metrics);
  }

  @Override
  public DB2Connection ping(Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = ping();
    if (handler != null) {
      fut.onComplete(handler);
    }
    return this;
  }

  @Override
  public Future<Void> ping() {
    Promise<Void> promise = promise();
    schedule(new PingCommand(), promise);
    return promise.future();
  }

  @Override
  public DB2Connection debug(Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException("Debug command not implemented");
  }

  @Override
  public Future<Void> debug() {
    throw new UnsupportedOperationException("Debug command not implemented");
  }
}

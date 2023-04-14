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
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Connection;
import io.vertx.db2client.impl.command.PingCommand;
import io.vertx.db2client.spi.DB2Driver;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SingletonSupplier;
import io.vertx.sqlclient.impl.SqlConnectionBase;
import io.vertx.sqlclient.spi.ConnectionFactory;

public class DB2ConnectionImpl extends SqlConnectionBase<DB2ConnectionImpl> implements DB2Connection {

  public static Future<DB2Connection> connect(Vertx vertx, DB2ConnectOptions options) {
    ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();
    DB2ConnectionFactory client;
    try {
      client = new DB2ConnectionFactory(ctx.owner(), SingletonSupplier.wrap(options));
    } catch (Exception e) {
      return ctx.failedFuture(e);
    }
    return prepareForClose(ctx, client.connect(ctx)).map(DB2Connection::cast);
  }

  public DB2ConnectionImpl(ContextInternal context, ConnectionFactory factory, Connection conn) {
    super(context, factory, conn, DB2Driver.INSTANCE);
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
    return schedule(context, new PingCommand());
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

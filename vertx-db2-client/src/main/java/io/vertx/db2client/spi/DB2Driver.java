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
package io.vertx.db2client.spi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.internal.CloseFuture;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.impl.*;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.CloseablePool;
import io.vertx.sqlclient.impl.PoolImpl;
import io.vertx.sqlclient.impl.SqlConnectionInternal;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

import java.util.function.Supplier;

public class DB2Driver implements Driver<DB2ConnectOptions> {

  private static final String SHARED_CLIENT_KEY = "__vertx.shared.db2client";

  public static final DB2Driver INSTANCE = new DB2Driver();

  @Override
  public DB2ConnectOptions downcast(SqlConnectOptions connectOptions) {
    return connectOptions instanceof DB2ConnectOptions ? (DB2ConnectOptions) connectOptions : new DB2ConnectOptions(connectOptions);
  }

  @Override
  public Pool newPool(Vertx vertx, Supplier<Future<DB2ConnectOptions>> databases, PoolOptions poolOptions, NetClientOptions transportOptions, Handler<SqlConnection> connectHandler, CloseFuture closeFuture) {
    VertxInternal vx = (VertxInternal) vertx;
    PoolImpl pool;
    if (poolOptions.isShared()) {
      pool = vx.createSharedResource(SHARED_CLIENT_KEY, poolOptions.getName(), closeFuture, cf -> newPoolImpl(vx, connectHandler, databases, poolOptions, transportOptions, cf));
    } else {
      pool = newPoolImpl(vx, connectHandler, databases, poolOptions, transportOptions, closeFuture);
    }
    return new CloseablePool<>(vx, closeFuture, pool);
  }

  private PoolImpl newPoolImpl(VertxInternal vertx, Handler<SqlConnection> connectHandler, Supplier<Future<DB2ConnectOptions>> databases, PoolOptions options, NetClientOptions transportOptions, CloseFuture closeFuture) {
    boolean pipelinedPool = options instanceof Db2PoolOptions && ((Db2PoolOptions) options).isPipelined();
    ConnectionFactory<DB2ConnectOptions> factory = createConnectionFactory(vertx, transportOptions);
    PoolImpl pool = new PoolImpl(vertx, this, pipelinedPool, options, null, null, context -> factory.connect(context, databases.get()), connectHandler, closeFuture);
    pool.init();
    closeFuture.add(factory);
    return pool;
  }

  @Override
  public DB2ConnectOptions parseConnectionUri(String uri) {
    JsonObject conf = DB2ConnectionUriParser.parse(uri, false);
    return conf == null ? null : new DB2ConnectOptions(conf);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof DB2ConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  @Override
  public ConnectionFactory<DB2ConnectOptions> createConnectionFactory(Vertx vertx, NetClientOptions transportOptions) {
    return new DB2ConnectionFactory((VertxInternal) vertx);
  }

  @Override
  public SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory<DB2ConnectOptions> factory, Connection conn) {
    return new DB2ConnectionImpl(context, factory, conn);
  }
}

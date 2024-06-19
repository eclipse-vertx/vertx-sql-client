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
package io.vertx.mysqlclient.spi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.internal.CloseFuture;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.impl.*;
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

public class MySQLDriver implements Driver<MySQLConnectOptions> {

  private static final String SHARED_CLIENT_KEY = "__vertx.shared.mysqlclient";

  public static final MySQLDriver INSTANCE = new MySQLDriver();

  @Override
  public MySQLConnectOptions downcast(SqlConnectOptions connectOptions) {
    return connectOptions instanceof MySQLConnectOptions ? (MySQLConnectOptions) connectOptions : new MySQLConnectOptions(connectOptions);
  }

  @Override
  public Pool newPool(Vertx vertx, Supplier<Future<MySQLConnectOptions>> databases, PoolOptions options, NetClientOptions transportOptions, Handler<SqlConnection> connectHandler, CloseFuture closeFuture) {
    VertxInternal vx = (VertxInternal) vertx;
    PoolImpl pool;
    if (options.isShared()) {
      pool = vx.createSharedResource(SHARED_CLIENT_KEY, options.getName(), closeFuture, cf -> newPoolImpl(vx, connectHandler, databases, options, transportOptions, cf));
    } else {
      pool = newPoolImpl(vx, connectHandler, databases, options, transportOptions, closeFuture);
    }
    return new CloseablePool<>(vx, closeFuture, pool);
  }

  private PoolImpl newPoolImpl(VertxInternal vertx, Handler<SqlConnection> connectHandler, Supplier<Future<MySQLConnectOptions>> databases, PoolOptions poolOptions, NetClientOptions transportOptions, CloseFuture closeFuture) {
    boolean pipelinedPool = poolOptions instanceof MySQLPoolOptions && ((MySQLPoolOptions) poolOptions).isPipelined();
    ConnectionFactory<MySQLConnectOptions> factory = createConnectionFactory(vertx, transportOptions);
    PoolImpl pool = new PoolImpl(vertx, this, pipelinedPool, poolOptions, null, null, context -> factory.connect(context, databases.get()), connectHandler, closeFuture);
    pool.init();
    closeFuture.add(factory);
    return pool;
  }

  @Override
  public MySQLConnectOptions parseConnectionUri(String uri) {
    JsonObject conf = MySQLConnectionUriParser.parse(uri, false);
    return conf == null ? null : new MySQLConnectOptions(conf);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof MySQLConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  @Override
  public ConnectionFactory<MySQLConnectOptions> createConnectionFactory(Vertx vertx, NetClientOptions transportOptions) {
    return new MySQLConnectionFactory((VertxInternal) vertx);
  }

  @Override
  public SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory<MySQLConnectOptions> factory, Connection conn) {
    return new MySQLConnectionImpl(context, factory, conn);
  }
}

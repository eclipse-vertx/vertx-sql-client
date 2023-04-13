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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;

import java.util.Map;
import java.util.function.Predicate;

public class DB2ConnectionFactory extends ConnectionFactoryBase<DB2ConnectOptions> {

  public DB2ConnectionFactory(VertxInternal vertx, CloseFuture closeFuture, boolean oneShot) {
    super(vertx, closeFuture, oneShot);
  }

  @Override
  protected Future<Connection> doConnectInternal(DB2ConnectOptions options, EventLoopContext context) {
    SocketAddress server = options.getSocketAddress();
    boolean cachePreparedStatements = options.getCachePreparedStatements();
    int preparedStatementCacheSize = options.getPreparedStatementCacheMaxSize();
    Predicate<String> preparedStatementCacheSqlFilter = options.getPreparedStatementCacheSqlFilter();
    String username = options.getUser();
    String password = options.getPassword();
    String database = options.getDatabase();
    Map<String, String> properties = options.getProperties();
    int pipeliningLimit = options.getPipeliningLimit();
    NetClient netClient = netClient(options);
    return netClient.connect(server).flatMap(so -> {
      VertxMetrics vertxMetrics = vertx.metricsSPI();
      ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(options.getSocketAddress(), "sql", options.getMetricsName()) : null;
      DB2SocketConnection conn = new DB2SocketConnection((NetSocketInternal) so, metrics, options, cachePreparedStatements,
        preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
      conn.init();
      return Future.future(p -> conn.sendStartupMessage(username, password, database, properties, p));
    });
  }

  @Override
  public Future<SqlConnection> connect(Context context, DB2ConnectOptions options) {
    ContextInternal contextInternal = (ContextInternal) context;
    Promise<SqlConnection> promise = contextInternal.promise();
    connect(asEventLoopContext(contextInternal), options)
      .map(conn -> {
        CloseFuture connectionCloseFuture = connectionCloseFuture();
        DB2ConnectionImpl db2Connection = new DB2ConnectionImpl(contextInternal, this, conn, connectionCloseFuture);
        conn.init(db2Connection);
        return (SqlConnection) db2Connection;
      }).onComplete(promise);
    return promise.future();
  }
}

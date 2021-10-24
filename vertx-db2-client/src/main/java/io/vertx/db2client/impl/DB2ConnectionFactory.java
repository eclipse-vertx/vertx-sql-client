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
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class DB2ConnectionFactory extends ConnectionFactoryBase {

  private int pipeliningLimit;

  public DB2ConnectionFactory(VertxInternal vertx, DB2ConnectOptions options) {
    super(vertx, options);
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions connectOptions) {
    DB2ConnectOptions options = (DB2ConnectOptions) connectOptions;
    this.pipeliningLimit = options.getPipeliningLimit();
  }

  @Override
  protected void configureNetClientOptions(NetClientOptions netClientOptions) {
    // currently no-op
  }

  @Override
  protected Future<Connection> doConnectInternal(SocketAddress server, String username, String password, String database, EventLoopContext context) {
    return netClient.connect(server).flatMap(so -> {
      DB2SocketConnection conn = new DB2SocketConnection((NetSocketInternal) so, cachePreparedStatements,
        preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
      conn.init();
      return Future.future(p -> conn.sendStartupMessage(username, password, database, properties, p));
    });
  }

  @Override
  public Future<SqlConnection> connect(Context context) {
    ContextInternal contextInternal = (ContextInternal) context;
    QueryTracer tracer = contextInternal.tracer() == null ? null : new QueryTracer(contextInternal.tracer(), options);
    Promise<SqlConnection> promise = contextInternal.promise();
    connect(asEventLoopContext(contextInternal))
      .map(conn -> {
        DB2ConnectionImpl db2Connection = new DB2ConnectionImpl(contextInternal, this, conn, tracer, null);
        conn.init(db2Connection);
        return (SqlConnection)db2Connection;
      }).onComplete(promise);
    return promise.future();
  }
}

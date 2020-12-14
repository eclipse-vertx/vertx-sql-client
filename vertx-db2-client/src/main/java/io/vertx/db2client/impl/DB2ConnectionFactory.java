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
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.SqlConnectionFactoryBase;

public class DB2ConnectionFactory extends SqlConnectionFactoryBase implements ConnectionFactory {

  private int pipeliningLimit;

  public DB2ConnectionFactory(EventLoopContext context, DB2ConnectOptions options) {
    super(context, options);
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
  protected void doConnectInternal(Promise<Connection> promise) {
    Future<NetSocket> fut = netClient.connect(socketAddress);
    fut.onComplete(ar -> {
      if (ar.succeeded()) {
        NetSocket so = ar.result();
        DB2SocketConnection conn = new DB2SocketConnection((NetSocketInternal) so, cachePreparedStatements,
          preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
        conn.init();
        conn.sendStartupMessage(username, password, database, properties, promise);
      } else {
        promise.fail(ar.cause());
      }
    });
  }
}

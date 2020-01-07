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

import java.util.Collections;
import java.util.Map;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.sqlclient.impl.Connection;

public class DB2ConnectionFactory {
    private final NetClient netClient;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String database;
    private final Map<String, String> connectionAttributes;
    private final boolean cachePreparedStatements;
    private final int preparedStatementCacheSize;
    private final int preparedStatementCacheSqlLimit;
    
    public DB2ConnectionFactory(Vertx vertx, DB2ConnectOptions options) {
        NetClientOptions netClientOptions = new NetClientOptions(options);

        this.host = options.getHost();
        this.port = options.getPort();
        this.username = options.getUser();
        this.password = options.getPassword();
        this.database = options.getDatabase();
        this.connectionAttributes = options.getProperties() == null ? null : Collections.unmodifiableMap(options.getProperties());

        this.cachePreparedStatements = options.getCachePreparedStatements();
        this.preparedStatementCacheSize = options.getPreparedStatementCacheMaxSize();
        this.preparedStatementCacheSqlLimit = options.getPreparedStatementCacheSqlLimit();

        this.netClient = vertx.createNetClient(netClientOptions);
      }

    void close() {
      netClient.close();
    }

    public Future<Connection> connect(ContextInternal context) {
        Future<NetSocket> fut = netClient.connect(port, host);
        return fut
          .map(so -> {
              DB2SocketConnection conn = new DB2SocketConnection((NetSocketInternal) so, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlLimit, context);
            conn.init();
            return conn;
          })
          .flatMap(conn -> Future.future(p -> {
              conn.sendStartupMessage(username, password, database, connectionAttributes, p);
          }));
      }
}

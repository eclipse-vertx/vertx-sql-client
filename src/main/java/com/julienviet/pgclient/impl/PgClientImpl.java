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

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgClient;
import com.julienviet.pgclient.PgClientOptions;
import com.julienviet.pgclient.PgConnection;
import com.julienviet.pgclient.PgPool;
import com.julienviet.pgclient.PgPoolOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgClientImpl implements PgClient {

  final NetClient client;
  final VertxInternal vertx;
  final String host;
  final int port;
  final boolean ssl;
  final String database;
  final String username;
  final String password;
  final boolean cachePreparedStatements;
  final int pipeliningLimit;
  final int writeBatchSize;

  public PgClientImpl(Vertx vertx, PgClientOptions options) {

    NetClientOptions netClientOptions = new NetClientOptions(options);

    // Make sure ssl=false as we will use STARTLS
    netClientOptions.setSsl(false);

    this.ssl = options.isSsl();
    this.host = options.getHost();
    this.port = options.getPort();
    this.database = options.getDatabase();
    this.username = options.getUsername();
    this.password = options.getPassword();
    this.vertx = (VertxInternal) vertx;
    this.client = vertx.createNetClient(netClientOptions);
    this.cachePreparedStatements = options.getCachePreparedStatements();
    this.pipeliningLimit = options.getPipeliningLimit();
    this.writeBatchSize = options.getWriteBatchSize();
  }

  @Override
  public void close() {
    client.close();
  }

  public void _connect(Handler<AsyncResult<Connection>> completionHandler) {
    client.connect(port, host, null, ar -> {
      if (ar.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar.result();
        SocketConnection conn = new SocketConnection(this, socket, vertx.getOrCreateContext());
        conn.init(username, password, database, completionHandler);
      } else {
        completionHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void connect(Handler<AsyncResult<PgConnection>> completionHandler) {
    _connect(ar ->
      completionHandler.handle(ar.map(conn -> {
        PgConnectionImpl p = new PgConnectionImpl(((SocketConnection)conn).context, conn);
        conn.init(p);
        return p;
      })))
    ;
  }

  @Override
  public PgPool createPool(PgPoolOptions options) {
    return new PgPoolImpl(vertx.getOrCreateContext(), this, options.getMaxSize(), options.getMode());
  }
}

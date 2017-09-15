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
import io.vertx.ext.sql.SQLConnection;

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

  public PgClientImpl(Vertx vertx, PgClientOptions options) {

    NetClientOptions netOptions = new NetClientOptions();

    this.ssl = options.isSsl();
    this.host = options.getHost();
    this.port = options.getPort();
    this.database = options.getDatabase();
    this.username = options.getUsername();
    this.password = options.getPassword();
    this.vertx = (VertxInternal) vertx;
    this.client = vertx.createNetClient(netOptions);
    this.cachePreparedStatements = options.getCachePreparedStatements();
    this.pipeliningLimit = options.getPipeliningLimit();
  }

  @Override
  public void close() {
    client.close();
  }

  @Override
  public void connect(Handler<AsyncResult<PgConnection>> completionHandler) {
    client.connect(port, host, null, ar1 -> {
      if (ar1.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar1.result();
        DbConnection conn = new DbConnection(this, socket, vertx.getOrCreateContext());
        conn.init(username, password, database, ar2 -> {
          if (ar2.succeeded()) {
            completionHandler.handle(Future.succeededFuture(new PgConnectionImpl(ar2.result(), cachePreparedStatements)));
          } else {
            completionHandler.handle(Future.failedFuture(ar2.cause()));
          }
        });
      } else {
        completionHandler.handle(Future.failedFuture(ar1.cause()));
      }
    });
  }

  @Override
  public PgClient getConnection(Handler<AsyncResult<SQLConnection>> handler) {
    client.connect(port, host, null, ar1 -> {
      if (ar1.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar1.result();
        DbConnection conn = new DbConnection(this, socket, vertx.getOrCreateContext());
        conn.init(username, password, database, ar2 -> {
          if (ar2.succeeded()) {
            handler.handle(Future.succeededFuture(new PostgresSQLConnection(ar2.result())));
          } else {
            handler.handle(Future.failedFuture(ar2.cause()));
          }
        });
      } else {
        handler.handle(Future.failedFuture(ar1.cause()));
      }
    });
    return this;
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException("Implement me");
  }

  @Override
  public PgPool createPool(PgPoolOptions options) {
    return new PgPoolImpl(this, options.getMaxSize(), options.getMode());
  }
}

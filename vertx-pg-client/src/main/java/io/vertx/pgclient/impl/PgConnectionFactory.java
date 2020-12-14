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

package io.vertx.pgclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.TrustOptions;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.SqlConnectionFactoryBase;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PgConnectionFactory extends SqlConnectionFactoryBase implements ConnectionFactory {

  private SslMode sslMode;
  private int pipeliningLimit;

  PgConnectionFactory(EventLoopContext context, PgConnectOptions options) {
    super(context, options);
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions connectOptions) {
    PgConnectOptions options = (PgConnectOptions) connectOptions;
    this.pipeliningLimit = options.getPipeliningLimit();
    this.sslMode = options.isUsingDomainSocket() ? SslMode.DISABLE : options.getSslMode();

    // check ssl mode here
    switch (sslMode) {
      case VERIFY_FULL:
        String hostnameVerificationAlgorithm = options.getHostnameVerificationAlgorithm();
        if (hostnameVerificationAlgorithm == null || hostnameVerificationAlgorithm.isEmpty()) {
          throw new IllegalArgumentException("Host verification algorithm must be specified under verify-full sslmode");
        }
      case VERIFY_CA:
        TrustOptions trustOptions = options.getTrustOptions();
        if (trustOptions == null) {
          throw new IllegalArgumentException("Trust options must be specified under verify-full or verify-ca sslmode");
        }
        break;
    }
  }

  @Override
  protected void configureNetClientOptions(NetClientOptions netClientOptions) {
    netClientOptions.setSsl(false);
  }

  @Override
  protected void doConnectInternal(Promise<Connection> promise) {
    doConnect().flatMap(conn -> {
      PgSocketConnection socket = (PgSocketConnection) conn;
      socket.init();
      return Future.<Connection>future(p -> socket.sendStartupMessage(username, password, database, properties, p))
        .map(conn);
    }).onComplete(promise);
  }

  public void cancelRequest(int processId, int secretKey, Handler<AsyncResult<Void>> handler) {
    doConnect().onComplete(ar -> {
      if (ar.succeeded()) {
        PgSocketConnection conn = (PgSocketConnection) ar.result();
        conn.sendCancelRequestMessage(processId, secretKey, handler);
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  private Future<Connection> doConnect() {
    Future<Connection> connFuture;
    switch (sslMode) {
      case DISABLE:
        connFuture = doConnect(false);
        break;
      case ALLOW:
        connFuture = doConnect(false).recover(err -> doConnect(true));
        break;
      case PREFER:
        connFuture = doConnect(true).recover(err -> doConnect(false));
        break;
      case REQUIRE:
      case VERIFY_CA:
      case VERIFY_FULL:
        connFuture = doConnect(true);
        break;
      default:
        return context.failedFuture(new IllegalArgumentException("Unsupported SSL mode"));
    }
    return connFuture;
  }

  private Future<Connection> doConnect(boolean ssl) {
    Future<NetSocket> soFut;
    try {
      soFut = netClient.connect(socketAddress, (String) null);
    } catch (Exception e) {
      // Client is closed
      return context.failedFuture(e);
    }
    Future<Connection> connFut = soFut.map(so -> newSocketConnection((NetSocketInternal) so));
    if (ssl && !socketAddress.isDomainSocket()) {
      // upgrade connection to SSL if needed
      connFut = connFut.flatMap(conn -> Future.future(p -> {
        PgSocketConnection socket = (PgSocketConnection) conn;
        socket.upgradeToSSLConnection(ar2 -> {
          if (ar2.succeeded()) {
            p.complete(conn);
          } else {
            p.fail(ar2.cause());
          }
        });
      }));
    }
    return connFut;
  }

  private PgSocketConnection newSocketConnection(NetSocketInternal socket) {
    return new PgSocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
  }
}

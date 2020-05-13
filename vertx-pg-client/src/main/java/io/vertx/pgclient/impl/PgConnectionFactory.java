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

import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.core.*;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.net.*;
import io.vertx.sqlclient.impl.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PgConnectionFactory implements ConnectionFactory {

  private final NetClient client;
  private final ContextInternal context;
  private final String host;
  private final int port;
  private final SslMode sslMode;
  private final TrustOptions trustOptions;
  private final String hostnameVerificationAlgorithm;
  private final String database;
  private final String username;
  private final String password;
  private final Map<String, String> properties;
  private final boolean cachePreparedStatements;
  private final int preparedStatementCacheSize;
  private final int preparedStatementCacheSqlLimit;
  private final int pipeliningLimit;
  private final boolean isUsingDomainSocket;

  PgConnectionFactory(VertxInternal vertx, ContextInternal context, PgConnectOptions options) {

    NetClientOptions netClientOptions = new NetClientOptions(options);

    // Make sure ssl=false as we will use STARTLS
    netClientOptions.setSsl(false);

    this.context = context;
    this.sslMode = options.getSslMode();
    this.hostnameVerificationAlgorithm = netClientOptions.getHostnameVerificationAlgorithm();
    this.trustOptions = netClientOptions.getTrustOptions();
    this.host = options.getHost();
    this.port = options.getPort();
    this.database = options.getDatabase();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.properties = new HashMap<>(options.getProperties());
    this.cachePreparedStatements = options.getCachePreparedStatements();
    this.pipeliningLimit = options.getPipeliningLimit();
    this.preparedStatementCacheSize = options.getPreparedStatementCacheMaxSize();
    this.preparedStatementCacheSqlLimit = options.getPreparedStatementCacheSqlLimit();
    this.isUsingDomainSocket = options.isUsingDomainSocket();
    this.client = vertx.createNetClient(netClientOptions);
  }

  public void close() {
    client.close();
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

  @Override
  public Future<Connection> connect() {
    return doConnect()
      .flatMap(conn -> {
        PgSocketConnection socket = (PgSocketConnection) conn;
        socket.init();
        return Future.<Connection>future(p -> socket.sendStartupMessage(username, password, database, properties, p))
          .map(conn);
      });
  }

  private Future<Connection> doConnect() {
    switch (sslMode) {
      case DISABLE:
        return doConnect( false);
      case ALLOW:
        return doConnect( false).recover(err -> doConnect( true));
      case PREFER:
        return doConnect( true).recover(err -> doConnect( false));
      case VERIFY_FULL:
        if (hostnameVerificationAlgorithm == null || hostnameVerificationAlgorithm.isEmpty()) {
          return context.failedFuture(new IllegalArgumentException("Host verification algorithm must be specified under verify-full sslmode"));
        }
      case VERIFY_CA:
        if (trustOptions == null) {
          return context.failedFuture(new IllegalArgumentException("Trust options must be specified under verify-full or verify-ca sslmode"));
        }
      case REQUIRE:
        return doConnect(true);
      default:
        return context.failedFuture(new IllegalArgumentException("Unsupported SSL mode"));
    }
  }

  private Future<Connection> doConnect(boolean ssl) {
    Promise<Connection> promise = context.promise();
    context.dispatch(null, v -> doConnect(ssl, promise));
    return promise.future();
  }

  private void doConnect(boolean ssl, Promise<Connection> promise) {
    SocketAddress socketAddress;
    if (!isUsingDomainSocket) {
      socketAddress = SocketAddress.inetSocketAddress(port, host);
    } else {
      socketAddress = SocketAddress.domainSocketAddress(host + "/.s.PGSQL." + port);
    }

    Future<NetSocket> soFut;
    try {
      soFut = client.connect(socketAddress, (String) null);
    } catch (Exception e) {
      // Client is closed
      promise.fail(e);
      return;
    }
    Future<Connection> connFut = soFut.map(so -> newSocketConnection((NetSocketInternal) so));
    if (ssl && !isUsingDomainSocket) {
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
    connFut.onComplete(promise);
  }

  private PgSocketConnection newSocketConnection(NetSocketInternal socket) {
    return new PgSocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlLimit, pipeliningLimit, context);
  }
}

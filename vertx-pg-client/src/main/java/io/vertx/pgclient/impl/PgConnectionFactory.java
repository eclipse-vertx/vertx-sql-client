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

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PgConnectionFactory {

  private final NetClient client;
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

  PgConnectionFactory(VertxInternal vertx, PgConnectOptions options) {

    NetClientOptions netClientOptions = new NetClientOptions(options);

    // Make sure ssl=false as we will use STARTLS
    netClientOptions.setSsl(false);

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

  void close() {
    client.close();
  }

  Future<Connection> connectAndInit(ContextInternal ctx) {
    return connect(ctx)
      .flatMap(conn -> {
        conn.init();
        return Future.<Connection>future(p -> conn.sendStartupMessage(username, password, database, properties, p))
          .map(conn);
      });
  }

  Future<PgSocketConnection> connect(ContextInternal ctx) {
    switch (sslMode) {
      case DISABLE:
        return doConnect(ctx, false);
      case ALLOW:
        return doConnect(ctx, false).recover(err -> doConnect(ctx, true));
      case PREFER:
        return doConnect(ctx, true).recover(err -> doConnect(ctx, false));
      case VERIFY_FULL:
        if (hostnameVerificationAlgorithm == null || hostnameVerificationAlgorithm.isEmpty()) {
          return ctx.failedFuture(new IllegalArgumentException("Host verification algorithm must be specified under verify-full sslmode"));
        }
      case VERIFY_CA:
        if (trustOptions == null) {
          return ctx.failedFuture(new IllegalArgumentException("Trust options must be specified under verify-full or verify-ca sslmode"));
        }
      case REQUIRE:
        return doConnect(ctx, true);
      default:
        return ctx.failedFuture(new IllegalArgumentException("Unsupported SSL mode"));
    }
  }

  private Future<PgSocketConnection> doConnect(ContextInternal ctx, boolean ssl) {
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
      return ctx.failedFuture(e);
    }
    Future<PgSocketConnection> connFut = soFut.map(so -> newSocketConnection(ctx, (NetSocketInternal) so));
    if (ssl && !isUsingDomainSocket) {
      // upgrade connection to SSL if needed
      return connFut.flatMap(conn -> Future.future(p -> {
        conn.upgradeToSSLConnection(ar2 -> {
          if (ar2.succeeded()) {
            p.complete(conn);
          } else {
            p.fail(ar2.cause());
          }
        });
      }));
    } else {
      return connFut;
    }
  }

  private PgSocketConnection newSocketConnection(ContextInternal ctx, NetSocketInternal socket) {
    return new PgSocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlLimit, pipeliningLimit, ctx);
  }
}

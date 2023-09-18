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
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.TrustOptions;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionFactory extends ConnectionFactoryBase<PgConnectOptions> {

  public PgConnectionFactory(VertxInternal context) {
    super(context);
  }

  private void checkSslMode(PgConnectOptions options) {
    switch (options.getSslMode()) {
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
  protected Future<Connection> doConnectInternal(PgConnectOptions options, ContextInternal context) {
    try {
      checkSslMode(options);
    } catch (Exception e) {
      return context.failedFuture(e);
    }
    String username = options.getUser();
    String password = options.getPassword();
    String database = options.getDatabase();
    SocketAddress server = options.getSocketAddress();
    Map<String, String> properties = options.getProperties() != null ? Collections.unmodifiableMap(options.getProperties()) : null;
    return doConnect(server, context, (PgConnectOptions) options).flatMap(conn -> {
      PgSocketConnection socket = (PgSocketConnection) conn;
      socket.init();
      return Future.<Connection>future(p -> socket.sendStartupMessage(username, password, database, properties, p))
        .map(conn);
    });
  }

  public void cancelRequest(PgConnectOptions options, int processId, int secretKey, Handler<AsyncResult<Void>> handler) {
    doConnect(options.getSocketAddress(), vertx.createEventLoopContext(), options).onComplete(ar -> {
      if (ar.succeeded()) {
        PgSocketConnection conn = (PgSocketConnection) ar.result();
        conn.sendCancelRequestMessage(processId, secretKey, handler);
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  private Future<Connection> doConnect(SocketAddress server, ContextInternal context, PgConnectOptions options) {
    SslMode sslMode = options.isUsingDomainSocket() ? SslMode.DISABLE : options.getSslMode();
    Future<Connection> connFuture;
    switch (sslMode) {
      case DISABLE:
        connFuture = doConnect(server, context, false, options);
        break;
      case ALLOW:
        connFuture = doConnect(server, context,false, options).recover(err -> doConnect(server, context,true, options));
        break;
      case PREFER:
        connFuture = doConnect(server, context,true, options).recover(err -> doConnect(server, context,false, options));
        break;
      case REQUIRE:
      case VERIFY_CA:
      case VERIFY_FULL:
        connFuture = doConnect(server, context, true, options);
        break;
      default:
        return context.failedFuture(new IllegalArgumentException("Unsupported SSL mode"));
    }
    return connFuture;
  }

  private Future<Connection> doConnect(SocketAddress server, ContextInternal context, boolean ssl, PgConnectOptions options) {
    Future<NetSocket> soFut;
    try {
      soFut = netClient(options).connect(server, (String) null);
    } catch (Exception e) {
      // Client is closed
      return context.failedFuture(e);
    }
    Future<Connection> connFut = soFut.map(so -> newSocketConnection(context, (NetSocketInternal) so, options));
    if (ssl && !server.isDomainSocket()) {
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

  @Override
  public Future<SqlConnection> connect(Context context, PgConnectOptions options) {
    ContextInternal contextInternal = (ContextInternal) context;
    if (options.isUsingDomainSocket() && !vertx.isNativeTransportEnabled()) {
      return contextInternal.failedFuture(new IllegalArgumentException(NATIVE_TRANSPORT_REQUIRED));
    }
    PromiseInternal<SqlConnection> promise = contextInternal.promise();
    connect(asEventLoopContext(contextInternal), options)
      .map(conn -> {
        PgConnectionImpl pgConn = new PgConnectionImpl(this, contextInternal, conn);
        conn.init(pgConn);
        return (SqlConnection)pgConn;
      })
      .onComplete(promise);
    return promise.future();
  }

  private PgSocketConnection newSocketConnection(ContextInternal context, NetSocketInternal socket, PgConnectOptions options) {
    boolean cachePreparedStatements = options.getCachePreparedStatements();
    int preparedStatementCacheMaxSize = options.getPreparedStatementCacheMaxSize();
    Predicate<String> preparedStatementCacheSqlFilter = options.getPreparedStatementCacheSqlFilter();
    int pipeliningLimit = options.getPipeliningLimit();
    boolean useLayer7Proxy = options.getUseLayer7Proxy();
    VertxMetrics vertxMetrics = vertx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(options.getSocketAddress(), "sql", options.getMetricsName()) : null;
    PgSocketConnection conn = new PgSocketConnection(socket, metrics, options, cachePreparedStatements, preparedStatementCacheMaxSize, preparedStatementCacheSqlFilter, pipeliningLimit, useLayer7Proxy, context);
    return conn;
  }
}

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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.net.*;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.internal.net.NetSocketInternal;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.internal.Connection;
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
        String hostnameVerificationAlgorithm = options.getSslOptions().getHostnameVerificationAlgorithm();
        if (hostnameVerificationAlgorithm == null || hostnameVerificationAlgorithm.isEmpty()) {
          throw new IllegalArgumentException("Host verification algorithm must be specified under verify-full sslmode");
        }
      case VERIFY_CA:
        TrustOptions trustOptions = options.getSslOptions().getTrustOptions();
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
    SocketAddress server = options.getSocketAddress();
    return connect(server, context, true, options);
  }

  public Future<Void> cancelRequest(PgConnectOptions options, int processId, int secretKey) {
    return connect(options.getSocketAddress(), vertx.createEventLoopContext(), false, options)
      .compose(conn -> {
        PgSocketConnection socket = (PgSocketConnection) conn;
        return socket.sendCancelRequestMessage(processId, secretKey);
      });
  }

  private Future<Connection> connect(SocketAddress server, ContextInternal context, boolean sendStartupMessage, PgConnectOptions options) {
    SslMode sslMode = options.isUsingDomainSocket() ? SslMode.DISABLE : options.getSslMode();
    ConnectOptions connectOptions = new ConnectOptions()
      .setRemoteAddress(server);
    Future<Connection> connFuture;
    switch (sslMode) {
      case DISABLE:
        connFuture = connect(connectOptions, context, false, sendStartupMessage, options);
        break;
      case ALLOW:
        connFuture = connect(connectOptions, context, false, sendStartupMessage, options).recover(err -> connect(connectOptions, context, true, sendStartupMessage, options));
        break;
      case PREFER:
        connFuture = connect(connectOptions, context, true, sendStartupMessage, options).recover(err -> connect(connectOptions, context, false, sendStartupMessage, options));
        break;
      case REQUIRE:
      case VERIFY_CA:
      case VERIFY_FULL:
        connFuture = connect(connectOptions, context, true, sendStartupMessage, options);
        break;
      default:
        return context.failedFuture(new IllegalArgumentException("Unsupported SSL mode"));
    }
    return connFuture;
  }

  private Future<Connection> connect(ConnectOptions connectOptions, ContextInternal context, boolean ssl, boolean sendStartupMessage, PgConnectOptions options) {
    Future<Connection> res = doConnect(connectOptions, context, ssl, options);
    if (sendStartupMessage) {
      return res.flatMap(conn -> {
        PgSocketConnection socket = (PgSocketConnection) conn;
        socket.init();
        String username = options.getUser();
        String password = options.getPassword();
        String database = options.getDatabase();
        Map<String, String> properties = options.getProperties() != null ? Collections.unmodifiableMap(options.getProperties()) : null;
        return socket.sendStartupMessage(username, password, database, properties);
      });
    } else {
      return res;
    }
  }

  private Future<Connection> doConnect(ConnectOptions connectOptions, ContextInternal context, boolean ssl, PgConnectOptions options) {
    Future<NetSocket> soFut;
    try {
      soFut = client.connect(connectOptions);
    } catch (Exception e) {
      // Client is closed
      return context.failedFuture(e);
    }
    Future<Connection> connFut = soFut.map(so -> newSocketConnection(context, (NetSocketInternal) so, options));
    if (ssl && !connectOptions.getRemoteAddress().isDomainSocket()) {
      // upgrade connection to SSL if needed
      connFut = connFut.flatMap(conn -> Future.future(p -> {
        PgSocketConnection socket = (PgSocketConnection) conn;
        ClientSSLOptions o = options.getSslOptions().copy();
        if (o.getHostnameVerificationAlgorithm() == null) {
          o.setHostnameVerificationAlgorithm("");
        }
        socket.upgradeToSSLConnection(o, ar2 -> {
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
  public Future<Connection> connect(Context context, PgConnectOptions options) {
    ContextInternal contextInternal = (ContextInternal) context;
    if (options.isUsingDomainSocket() && !vertx.isNativeTransportEnabled()) {
      return contextInternal.failedFuture(new IllegalArgumentException(NATIVE_TRANSPORT_REQUIRED));
    }
    PromiseInternal<Connection> promise = contextInternal.promise();
    connect(asEventLoopContext(contextInternal), options).onComplete(promise);
    return promise.future();
  }

  private PgSocketConnection newSocketConnection(ContextInternal context, NetSocketInternal socket, PgConnectOptions options) {
    boolean cachePreparedStatements = options.getCachePreparedStatements();
    int preparedStatementCacheMaxSize = options.getPreparedStatementCacheMaxSize();
    Predicate<String> preparedStatementCacheSqlFilter = options.getPreparedStatementCacheSqlFilter();
    int pipeliningLimit = options.getPipeliningLimit();
    boolean useLayer7Proxy = options.getUseLayer7Proxy();
    VertxMetrics vertxMetrics = vertx.metrics();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(options.getSocketAddress(), "sql", options.getMetricsName()) : null;
    PgSocketConnection conn = new PgSocketConnection(socket, metrics, options, cachePreparedStatements, preparedStatementCacheMaxSize, preparedStatementCacheSqlFilter, pipeliningLimit, useLayer7Proxy, context);
    return conn;
  }
}

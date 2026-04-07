/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.pgclient.impl;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.internal.net.NetSocketInternal;
import io.vertx.core.net.*;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.pgclient.SslNegotiation;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;
import io.vertx.sqlclient.spi.connection.Connection;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionFactory extends ConnectionFactoryBase<PgConnectOptions> {

  private static final List<String> PG_PROTOCOLS = List.of("postgresql");

  public PgConnectionFactory(VertxInternal vertx) {
    super(vertx);
  }

  public PgConnectionFactory(VertxInternal vertx, NetClientOptions transportOptions) {
    super(vertx, transportOptions);
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
    Future<Connection> res;
    if (ssl && !connectOptions.getRemoteAddress().isDomainSocket()) {
      ClientSSLOptions sslOptions = options.getSslOptions().copy();
      if (sslOptions.getHostnameVerificationAlgorithm() == null) {
        sslOptions.setHostnameVerificationAlgorithm("");
      }
      SslNegotiation sslNegotiation = options.getSslNegotiation();
      if (sslNegotiation == SslNegotiation.DIRECT) {
        sslOptions.setUseAlpn(true).setApplicationLayerProtocols(PG_PROTOCOLS);
        ConnectOptions opts = new ConnectOptions(connectOptions)
          .setSsl(true)
          .setSslOptions(sslOptions);
        res = doConnect(opts, context, options);
      } else {
        res = doConnect(connectOptions, context, options).flatMap(conn -> upgradeToSSLConnection(conn, sslOptions));
      }
    } else {
      res = doConnect(connectOptions, context, options);
    }
    if (sendStartupMessage) {
      res = res.flatMap(conn -> sendStartupMessage(conn, options));
    }
    return res;
  }

  private Future<Connection> doConnect(ConnectOptions connectOptions, ContextInternal context, PgConnectOptions options) {
    Future<NetSocket> soFut;
    try {
      soFut = client.connect(connectOptions);
    } catch (Exception e) {
      return context.failedFuture(e);
    }
    return soFut.map(so -> newSocketConnection(context, (NetSocketInternal) so, options));
  }

  private Future<Connection> upgradeToSSLConnection(Connection conn, ClientSSLOptions sslOptions) {
    PgSocketConnection socketConnection = (PgSocketConnection) conn;
    return Future.future(p -> {
      socketConnection.upgradeToSSLConnection(sslOptions, ar -> {
        if (ar.succeeded()) {
          p.complete(socketConnection);
        } else {
          p.fail(ar.cause());
        }
      });
    });
  }

  private Future<Connection> sendStartupMessage(Connection conn, PgConnectOptions options) {
    PgSocketConnection socketConnection = (PgSocketConnection) conn;
    socketConnection.init();
    String username = options.getUser();
    String password = options.getPassword();
    String database = options.getDatabase();
    Map<String, String> properties = options.getProperties() != null ? Collections.unmodifiableMap(options.getProperties()) : null;
    return socketConnection.sendStartupMessage(username, password, database, properties);
  }

  @Override
  public Future<Connection> connect(Context context, PgConnectOptions options) {
    ContextInternal contextInternal = (ContextInternal) context;
    if (options.isUsingDomainSocket() && !vertx.transport().supportsDomainSockets()) {
      return contextInternal.failedFuture(UDS_NOT_SUPPORTED);
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

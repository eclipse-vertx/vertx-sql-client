/*
 * Copyright (c) 2011-2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.internal.tls.SslContextManager;
import io.vertx.core.net.HostAndPort;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.internal.net.NetSocketInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;

import java.util.Map;

import static io.vertx.mssqlclient.impl.codec.EncryptionLevel.*;

public class MSSQLConnectionFactory extends ConnectionFactoryBase<MSSQLConnectOptions> {

  private final SslContextManager sslContextManager;

  public MSSQLConnectionFactory(VertxInternal vertx) {
    super(vertx);
    sslContextManager = new SslContextManager(SslContextManager.resolveEngineOptions(tcpOptions.getSslEngineOptions(), tcpOptions.isUseAlpn()));
  }

  @Override
  protected Future<Connection> doConnectInternal(MSSQLConnectOptions options, ContextInternal context) {
    return connectOrRedirect(options, context, 0);
  }

  private Future<Connection> connectOrRedirect(MSSQLConnectOptions options, ContextInternal context, int redirections) {
    if (redirections > 1) {
      return context.failedFuture("The client can be redirected only once");
    }
    SocketAddress server = options.getSocketAddress();
    boolean clientSslConfig = options.isSsl();
    // Always start unencrypted, the connection will be upgraded if client and server agree
    return client.connect(server)
      .map(so -> createSocketConnection(so, options, context))
      .compose(conn -> conn.sendPreLoginMessage(clientSslConfig)
        .compose(encryptionLevel -> login(conn, options, encryptionLevel, context))
      )
      .compose(connBase -> {
        MSSQLSocketConnection conn = (MSSQLSocketConnection) connBase;
        HostAndPort alternateServer = conn.getAlternateServer();
        if (alternateServer == null) {
          return context.succeededFuture(conn);
        }
        Promise<Void> closePromise = context.promise();
        conn.close(null, closePromise);
        return closePromise.future().transform(v -> {
          MSSQLConnectOptions connectOptions = new MSSQLConnectOptions(options)
            .setHost(alternateServer.host())
            .setPort(alternateServer.port());
          return connectOrRedirect(connectOptions, context, redirections + 1);
        });
      });
  }

  private MSSQLSocketConnection createSocketConnection(NetSocket so, MSSQLConnectOptions options, ContextInternal context) {
    VertxMetrics vertxMetrics = vertx.metrics();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(options.getSocketAddress(), "sql", options.getMetricsName()) : null;
    MSSQLSocketConnection conn = new MSSQLSocketConnection((NetSocketInternal) so, sslContextManager, metrics, options, false, 0, sql -> true, 1, context);
    conn.init();
    return conn;
  }

  private Future<Connection> login(MSSQLSocketConnection conn, MSSQLConnectOptions options, Byte encryptionLevel, ContextInternal context) {
    boolean clientSslConfig = options.isSsl();
    if (clientSslConfig && encryptionLevel != ENCRYPT_ON && encryptionLevel != ENCRYPT_REQ) {
      Promise<Void> closePromise = context.promise();
      conn.close(null, closePromise);
      return closePromise.future().transform(v -> context.failedFuture("The client is configured for encryption but the server does not support it"));
    }
    Future<Void> future;
    if (encryptionLevel != ENCRYPT_NOT_SUP) {
      // Start connection encryption ...
      future = conn.enableSsl(clientSslConfig, encryptionLevel, options);
    } else {
      // ... unless the client did not require encryption and the server does not support it
      future = context.succeededFuture();
    }
    String username = options.getUser();
    String password = options.getPassword();
    String database = options.getDatabase();
    Map<String, String> properties = options.getProperties();
    return future.compose(v -> conn.sendLoginMessage(username, password, database, properties));
  }

  @Override
  public Future<Connection> connect(Context context, MSSQLConnectOptions options) {
    ContextInternal ctx = (ContextInternal) context;
    Promise<Connection> promise = ctx.promise();
    connect(asEventLoopContext(ctx), options).onComplete(promise);
    return promise.future();
  }
}

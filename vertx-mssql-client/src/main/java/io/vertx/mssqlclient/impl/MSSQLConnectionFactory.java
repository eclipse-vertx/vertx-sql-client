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
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.*;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.impl.command.PreLoginResponse;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;

import java.util.Map;
import java.util.function.Supplier;

import static io.vertx.mssqlclient.impl.codec.EncryptionLevel.*;

public class MSSQLConnectionFactory extends ConnectionFactoryBase {

  public MSSQLConnectionFactory(VertxInternal vertx, Supplier<? extends Future<? extends SqlConnectOptions>> options) {
    super(vertx, options);
  }

  @Override
  protected Future<Connection> doConnectInternal(SqlConnectOptions options, ContextInternal context) {
    return connectOrRedirect(MSSQLConnectOptions.wrap(options), context, 0);
  }

  private Future<Connection> connectOrRedirect(MSSQLConnectOptions options, ContextInternal context, int redirections) {
    if (redirections > 1) {
      return context.failedFuture("The client can be redirected only once");
    }
    SocketAddress server = options.getSocketAddress();
    boolean clientSslConfig = options.isSsl();
    // Always start unencrypted, the connection will be upgraded if client and server agree
    NetClient netClient = netClient(new NetClientOptions(options).setSsl(false));
    return netClient.connect(server)
      .map(so -> createSocketConnection(so, options, context))
      .compose(conn -> conn.sendPreLoginMessage(clientSslConfig, options.getAccessToken() != null)
        .compose(preLoginResponse -> login(conn, options, preLoginResponse, context))
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
    VertxMetrics vertxMetrics = vertx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(options.getSocketAddress(), "sql", options.getMetricsName()) : null;
    MSSQLSocketConnection conn = new MSSQLSocketConnection((NetSocketInternal) so, metrics, options, false, 0, sql -> true, 1, context);
    conn.init();
    return conn;
  }

  private Future<Connection> login(MSSQLSocketConnection conn, MSSQLConnectOptions options, PreLoginResponse preLoginResponse, ContextInternal context) {
    Byte encryptionLevel = preLoginResponse.encryptionLevel();
    boolean clientSslConfig = options.isSsl();
    String accessToken = options.getAccessToken();

    if (accessToken != null) {
      String failure = validateFedAuth(options, encryptionLevel);
      if (failure != null) {
        Promise<Void> closePromise = context.promise();
        conn.close(null, closePromise);
        return closePromise.future().transform(v -> context.failedFuture(failure));
      }
    }

    if (clientSslConfig && encryptionLevel != ENCRYPT_ON && encryptionLevel != ENCRYPT_REQ) {
      Promise<Void> closePromise = context.promise();
      conn.close(null, closePromise);
      return closePromise.future().transform(v -> context.failedFuture("The client is configured for encryption but the server does not support it"));
    }
    Future<Void> future;
    if (encryptionLevel != ENCRYPT_NOT_SUP) {
      // Start connection encryption ...
      future = conn.enableSsl(clientSslConfig, encryptionLevel, (MSSQLConnectOptions) options);
    } else {
      // ... unless the client did not require encryption and the server does not support it
      future = context.succeededFuture();
    }
    // With federated authentication the token replaces the credentials: the user and password
    // fields must go on the wire empty, and MSSQLConnectOptions defaults the user to "sa".
    String username = accessToken == null ? options.getUser() : "";
    String password = accessToken == null ? options.getPassword() : "";
    String database = options.getDatabase();
    Map<String, String> properties = options.getProperties();
    boolean fedAuthEcho = accessToken != null && Boolean.TRUE.equals(preLoginResponse.fedAuthRequired());
    return future.compose(v -> conn.sendLoginMessage(username, password, database, properties, accessToken, fedAuthEcho));
  }

  /**
   * @return a description of why federated authentication cannot proceed, or {@code null} if it can
   */
  private static String validateFedAuth(MSSQLConnectOptions options, Byte encryptionLevel) {
    String password = options.getPassword();
    if (password != null && !password.isEmpty()) {
      return "Cannot set both a password and an accessToken on MSSQLConnectOptions: " +
        "Microsoft Entra ID token authentication does not use a password";
    }
    if (!options.isSsl()) {
      // enableSsl() turns on trustAll when the client did not ask for encryption, which would
      // hand the bearer token to whoever answers on the other end.
      return "Microsoft Entra ID token authentication requires setSsl(true): without it the driver " +
        "does not validate the server certificate, which would expose the access token to a man-in-the-middle";
    }
    if (encryptionLevel != null && encryptionLevel == ENCRYPT_NOT_SUP) {
      return "Microsoft Entra ID token authentication requires an encrypted login, " +
        "but the server reported that it does not support encryption";
    }
    return null;
  }

  @Override
  public Future<SqlConnection> connect(Context context, SqlConnectOptions options) {
    ContextInternal ctx = (ContextInternal) context;
    Promise<SqlConnection> promise = ctx.promise();
    connect(asEventLoopContext(ctx), options)
      .map(conn -> {
        MSSQLConnectionImpl msConn = new MSSQLConnectionImpl(ctx, this, conn);
        conn.init(msConn);
        return (SqlConnection)msConn;
      })
      .onComplete(promise);
    return promise.future();
  }
}

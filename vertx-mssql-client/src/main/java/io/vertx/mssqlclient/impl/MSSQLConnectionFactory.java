/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
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
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

import static io.vertx.mssqlclient.impl.codec.EncryptionLevel.*;

public class MSSQLConnectionFactory extends ConnectionFactoryBase {

  private final int desiredPacketSize;
  private final boolean clientConfigSsl;

  public MSSQLConnectionFactory(VertxInternal vertx, MSSQLConnectOptions options) {
    super(vertx, options);
    desiredPacketSize = options.getPacketSize();
    clientConfigSsl = options.isSsl();
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions options) {
    // currently no-op
  }

  @Override
  protected void configureNetClientOptions(NetClientOptions netClientOptions) {
    // Always start unencrypted, the connection will be upgraded if client and server agree
    netClientOptions.setSsl(false);
  }

  @Override
  protected Future<Connection> doConnectInternal(SocketAddress server, String username, String password, String database, EventLoopContext context) {
    return connectOrRedirect(server, username, password, database, context, 0);
  }

  private Future<Connection> connectOrRedirect(SocketAddress server, String username, String password, String database, EventLoopContext context, int redirections) {
    if (redirections > 1) {
      return context.failedFuture("The client can be redirected only once");
    }
    return netClient.connect(server)
      .map(so -> createSocketConnection(so, context))
      .compose(conn -> conn.sendPreLoginMessage(clientConfigSsl)
        .compose(encryptionLevel -> login(conn, username, password, database, encryptionLevel, context))
      )
      .compose(connBase -> {
        MSSQLSocketConnection conn = (MSSQLSocketConnection) connBase;
        SocketAddress alternateServer = conn.getAlternateServer();
        if (alternateServer == null) {
          return context.succeededFuture(conn);
        }
        Promise<Void> closePromise = context.promise();
        conn.close(null, closePromise);
        return closePromise.future().transform(v -> connectOrRedirect(alternateServer, username, password, database, context, redirections + 1));
      });
  }

  private MSSQLSocketConnection createSocketConnection(NetSocket so, EventLoopContext context) {
    MSSQLSocketConnection conn = new MSSQLSocketConnection((NetSocketInternal) so, desiredPacketSize, false, 0, sql -> true, 1, context);
    conn.init();
    return conn;
  }

  private Future<Connection> login(MSSQLSocketConnection conn, String username, String password, String database, Byte encryptionLevel, EventLoopContext context) {
    if (clientConfigSsl && encryptionLevel != ENCRYPT_ON && encryptionLevel != ENCRYPT_REQ) {
      Promise<Void> closePromise = context.promise();
      conn.close(null, closePromise);
      return closePromise.future().transform(v -> context.failedFuture("The client is configured for encryption but the server does not support it"));
    }
    Future<Void> future;
    if (encryptionLevel != ENCRYPT_NOT_SUP) {
      // Start connection encryption ...
      future = conn.enableSsl(clientConfigSsl, encryptionLevel, (MSSQLConnectOptions) options);
    } else {
      // ... unless the client did not require encryption and the server does not support it
      future = context.succeededFuture();
    }
    return future.compose(v -> conn.sendLoginMessage(username, password, database, properties));
  }

  @Override
  public Future<SqlConnection> connect(Context context) {
    ContextInternal ctx = (ContextInternal) context;
    QueryTracer tracer = ctx.tracer() == null ? null : new QueryTracer(ctx.tracer(), options);
    Promise<SqlConnection> promise = ctx.promise();
    connect(asEventLoopContext(ctx))
      .map(conn -> {
        MSSQLConnectionImpl msConn = new MSSQLConnectionImpl(ctx, this, conn, tracer, null);
        conn.init(msConn);
        return (SqlConnection)msConn;
      })
      .onComplete(promise);
    return promise.future();
  }
}

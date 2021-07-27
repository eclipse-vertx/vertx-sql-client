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

public class MSSQLConnectionFactory extends ConnectionFactoryBase {

  private final int desiredPacketSize;
  private final boolean ssl;

  public MSSQLConnectionFactory(VertxInternal vertx, MSSQLConnectOptions options) {
    super(vertx, options);
    desiredPacketSize = options.getPacketSize();
    ssl = options.isSsl();
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions options) {
    // currently no-op
  }

  @Override
  protected void configureNetClientOptions(NetClientOptions netClientOptions) {
    netClientOptions.setSsl(false);
  }

  @Override
  protected Future<Connection> doConnectInternal(SocketAddress server, String username, String password, String database, EventLoopContext context) {
    Future<NetSocket> fut = netClient.connect(server);
    return fut
      .map(so -> {
        MSSQLSocketConnection conn = new MSSQLSocketConnection((NetSocketInternal) so, desiredPacketSize, false, 0, sql -> true, 1, context);
        conn.init();
        return conn;
      })
      .compose(conn -> conn.sendPreLoginMessage(ssl).compose(encryptionLevel -> {
          // TODO inspect negociated encryption level
          return conn.sendLoginMessage(username, password, database, properties);
        })
      );
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

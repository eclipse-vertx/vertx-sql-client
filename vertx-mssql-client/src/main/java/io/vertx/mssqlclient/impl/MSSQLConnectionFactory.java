/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl;

import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.core.*;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.net.NetSocket;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionFactoryBase;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class MSSQLConnectionFactory extends SqlConnectionFactoryBase {

  private MSSQLConnectOptions options;

  public MSSQLConnectionFactory(VertxInternal vertx, MSSQLConnectOptions options) {
    super(vertx, options);

    this.options = options;
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions options) {
    // currently no-op
  }

  @Override
  protected void configureNetClientOptions(NetClientOptions netClientOptions) {
    // currently no-op
  }

  @Override
  protected void doConnectInternal(SocketAddress server, String username, String password, String database, Promise<Connection> promise) {
    PromiseInternal<Connection> promiseInternal = (PromiseInternal<Connection>) promise;
    EventLoopContext context = SqlConnectionFactoryBase.asEventLoopContext(promiseInternal.context());
    Future<NetSocket> fut = netClient.connect(server);
    fut.onComplete(ar -> {
      if (ar.succeeded()) {
        NetSocket so = ar.result();
        MSSQLSocketConnection conn = new MSSQLSocketConnection((NetSocketInternal) so, false, 0, sql -> true, 1, context);
        conn.init();
        conn.sendPreLoginMessage(false, preLogin -> {
          if (preLogin.succeeded()) {
            conn.sendLoginMessage(username, password, database, properties, promise);
          } else {
            promise.fail(preLogin.cause());
          }
        });
      } else {
        promise.fail(ar.cause());
      }
    });
  }

  @Override
  public Future<SqlConnection> connect(Context context) {
    ContextInternal ctx = (ContextInternal) context;
    QueryTracer tracer = ctx.tracer() == null ? null : new QueryTracer(ctx.tracer(), options);
    Promise<Connection> promise = ctx.promise();
    ctx.emit(promise, v -> connect(promise));
    return promise.future().map(conn -> {
      MSSQLConnectionImpl msConn = new MSSQLConnectionImpl(ctx, this, conn, tracer, null);
      conn.init(msConn);
      return msConn;
    });
  }
}

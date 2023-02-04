/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouseclient.binary.impl;

import io.vertx.clickhouseclient.binary.ClickhouseConstants;
import io.vertx.clickhouseclient.binary.ClickhouseBinaryConnectOptions;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import net.jpountz.lz4.LZ4Factory;

public class ClickhouseBinaryConnectionFactory extends ConnectionFactoryBase {
  public static final String LZ4_FASTEST_JAVA = "lz4_fastest_java";

  private final LZ4Factory lz4Factory;

  public ClickhouseBinaryConnectionFactory(VertxInternal vertx, ClickhouseBinaryConnectOptions options) {
    super(vertx, options);
    this.lz4Factory = lz4FactoryForName(options.getProperties().getOrDefault(ClickhouseConstants.OPTION_COMPRESSOR, LZ4_FASTEST_JAVA));
  }

  private LZ4Factory lz4FactoryForName(String name) {
    if ("lz4_native".equals(name)) {
      return LZ4Factory.nativeInstance();
    } else if ("lz4_fastest".equals(name)) {
      return LZ4Factory.fastestInstance();
    } else if (LZ4_FASTEST_JAVA.equals(name)) {
      return LZ4Factory.fastestJavaInstance();
    } else if ("lz4_safe".equals(name)) {
      return LZ4Factory.safeInstance();
    } else if ("lz4_unsafe".equals(name)) {
      return LZ4Factory.unsafeInstance();
    }
    return null;
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions connectOptions) {
  }

  @Override
  protected void configureNetClientOptions(NetClientOptions netClientOptions) {
    netClientOptions.setSsl(false);
  }

  @Override
  protected Future<Connection> doConnectInternal(SocketAddress server, String username, String password, String database, EventLoopContext context) {
    return doConnect(server, context).flatMap(conn -> {
      ClickhouseBinarySocketConnection socket = (ClickhouseBinarySocketConnection) conn;
      socket.init();
      return Future.<Connection>future(p -> socket.sendStartupMessage(username, password, database, properties, p))
        .map(conn);
    });
  }

  private Future<Connection> doConnect(SocketAddress server, EventLoopContext ctx) {
    Future<NetSocket> soFut;
    try {
      soFut = netClient.connect(server, (String) null);
    } catch (Exception e) {
      // Client is closed
      return ctx.failedFuture(e);
    }
    return soFut.map(so -> newSocketConnection(ctx, (NetSocketInternal) so));
  }

  @Override
  public Future<SqlConnection> connect(Context context) {
    ContextInternal contextInternal = (ContextInternal) context;
    PromiseInternal<SqlConnection> promise = contextInternal.promise();
    connect(asEventLoopContext(contextInternal))
      .map(conn -> {
        QueryTracer tracer = contextInternal.tracer() == null ? null : new QueryTracer(contextInternal.tracer(), options);
        ClickhouseBinaryConnectionImpl dbConn = new ClickhouseBinaryConnectionImpl(this, contextInternal, conn, tracer, null);
        conn.init(dbConn);
        return (SqlConnection)dbConn;
      })
      .onComplete(promise);
    return promise.future();
  }

  private ClickhouseBinarySocketConnection newSocketConnection(EventLoopContext ctx, NetSocketInternal socket) {
    return new ClickhouseBinarySocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize,
      preparedStatementCacheSqlFilter, ctx, lz4Factory);
  }
}

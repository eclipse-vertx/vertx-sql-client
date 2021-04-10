/*
 *
 *  * Copyright (c) 2021 Vladimir Vishnevsky
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnection;
import io.vertx.core.Future;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class ClickhouseNativeConnectionImpl extends SqlConnectionImpl<ClickhouseNativeConnectionImpl> implements ClickhouseNativeConnection {
  private final ClickhouseNativeConnectionFactory factory;

  public static Future<ClickhouseNativeConnection> connect(ContextInternal ctx, ClickhouseNativeConnectOptions options) {
    ClickhouseNativeConnectionFactory client;
    try {
      client = new ClickhouseNativeConnectionFactory(ConnectionFactory.asEventLoopContext(ctx), options);
    } catch (Exception e) {
      return ctx.failedFuture(e);
    }
    ctx.addCloseHook(client);
    QueryTracer tracer = ctx.tracer() == null ? null : new QueryTracer(ctx.tracer(), options);
    PromiseInternal<Connection> promise = ctx.promise();
    client.connect(promise);
    return promise.future().map(conn -> {
      ClickhouseNativeConnectionImpl mySQLConnection = new ClickhouseNativeConnectionImpl(client, ctx, conn, tracer, null);
      conn.init(mySQLConnection);
      return mySQLConnection;
    });
  }

  ClickhouseNativeConnectionImpl(ClickhouseNativeConnectionFactory factory, ContextInternal context, Connection conn, QueryTracer tracer, ClientMetrics metrics) {
    super(context, conn, tracer, metrics);
    this.factory = factory;
  }

  @Override
  public Future<Transaction> begin() {
    return Future.failedFuture(new UnsupportedOperationException());
  }
}

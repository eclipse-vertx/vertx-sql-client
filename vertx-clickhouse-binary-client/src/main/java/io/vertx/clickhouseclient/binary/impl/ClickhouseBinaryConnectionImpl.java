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

import io.vertx.clickhouseclient.binary.ClickhouseBinaryConnectOptions;
import io.vertx.clickhouseclient.binary.ClickhouseBinaryConnection;
import io.vertx.clickhouseclient.binary.spi.ClickhouseBinaryDriver;
import io.vertx.core.Future;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionBase;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;

public class ClickhouseBinaryConnectionImpl extends SqlConnectionBase<ClickhouseBinaryConnectionImpl> implements ClickhouseBinaryConnection {
  public static Future<ClickhouseBinaryConnection> connect(ContextInternal ctx, ClickhouseBinaryConnectOptions options) {
    ClickhouseBinaryConnectionFactory client;
    try {
      client = new ClickhouseBinaryConnectionFactory(ctx.owner(), options);
    } catch (Exception e) {
      return ctx.failedFuture(e);
    }
    ctx.addCloseHook(client);
    return (Future)client.connect(ctx);
  }

  ClickhouseBinaryConnectionImpl(ConnectionFactory factory, ContextInternal context, Connection conn, QueryTracer tracer, ClientMetrics metrics) {
    super(context, factory, conn, ClickhouseBinaryDriver.INSTANCE, tracer, metrics);
  }

  @Override
  public Future<Transaction> begin() {
    return Future.failedFuture(new UnsupportedOperationException());
  }
}

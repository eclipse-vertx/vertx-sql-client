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
package io.vertx.oracle.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.oracle.OracleConnectOptions;
import io.vertx.oracle.OracleConnection;
import io.vertx.oracle.impl.commands.PingCommand;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class OracleConnectionImpl extends SqlConnectionImpl<OracleConnectionImpl> implements OracleConnection {

  public static Future<OracleConnection> connect(ContextInternal ctx, OracleConnectOptions options) {
    // TODO Add support for domain socket
    //        if (options.isUsingDomainSocket() && !ctx.owner().isNativeTransportEnabled()) {
    //            return ctx.failedFuture("Native transport is not available");
    //        }

    OracleConnectionFactory client;
    try {
      client = new OracleConnectionFactory(ctx.owner(), options);
    } catch (Exception e) {
      return ctx.failedFuture(e);
    }
    ctx.addCloseHook(client);
    QueryTracer tracer = ctx.tracer() == null ? null : new QueryTracer(ctx.tracer(), options);
    PromiseInternal<Connection> promise = ctx.promise();
    client.connect(promise);
    return promise.future().map(conn -> {
      OracleConnectionImpl connection = new OracleConnectionImpl(client, ctx, conn, tracer, null);
      conn.init(connection);
      return connection;
    });
  }

  private final OracleConnectionFactory factory;

  public OracleConnectionImpl(OracleConnectionFactory factory, ContextInternal context, Connection conn,
    QueryTracer tracer, ClientMetrics metrics) {
    super(context, conn, tracer, metrics);
    this.factory = factory;
  }

  @Override
  public OracleConnection ping(Handler<AsyncResult<Integer>> handler) {
    Future<Integer> fut = ping();
    if (handler != null) {
      fut.onComplete(handler);
    }
    return this;
  }

  @Override
  public Future<Integer> ping() {
    return schedule(context, new PingCommand(factory.options()));
  }

}

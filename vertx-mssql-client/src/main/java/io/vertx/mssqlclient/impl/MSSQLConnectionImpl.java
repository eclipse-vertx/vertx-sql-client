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
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.mssqlclient.spi.MSSQLDriver;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionBase;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;

public class MSSQLConnectionImpl extends SqlConnectionBase<MSSQLConnectionImpl> implements MSSQLConnection {

  public MSSQLConnectionImpl(ContextInternal context, ConnectionFactory factory, Connection conn, QueryTracer tracer, ClientMetrics metrics) {
    super(context, factory, conn, MSSQLDriver.INSTANCE, tracer, metrics);
  }

  public static Future<MSSQLConnection> connect(Vertx vertx, MSSQLConnectOptions options) {
    ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();
    MSSQLConnectionFactory client = new MSSQLConnectionFactory(ctx.owner(), options);
    ctx.addCloseHook(client);
    return (Future)client.connect(ctx);
  }
}

/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient.impl;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.internal.ContextInternal;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.OracleConnection;
import io.vertx.oracleclient.spi.OracleDriver;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.internal.SqlConnectionBase;
import io.vertx.sqlclient.spi.ConnectionFactory;

public class OracleConnectionImpl extends SqlConnectionBase<OracleConnectionImpl> implements OracleConnection {

  public OracleConnectionImpl(ContextInternal context, ConnectionFactory factory, Connection conn) {
    super(context, factory, conn, OracleDriver.INSTANCE);
  }

  public static Future<OracleConnection> connect(Vertx vertx, OracleConnectOptions options) {
    ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();
    OracleConnectionFactory client = new OracleConnectionFactory(ctx.owner());
    return client.connect(ctx, options).map(conn -> {
      OracleConnectionImpl impl = new OracleConnectionImpl(ctx, client, conn);
      conn.init(impl);
      prepareForClose(ctx, impl);
      return impl;
    });
  }
}

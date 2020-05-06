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

import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

public class MSSQLConnectionImpl extends SqlConnectionImpl<MSSQLConnectionImpl> implements MSSQLConnection {
  private final MSSQLConnectionFactory factory;

  public MSSQLConnectionImpl(MSSQLConnectionFactory factory, Context context, Connection conn) {
    super(context, conn);
    this.factory = factory;
  }

  public static Future<MSSQLConnection> connect(Vertx vertx, MSSQLConnectOptions options) {
    ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();
    Promise<MSSQLConnection> promise = Promise.promise();
    MSSQLConnectionFactory client = new MSSQLConnectionFactory(vertx, ctx, options);
    ctx.runOnContext(v -> {
      client.connect()
        .<MSSQLConnection>map(conn -> {
          MSSQLConnectionImpl msConn = new MSSQLConnectionImpl(client, ctx, conn);
          conn.init(msConn);
          return msConn;
        }).onComplete(promise);
    });
    return promise.future();
  }

  @Override
  public void handleNotification(int processId, String channel, String payload) {
    throw new UnsupportedOperationException();
  }
}

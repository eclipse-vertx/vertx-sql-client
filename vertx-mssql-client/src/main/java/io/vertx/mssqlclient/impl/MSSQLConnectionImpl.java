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

package io.vertx.mssqlclient.impl;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.mssqlclient.MSSQLInfo;
import io.vertx.mssqlclient.spi.MSSQLDriver;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SingletonSupplier;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.SqlConnectionBase;
import io.vertx.sqlclient.spi.ConnectionFactory;

public class MSSQLConnectionImpl extends SqlConnectionBase<MSSQLConnectionImpl> implements MSSQLConnection {

  private volatile Handler<MSSQLInfo> infoHandler;

  public MSSQLConnectionImpl(ContextInternal context, ConnectionFactory factory, Connection conn, boolean oneShot) {
    super(context, factory, conn, MSSQLDriver.INSTANCE, oneShot);
  }

  public static Future<MSSQLConnection> connect(Vertx vertx, MSSQLConnectOptions options) {
    ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();
    MSSQLConnectionFactory client = new MSSQLConnectionFactory(ctx.owner(), SingletonSupplier.wrap(options), true);
    return (Future) client.connect(ctx);
  }

  @Override
  public void handleEvent(Object event) {
    Handler<MSSQLInfo> handler = infoHandler;
    MSSQLInfo info = (MSSQLInfo) event;
    if (handler != null) {
      handler.handle(info);
    } else {
      SocketConnectionBase.logger.warn(event);
    }
  }

  @Override
  public MSSQLConnection infoHandler(Handler<MSSQLInfo> handler) {
    infoHandler = handler;
    return this;
  }
}

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

package io.vertx.mysqlclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.SqlTracer;

public class MySQLPoolImpl extends PoolBase<MySQLPoolImpl> implements MySQLPool {

  public static MySQLPoolImpl create(ContextInternal context, boolean closeVertx, MySQLConnectOptions connectOptions, PoolOptions poolOptions) {
    SqlTracer tracer = context.tracer() == null ? null : new SqlTracer(context.tracer(), connectOptions);
    MySQLPoolImpl pool = new MySQLPoolImpl(context, new MySQLConnectionFactory(context, connectOptions), tracer, poolOptions);
    CloseFuture closeFuture = pool.closeFuture();
    if (closeVertx) {
      closeFuture.onComplete(ar -> context.owner().close());
    } else {
      context.addCloseHook(closeFuture);
    }
    return pool;
  }

  private final MySQLConnectionFactory factory;

  private MySQLPoolImpl(ContextInternal context, MySQLConnectionFactory factory, SqlTracer tracer, PoolOptions poolOptions) {
    super(context, factory, tracer, poolOptions);
    this.factory = factory;
  }

  @Override
  public void connect(Handler<AsyncResult<Connection>> completionHandler) {
    factory.connect().onComplete(completionHandler);
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection conn) {
    return new MySQLConnectionImpl(factory, context, conn, tracer);
  }
}

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
import io.vertx.core.impl.ContextInternal;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.pool.ConnectionPool;

public class MySQLPoolImpl extends PoolBase<MySQLPoolImpl> implements MySQLPool {
  private final MySQLConnectionFactory factory;
  private final ConnectionPool pool;

  public MySQLPoolImpl(ContextInternal context, boolean closeVertx, MySQLConnectOptions connectOptions, PoolOptions poolOptions) {
    super(context.owner(), closeVertx);
    this.factory = new MySQLConnectionFactory(context.owner(), context, connectOptions);
    this.pool = new ConnectionPool(factory, context, poolOptions.getMaxSize(), poolOptions.getMaxWaitQueueSize());
  }

  @Override
  public void connect(Handler<AsyncResult<Connection>> completionHandler) {
    factory.connect().onComplete(completionHandler);
  }

  @Override
  public void acquire(Handler<AsyncResult<Connection>> completionHandler) {
    pool.acquire(completionHandler);
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection conn) {
    return new MySQLConnectionImpl(factory, context, conn);
  }

  @Override
  protected void doClose() {
    pool.close();
    factory.close();
    super.doClose();
  }
}

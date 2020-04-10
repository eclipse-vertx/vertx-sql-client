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

import io.vertx.core.Future;
import io.vertx.core.impl.ContextInternal;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLPool;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.pool.ConnectionPool;

import java.util.function.Function;

public class MSSQLPoolImpl extends PoolBase<MSSQLPoolImpl> implements MSSQLPool {
  private final MSSQLConnectionFactory connectionFactory;
  private final ConnectionPool pool;

  public MSSQLPoolImpl(ContextInternal context, boolean closeVertx, MSSQLConnectOptions connectOptions, PoolOptions poolOptions) {
    super(context.owner(), closeVertx);
    this.connectionFactory = new MSSQLConnectionFactory(context.owner(), context, connectOptions);
    this.pool = new ConnectionPool(connectionFactory, context, poolOptions.getMaxSize(), poolOptions.getMaxWaitQueueSize());
  }

  @Override
  public void connect(Handler<AsyncResult<Connection>> completionHandler) {
    connectionFactory.connect().onComplete(completionHandler);
  }

  @Override
  public void acquire(Handler<AsyncResult<Connection>> completionHandler) {
    pool.acquire(completionHandler);
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection connection) {
    return new MSSQLConnectionImpl(connectionFactory, context, connection);
  }

  @Override
  public <T> Future<T> withTransaction(Function<SqlClient, Future<T>> function) {
    return Future.failedFuture("Transaction is not supported for now");
  }

  @Override
  protected void doClose() {
    pool.close();
    connectionFactory.close();
    super.doClose();
  }
}

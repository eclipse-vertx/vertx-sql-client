/*
 * Copyright (C) 2019,2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.db2client.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

public class DB2PoolImpl extends PoolBase<DB2PoolImpl> implements DB2Pool {

  public static DB2PoolImpl create(ContextInternal context, boolean closeVertx, DB2ConnectOptions connectOptions,
                                   PoolOptions poolOptions) {
    DB2PoolImpl pool = new DB2PoolImpl(context, poolOptions, new DB2ConnectionFactory(context, connectOptions));
    CloseFuture closeFuture = pool.closeFuture();
    if (closeVertx) {
      closeFuture.onComplete(ar -> context.owner().close());
    } else {
      context.addCloseHook(closeFuture);
    }
    return pool;
  }

  private final DB2ConnectionFactory factory;

  private DB2PoolImpl(ContextInternal context, PoolOptions poolOptions, DB2ConnectionFactory factory) {
    super(context, factory, poolOptions);
    this.factory = factory;
  }

  @Override
  public void connect(Handler<AsyncResult<Connection>> completionHandler) {
    factory.connect().onComplete(completionHandler);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection conn) {
    return new DB2ConnectionImpl(factory, context, conn);
  }
}

/*
 * Copyright (C) 2017 Julien Viet
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
 *
 */

package io.vertx.pgclient.impl;

import io.vertx.pgclient.*;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.core.*;

/**
 * Todo :
 *
 * - handle timeout when acquiring a connection
 * - for per statement pooling, have several physical connection and use the less busy one to avoid head of line blocking effect
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class PgPoolImpl extends PoolBase<PgPoolImpl> {

  private final PgConnectionFactory factory;

  public PgPoolImpl(Context context, boolean closeVertx, PgPoolOptions options) {
    super(context, closeVertx, options);
    this.factory = new PgConnectionFactory(context, Vertx.currentContext() != null, options);
  }

  @Override
  public void connect(Handler<AsyncResult<Connection>> completionHandler) {
    factory.connectAndInit(completionHandler);
  }

  @Override
  protected SqlConnectionImpl wrap(Context context, Connection conn) {
    return new PgConnectionImpl(factory, context, conn);
  }

  @Override
  protected void doClose() {
    factory.close();
    super.doClose();
  }
}

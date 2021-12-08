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

import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.VertxInternal;
import io.vertx.db2client.DB2Pool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.impl.PoolBase;

public class DB2PoolImpl extends PoolBase<DB2PoolImpl> implements DB2Pool {

  public DB2PoolImpl(VertxInternal vertx, CloseFuture closeFuture, Pool delegate) {
    super(vertx, closeFuture, delegate);
  }
}

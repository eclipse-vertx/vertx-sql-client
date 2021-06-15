/*
 * Copyright (C) 2020 IBM Corporation
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
package io.vertx.db2client.spi;

import io.vertx.core.Vertx;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Pool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolConfig;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.Driver;

public class DB2Driver implements Driver {

  @Override
  public Pool createPool(PoolConfig config) {
    return DB2Pool.pool(DB2ConnectOptions.wrap(config.determineConnectOptions()), config.options());
  }

  @Override
  public Pool createPool(Vertx vertx, PoolConfig config) {
    return DB2Pool.pool(vertx, DB2ConnectOptions.wrap(config.determineConnectOptions()), config.options());
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof DB2ConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }
}

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
 *
 */
package io.vertx.sqlclient.spi;

import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolConfig;
import io.vertx.sqlclient.SqlConnectOptions;

/**
 * An entry point to the Vertx Reactive SQL Client
 * Every driver must implement this interface.
 */
public interface Driver {

  /**
   * Create a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param config the pool config for creating the pool
   * @return the connection pool
   */
  Pool createPool(PoolConfig config);

  /**
   * Create a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param vertx the Vertx instance to be used with the connection pool
   * @param config the pool config for creating the pool
   * @return the connection pool
   */
  Pool createPool(Vertx vertx, PoolConfig config);

  /**
   * @return true if the driver accepts the {@code connectOptions}, false otherwise
   */
  boolean acceptsOptions(SqlConnectOptions connectOptions);

}

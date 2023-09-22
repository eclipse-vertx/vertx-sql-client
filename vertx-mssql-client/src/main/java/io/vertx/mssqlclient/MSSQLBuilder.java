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

package io.vertx.mssqlclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.mssqlclient.spi.MSSQLDriver;
import io.vertx.sqlclient.ClientBuilder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.ClientBuilderBase;

/**
 * Entry point for building MSSQL clients.
 */
@VertxGen
public interface MSSQLBuilder {

  /**
   * Build a pool with the specified {@code block} argument.
   * The {@code block} argument is usually a lambda that configures the provided builder
   * <p>
   * Example usage: {@code Pool pool = PgBuilder.pool(builder -> builder.connectingTo(connectOptions));}
   *
   * @return the pool as configured by the code {@code block}
   */
  static Pool pool(Handler<ClientBuilder<Pool>> block) {
    return ClientBuilder.pool(MSSQLDriver.INSTANCE, block);
  }

  /**
   * Provide a builder for MSSQL pool of connections
   * <p>
   * Example usage: {@code Pool pool = PgBuilder.pool().connectingTo(connectOptions).build()}
   */
  static ClientBuilder<Pool> pool() {
    return ClientBuilder.pool(MSSQLDriver.INSTANCE);
  }
}

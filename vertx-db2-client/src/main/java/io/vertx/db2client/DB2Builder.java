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

package io.vertx.db2client;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.db2client.impl.Db2PoolOptions;
import io.vertx.db2client.spi.DB2Driver;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.ClientBuilderBase;

import java.util.function.Supplier;

/**
 * Entry point for building DB2 clients.
 */
@VertxGen
public interface DB2Builder {

  /**
   * Build a pool with the specified {@code block} argument.
   * The {@code block} argument is usually a lambda that configures the provided builder
   * <p>
   * Example usage: {@code Pool pool = PgBuilder.pool(builder -> builder.connectingTo(connectOptions));}
   *
   * @return the pool as configured by the code {@code block}
   */
  static Pool pool(Handler<ClientBuilder<Pool>> block) {
    return ClientBuilder.pool(DB2Driver.INSTANCE, block);
  }

  /**
   * Provide a builder for DB2 pool of connections
   * <p>
   * Example usage: {@code Pool pool = PgBuilder.pool().connectingTo(connectOptions).build()}
   */
  static ClientBuilder<Pool> pool() {
    return ClientBuilder.pool(DB2Driver.INSTANCE);
  }

  /**
   * Build a client backed by a connection pool with the specified {@code block} argument.
   * The {@code block} argument is usually a lambda that configures the provided builder
   * <p>
   * Example usage: {@code SqlClient client = PgBuilder.client(builder -> builder.connectingTo(connectOptions));}
   *
   * @return the client as configured by the code {@code block}
   */
  static SqlClient client(Handler<ClientBuilder<SqlClient>> handler) {
    ClientBuilder<SqlClient> builder = client();
    handler.handle(builder);
    return builder.build();
  }

  /**
   * Provide a builder for DB2 client backed by a connection pool.
   * <p>
   * Example usage: {@code SqlClient client = PgBuilder.client().connectingTo(connectOptions).build()}
   */
  static ClientBuilder<SqlClient> client() {
    return new ClientBuilderBase<SqlClient>(DB2Driver.INSTANCE) {
      @Override
      public ClientBuilder<SqlClient> with(PoolOptions options) {
        if (options != null) {
          options = new Db2PoolOptions(options).setPipelined(true);
        }
        return super.with(options);
      }

      @Override
      protected SqlClient create(Vertx vertx, Supplier<Future<SqlConnectOptions>> databases, PoolOptions poolOptions) {
        return driver.createPool(vertx, databases, poolOptions);
      }
    };
  }
}

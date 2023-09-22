/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.sqlclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.impl.ClientBuilderBase;
import io.vertx.sqlclient.spi.Driver;

import java.util.List;
import java.util.function.Supplier;

/**
 * Builder for {@link SqlClient} instances.
 */
@VertxGen
public interface ClientBuilder<C> {

  /**
   * Provide a builder for a pool of connections for the specified {@link Driver}
   * <p>
   * Example usage: {@code Pool pool = ClientBuilder.pool(driver).connectingTo(connectOptions).build()}
   */
  @GenIgnore
  static ClientBuilder<Pool> pool(Driver driver) {
    return new ClientBuilderBase<Pool>(driver) {
      @Override
      protected Pool create(Vertx vertx, Supplier<Future<SqlConnectOptions>> databases, PoolOptions poolOptions) {
        return driver.createPool(vertx, databases, poolOptions);
      }
    };
  }

  /**
   * Build a pool with the specified {@code block} argument and {@link Driver}
   * The {@code block} argument is usually a lambda that configures the provided builder
   * <p>
   * Example usage: {@code Pool pool = ClientBuilder.pool(driver, builder -> builder.connectingTo(connectOptions));}
   *
   * @return the pool as configured by the code {@code block}
   */
  @GenIgnore
  static Pool pool(Driver driver, Handler<ClientBuilder<Pool>> block) {
    ClientBuilder<Pool> builder = pool(driver);
    block.handle(builder);
    return builder.build();
  }

  /**
   * Configure the client with the given pool {@code options}
   * @param options the pool options
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  ClientBuilder<C> with(PoolOptions options);

  /**
   * Configure the {@code database} the client should connect to. The target {@code database} is specified as
   * a {@link SqlConnectOptions} coordinates.
   * @param database the database coordinates
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  ClientBuilder<C> connectingTo(SqlConnectOptions database);

  /**
   * Configure the {@code database} the client should connect to. The target {@code database} is specified as
   * a {@link SqlConnectOptions} coordinates.
   * @param database the database URI
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  ClientBuilder<C> connectingTo(String database);

  /**
   * Configure the {@code database} the client should connect to. When the client needs to connect to the database,
   * it gets fresh database configuration from the database {@code supplier}.
   * @param supplier the supplier of database coordinates
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  @Fluent
  ClientBuilder<C> connectingTo(Supplier<Future<SqlConnectOptions>> supplier);

  /**
   * Configure the {@code database} the client should connect to. When the client needs to connect to the database,
   * it gets a database configuration from the list of  {@code databases} using a round-robin policy.
   * @param databases the list of database coordinates
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  ClientBuilder<C> connectingTo(List<SqlConnectOptions> databases);

  /**
   * Sets the vertx instance to use.
   * @param vertx the vertx instance
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  ClientBuilder<C> using(Vertx vertx);

  /**
   * Set a handler called when the pool has established a connection to the database.
   *
   * <p> This handler allows interactions with the database before the connection is added to the pool.
   *
   * <p> When the handler has finished, it must call {@link SqlConnection#close()} to release the connection
   * to the pool.
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  ClientBuilder<C> withConnectHandler(Handler<SqlConnection> handler);

  /**
   * Build and return the client.
   * @return the client
   */
  C build();

}

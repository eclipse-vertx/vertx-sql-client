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

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.function.Supplier;

/**
 * Builder for {@link SqlClient} instances.
 */
@VertxGen
public interface ClientBuilder<C> {

  /**
   * Configure the client pool with the given {@code options.
   * @param options the pool options
   * @return a reference to this, so the API can be used fluently
   */
  ClientBuilder<C> config(PoolOptions options);

  /**
   * Configure the {@code database} the client should connect to. The target {@code database} is specified as
   * a {@link SqlConnectOptions} coordinates.
   * @param database the database coordinates
   * @return a reference to this, so the API can be used fluently
   */
  ClientBuilder<C> connectingTo(SqlConnectOptions database);

  /**
   * Configure the {@code database} the client should connect to. When the client needs to connect to the database,
   * it gets fresh database configuration from the database {@code supplier}.
   * @param supplier the supplier of database coordinates
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  ClientBuilder<C> connectingTo(Supplier<Future<SqlConnectOptions>> supplier);

  /**
   * Configure the {@code database} the client should connect to. When the client needs to connect to the database,
   * it gets a database configuration from the list of  {@code databases} using a round-robin policy.
   * @param databases the list of database coordinates
   * @return a reference to this, so the API can be used fluently
   */
  ClientBuilder<C> connectingTo(List<SqlConnectOptions> databases);

  /**
   * Sets the vertx instance to use.
   * @param vertx the vertx instance
   * @return a reference to this, so the API can be used fluently
   */
  ClientBuilder<C> using(Vertx vertx);

  /**
   * Build and return the client.
   * @return the client
   */
  C build();

}

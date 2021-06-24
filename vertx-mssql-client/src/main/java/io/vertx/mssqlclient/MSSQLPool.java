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

package io.vertx.mssqlclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mssqlclient.spi.MSSQLDriver;
import io.vertx.sqlclient.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static io.vertx.mssqlclient.MSSQLConnectOptions.fromUri;

/**
 * A {@link Pool pool} of {@link MSSQLConnection SQL Server connections}.
 */
@VertxGen
public interface MSSQLPool extends Pool {

  /**
   * Like {@link #pool(String, PoolOptions)} with default options.
   */
  static MSSQLPool pool(String connectionUri) {
    return pool(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #pool(MSSQLConnectOptions, PoolOptions)} with {@code database} built from {@code connectionUri}.
   */
  static MSSQLPool pool(String connectionUri, PoolOptions options) {
    return pool(fromUri(connectionUri), options);
  }

  /**
   * Like {@link #pool(Vertx, String, PoolOptions)} with default options.
   */
  static MSSQLPool pool(Vertx vertx, String connectionUri) {
    return pool(vertx, fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #pool(Vertx, MSSQLConnectOptions, PoolOptions)} with {@code database} built from {@code connectionUri}.
   */
  static MSSQLPool pool(Vertx vertx, String connectionUri, PoolOptions options) {
    return pool(vertx, fromUri(connectionUri), options);
  }

  /**
   * Create a connection pool to the SQL server {@code database} configured with the given {@code options}.
   *
   * @param database the options for the connection
   * @param options the options for creating the pool
   * @return the connection pool
   */
  static MSSQLPool pool(MSSQLConnectOptions database, PoolOptions options) {
    return pool(null, database, options);
  }

  /**
   * Like {@link #pool(MSSQLConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static MSSQLPool pool(Vertx vertx, MSSQLConnectOptions database, PoolOptions options) {
    return pool(vertx, Collections.singletonList(database), options);
  }

  /**
   * Create a connection pool to the SQL Server {@code databases} with
   * round-robin selection.
   *
   * @param databases the list of databases
   * @param options the options for creating the pool
   * @return the connection pool
   */
  static MSSQLPool pool(List<MSSQLConnectOptions> databases, PoolOptions options) {
    return pool(null, databases, options);
  }

  /**
   * Like {@link #pool(List, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static MSSQLPool pool(Vertx vertx, List<MSSQLConnectOptions> databases, PoolOptions options) {
    return new MSSQLDriver().createPool(vertx, databases, options);
  }

  @Override
  MSSQLPool connectHandler(Handler<SqlConnection> handler);

  @Fluent
  MSSQLPool connectionProvider(Function<Context, Future<SqlConnection>> provider);
}

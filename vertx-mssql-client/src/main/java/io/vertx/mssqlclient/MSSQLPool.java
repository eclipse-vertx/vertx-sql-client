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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.impl.VertxInternal;
import io.vertx.mssqlclient.impl.MSSQLPoolImpl;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.sqlclient.*;

import static io.vertx.mssqlclient.MSSQLConnectOptions.fromUri;

/**
 * A {@link Pool pool} of {@link MSSQLConnection SQL Server connections}.
 */
@VertxGen
public interface MSSQLPool extends Pool {

  /**
   * Like {@link #pool(String, PoolOptions)} with a default {@code poolOptions}.
   */
  static MSSQLPool pool(String connectionUri) {
    return pool(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #pool(MSSQLConnectOptions, PoolOptions)} with {@code connectOptions} built from {@code connectionUri}.
   */
  static MSSQLPool pool(String connectionUri, PoolOptions poolOptions) {
    return pool(fromUri(connectionUri), poolOptions);
  }

  /**
   * Like {@link #pool(Vertx, String, PoolOptions)} with a default {@code poolOptions}..
   */
  static MSSQLPool pool(Vertx vertx, String connectionUri) {
    return pool(vertx, fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #pool(Vertx, MSSQLConnectOptions, PoolOptions)} with {@code connectOptions} built from {@code connectionUri}.
   */
  static MSSQLPool pool(Vertx vertx, String connectionUri, PoolOptions poolOptions) {
    return pool(vertx, fromUri(connectionUri), poolOptions);
  }

  /**
   * Create a connection pool to the SQL server configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param connectOptions the options for the connection
   * @param poolOptions the options for creating the pool
   * @return the connection pool
   */
  static MSSQLPool pool(MSSQLConnectOptions connectOptions, PoolOptions poolOptions) {
    return pool(null, connectOptions, poolOptions);
  }

  /**
   * Like {@link #pool(MSSQLConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static MSSQLPool pool(Vertx vertx, MSSQLConnectOptions connectOptions, PoolOptions poolOptions) {
    return pool(vertx, PoolConfig.create(poolOptions).connectOptions(connectOptions));
  }

  /**
   * Create a connection pool to the SQL server configured with the given {@code config}.
   *
   * @param config the pool configuration
   * @return the connection pool
   */
  static MSSQLPool pool(PoolConfig config) {
    return MSSQLPoolImpl.create(null, config);
  }

  /**
   * Like {@link #pool(PoolConfig)} with a specific {@link Vertx} instance.
   */
  static MSSQLPool pool(Vertx vertx, PoolConfig config) {
    return MSSQLPoolImpl.create((VertxInternal) vertx, config);
  }
}

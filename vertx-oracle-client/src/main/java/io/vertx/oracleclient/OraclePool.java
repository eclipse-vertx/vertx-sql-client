/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.oracleclient.spi.OracleDriver;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.SingletonSupplier;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a pool of connection to interact with an Oracle database.
 */
@VertxGen
public interface OraclePool extends Pool {

  static OraclePool pool(OracleConnectOptions connectOptions, PoolOptions poolOptions) {
    return pool(null, connectOptions, poolOptions);
  }

  /**
   * Like {@link #pool(OracleConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static OraclePool pool(Vertx vertx, OracleConnectOptions connectOptions, PoolOptions poolOptions) {
    return (OraclePool) OracleDriver.INSTANCE.createPool(vertx, Collections.singletonList(connectOptions), poolOptions);
  }

  /**
   * Like {@link #pool(OracleConnectOptions, PoolOptions)} but connection options are created from the provided {@code connectionUri}.
   */
  static OraclePool pool(String connectionUri, PoolOptions poolOptions) {
    return pool(OracleConnectOptions.fromUri(connectionUri), poolOptions);
  }

  /**
   * Like {@link #pool(String, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static OraclePool pool(Vertx vertx, String connectionUri, PoolOptions poolOptions) {
    return pool(vertx, OracleConnectOptions.fromUri(connectionUri), poolOptions);
  }

  /**
   * Create a connection pool to the Oracle {@code databases}. The supplier is called
   * to provide the options when a new connection is created by the pool.
   *
   * @param databases the databases supplier
   * @param poolOptions the options for creating the pool
   * @return the connection pool
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static OraclePool pool(Supplier<Future<OracleConnectOptions>> databases, PoolOptions poolOptions) {
    return pool(null, databases, poolOptions);
  }


  /**
   * Like {@link #pool(Supplier, PoolOptions)} with a specific {@link Vertx} instance.
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static OraclePool pool(Vertx vertx, Supplier<Future<OracleConnectOptions>> databases, PoolOptions poolOptions) {
    return (OraclePool) OracleDriver.INSTANCE.createPool(vertx, databases, poolOptions);
  }

  @Override
  OraclePool connectHandler(Handler<SqlConnection> handler);

  @Override
  OraclePool connectionProvider(Function<Context, Future<SqlConnection>> provider);
}

/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.impl.MySQLPoolOptions;
import io.vertx.mysqlclient.spi.MySQLDriver;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static io.vertx.mysqlclient.MySQLConnectOptions.fromUri;

/**
 * A {@link Pool pool} of {@link MySQLConnection MySQL Connections}.
 */
@VertxGen
public interface MySQLPool extends Pool {

  /**
   * Like {@link #pool(String, PoolOptions)} with default options.
   */
  static MySQLPool pool(String connectionUri) {
    return pool(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #pool(MySQLConnectOptions, PoolOptions)} with {@code database} built from {@code connectionUri}.
   */
  static MySQLPool pool(String connectionUri, PoolOptions options) {
    return pool(fromUri(connectionUri), options);
  }

  /**
   * Like {@link #pool(Vertx, String, PoolOptions)} with default options.
   */
  static MySQLPool pool(Vertx vertx, String connectionUri) {
    return pool(vertx, fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #pool(Vertx, MySQLConnectOptions, PoolOptions)} with {@code database} built from {@code connectionUri}.
   */
  static MySQLPool pool(Vertx vertx, String connectionUri, PoolOptions options) {
    return pool(vertx, fromUri(connectionUri), options);
  }

  /**
   * Create a connection pool to the MySQL {@code server} configured with the given {@code options}.
   *
   * @param database the options for the connection
   * @param options the options for creating the pool
   * @return the connection pool
   */
  static MySQLPool pool(MySQLConnectOptions database, PoolOptions options) {
    return pool(null, database, options);
  }

  /**
   * Like {@link #pool(MySQLConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static MySQLPool pool(Vertx vertx, MySQLConnectOptions database, PoolOptions options) {
    return pool(vertx, Collections.singletonList(database), options);
  }

  /**
   * Create a connection pool to the MySQL {@code databases} with round-robin selection.
   * Round-robin is applied when a new connection is created by the pool.
   *
   * @param databases the list of servers
   * @param options the options for creating the pool
   * @return the connection pool
   */
  static MySQLPool pool(List<MySQLConnectOptions> databases, PoolOptions options) {
    return pool(null, databases, options);
  }

  /**
   * Like {@link #pool(List, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static MySQLPool pool(Vertx vertx, List<MySQLConnectOptions> databases, PoolOptions options) {
    return (MySQLPool) MySQLDriver.INSTANCE.createPool(vertx, databases, options);
  }


  /**
   * Like {@link #client(String, PoolOptions)} with a default {@code poolOptions}.
   */
  static SqlClient client(String connectionUri) {
    return client(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #client(MySQLConnectOptions, PoolOptions)} with {@code connectOptions} build from {@code connectionUri}.
   */
  static SqlClient client(String connectionUri, PoolOptions poolOptions) {
    return client(MySQLConnectOptions.fromUri(connectionUri), poolOptions);
  }

  /**
   * Like {@link #client(Vertx, String,PoolOptions)} with a default {@code poolOptions}.
   */
  static SqlClient client(Vertx vertx, String connectionUri) {
    return client(vertx, MySQLConnectOptions.fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #client(Vertx, MySQLConnectOptions, PoolOptions)} with {@code connectOptions} build from {@code connectionUri}.
   */
  static SqlClient client(Vertx vertx, String connectionUri, PoolOptions poolOptions) {
    return client(vertx, MySQLConnectOptions.fromUri(connectionUri), poolOptions);
  }

  /**
   * Create a client backed by a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param poolOptions the options for creating the backing pool
   * @return the client
   */
  static SqlClient client(MySQLConnectOptions connectOptions, PoolOptions poolOptions) {
    return client(null, Collections.singletonList(connectOptions), poolOptions);
  }

  /**
   * Like {@link #client(MySQLConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static SqlClient client(Vertx vertx, MySQLConnectOptions connectOptions, PoolOptions poolOptions) {
    return client(vertx, Collections.singletonList(connectOptions), poolOptions);
  }

  /**
   * Like {@link #client(List, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static SqlClient client(Vertx vertx, List<MySQLConnectOptions> mySQLConnectOptions, PoolOptions options) {
    return MySQLDriver.INSTANCE.createPool(vertx, mySQLConnectOptions, new MySQLPoolOptions(options).setPipelined(true));
  }

  /**
   * Create a client backed by a connection pool to the MySQL {@code databases} with round-robin selection.
   * Round-robin is applied when a new connection is created by the pool.
   *
   * @param databases the list of databases
   * @param options the options for creating the pool
   * @return the pooled client
   */
  static SqlClient client(List<MySQLConnectOptions> databases, PoolOptions options) {
    return client(null, databases, options);
  }


  @Override
  MySQLPool connectHandler(Handler<SqlConnection> handler);

  @Fluent
  MySQLPool connectionProvider(Function<Context, Future<SqlConnection>> provider);
}

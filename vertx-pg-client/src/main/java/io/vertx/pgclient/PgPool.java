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

package io.vertx.pgclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.pgclient.spi.PgDriver;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * A {@link Pool pool} of {@link PgConnection PostgreSQL connections}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgPool extends Pool {

  /**
   * Like {@link #pool(PoolOptions)} with a default {@code poolOptions}.
   */
  static PgPool pool() {
    return pool(PgConnectOptions.fromEnv(), new PoolOptions());
  }

  /**
   * Like {@link #pool(PgConnectOptions, PoolOptions)} with {@code connectOptions} build from the environment variables.
   */
  static PgPool pool(PoolOptions options) {
    return pool(PgConnectOptions.fromEnv(), options);
  }

  /**
   * Like {@link #pool(String, PoolOptions)} with a default {@code poolOptions}.
   */
  static PgPool pool(String connectionUri) {
    return pool(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #pool(PgConnectOptions, PoolOptions)} with {@code connectOptions} build from {@code connectionUri}.
   */
  static PgPool pool(String connectionUri, PoolOptions options) {
    return pool(PgConnectOptions.fromUri(connectionUri), options);
  }

  /**
   * Like {@link #pool(Vertx, String,PoolOptions)} with default options.
   */
  static PgPool pool(Vertx vertx, String connectionUri) {
    return pool(vertx, PgConnectOptions.fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #pool(Vertx, PgConnectOptions, PoolOptions)} with the {@code database} retrieved from the environment variables.
   */
  static PgPool pool(Vertx vertx, PoolOptions options) {
    return pool(vertx, PgConnectOptions.fromEnv(), options);
  }

  /**
   * Like {@link #pool(Vertx, PgConnectOptions, PoolOptions)} with {@code database} retrieved from the given {@code connectionUri}.
   */
  static PgPool pool(Vertx vertx, String connectionUri, PoolOptions poolOptions) {
    return pool(vertx, PgConnectOptions.fromUri(connectionUri), poolOptions);
  }

  /**
   * Create a connection pool to the PostgreSQL {@code database} configured with the given {@code options}.
   *
   * @param database the database
   * @param options the options for creating the pool
   * @return the connection pool
   */
  static PgPool pool(PgConnectOptions database, PoolOptions options) {
    return pool(null, database, options);
  }

  /**
   * Like {@link #pool(PgConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static PgPool pool(Vertx vertx, PgConnectOptions database, PoolOptions options) {
    return pool(vertx, Collections.singletonList(database), options);
  }

  /**
   * Create a connection pool to the PostgreSQL {@code databases} with round-robin selection.
   * Round-robin is applied when a new connection is created by the pool.
   *
   * @param databases the list of databases
   * @param poolOptions the options for creating the pool
   * @return the connection pool
   */
  static PgPool pool(List<PgConnectOptions> databases, PoolOptions poolOptions) {
    return pool(null, databases, poolOptions);
  }

  /**
   * Like {@link #pool(List, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static PgPool pool(Vertx vertx, List<PgConnectOptions> databases, PoolOptions poolOptions) {
    return new PgDriver().createPool(vertx, databases, poolOptions);
  }

  /**
   * Like {@link #client(PoolOptions)} with default options.
   */
  static SqlClient client() {
    return client(PgConnectOptions.fromEnv(), new PoolOptions());
  }

  /**
   * Like {@link #client(PgConnectOptions, PoolOptions)} with {@code database} retrieved from the environment variables.
   */
  static SqlClient client(PoolOptions options) {
    return client(PgConnectOptions.fromEnv(), options);
  }

  /**
   * Like {@link #pool(String, PoolOptions)} with default options.
   */
  static SqlClient client(String connectionUri) {
    return client(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #client(PgConnectOptions, PoolOptions)} with {@code database} retrieved from the {@code connectionUri}.
   */
  static SqlClient client(String connectionUri, PoolOptions options) {
    return client(PgConnectOptions.fromUri(connectionUri), options);
  }

  /**
   * Like {@link #client(Vertx, String,PoolOptions)} with default options.
   */
  static SqlClient client(Vertx vertx, String connectionUri) {
    return client(vertx, PgConnectOptions.fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #client(Vertx, PgConnectOptions, PoolOptions)} with {@code database} retrieved from the environment variables.
   */
  static SqlClient client(Vertx vertx, PoolOptions poolOptions) {
    return client(vertx, PgConnectOptions.fromEnv(), poolOptions);
  }

  /**
   * Like {@link #client(Vertx, PgConnectOptions, PoolOptions)} with {@code database} build from {@code connectionUri}.
   */
  static SqlClient client(Vertx vertx, String connectionUri, PoolOptions options) {
    return client(vertx, PgConnectOptions.fromUri(connectionUri), options);
  }

  /**
   * Create a client backed by a connection pool to the PostgreSQL {@code database} configured with the given {@code options}.
   *
   * @param options the options for creating the backing pool
   * @return the pooled client
   */
  static SqlClient client(PgConnectOptions database, PoolOptions options) {
    return client(null, database, options);
  }

  /**
   * Like {@link #client(PgConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static SqlClient client(Vertx vertx, PgConnectOptions database, PoolOptions options) {
    return client(vertx, Collections.singletonList(database), options);
  }

  /**
   * Like {@link #client(List, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static SqlClient client(Vertx vertx, List<PgConnectOptions> databases, PoolOptions options) {
    return new PgDriver().createClient(vertx, databases, options);
  }

  /**
   * Create a client backed by a connection pool to the PostgreSQL {@code databases} with round-robin selection.
   * Round-robin is applied when a new connection is created by the pool.
   *
   * @param databases the list of databases
   * @param options the options for creating the pool
   * @return the pooled client
   */
  static SqlClient client(List<PgConnectOptions> databases, PoolOptions options) {
    return client(null, databases, options);
  }

  @Override
  PgPool connectHandler(Handler<SqlConnection> handler);

  @Fluent
  PgPool connectionProvider(Function<Context, Future<SqlConnection>> provider);
}

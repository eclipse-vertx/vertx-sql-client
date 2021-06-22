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

import io.vertx.core.impl.VertxInternal;
import io.vertx.pgclient.impl.PgPoolImpl;
import io.vertx.sqlclient.PoolConfig;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.SqlClient;

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
  static PgPool pool(PoolOptions poolOptions) {
    return pool(PgConnectOptions.fromEnv(), poolOptions);
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
  static PgPool pool(String connectionUri, PoolOptions poolOptions) {
    return pool(PgConnectOptions.fromUri(connectionUri), poolOptions);
  }

  /**
   * Like {@link #pool(Vertx, String,PoolOptions)} with a default {@code poolOptions}.
   */
  static PgPool pool(Vertx vertx, String connectionUri) {
    return pool(vertx, PgConnectOptions.fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #pool(Vertx, PgConnectOptions, PoolOptions)} with {@code connectOptions} build from the environment variables.
   */
  static PgPool pool(Vertx vertx, PoolOptions poolOptions) {
    return pool(vertx, PgConnectOptions.fromEnv(), poolOptions);
  }

  /**
   * Like {@link #pool(Vertx, PgConnectOptions, PoolOptions)} with {@code connectOptions} build from {@code connectionUri}.
   */
  static PgPool pool(Vertx vertx, String connectionUri, PoolOptions poolOptions) {
    return pool(vertx, PgConnectOptions.fromUri(connectionUri), poolOptions);
  }

  /**
   * Create a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param poolOptions the options for creating the pool
   * @return the connection pool
   */
  static PgPool pool(PgConnectOptions connectOptions, PoolOptions poolOptions) {
    return pool(null, connectOptions, poolOptions);
  }

  /**
   * Like {@link #pool(PgConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static PgPool pool(Vertx vertx, PgConnectOptions connectOptions, PoolOptions poolOptions) {
    PoolConfig poolConfig = PoolConfig.create(poolOptions);
    PoolConfig config = poolConfig.connectingTo(connectOptions);
    return pool(vertx, config);
  }

  /**
   * Create a connection pool to the database configured with the given {@code config}.
   *
   * @param config the pool config
   * @return the connection pool
   */
  static PgPool pool(PoolConfig config) {
    return pool(null, config);
  }

  /**
   * Like {@link #pool(PoolConfig)} with a specific {@link Vertx} instance.
   */
  static PgPool pool(Vertx vertx, PoolConfig config) {
    return PgPoolImpl.create((VertxInternal) vertx, false, config);
  }

  /**
   * Like {@link #client(PoolOptions)} with a default {@code poolOptions}.
   */
  static SqlClient client() {
    return client(PgConnectOptions.fromEnv(), new PoolOptions());
  }

  /**
   * Like {@link #client(PgConnectOptions, PoolOptions)} with {@code connectOptions} build from the environment variables.
   */
  static SqlClient client(PoolOptions poolOptions) {
    return client(PgConnectOptions.fromEnv(), poolOptions);
  }

  /**
   * Like {@link #pool(String, PoolOptions)} with a default {@code poolOptions}.
   */
  static SqlClient client(String connectionUri) {
    return client(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #client(PgConnectOptions, PoolOptions)} with {@code connectOptions} build from {@code connectionUri}.
   */
  static SqlClient client(String connectionUri, PoolOptions poolOptions) {
    return client(PgConnectOptions.fromUri(connectionUri), poolOptions);
  }

  /**
   * Like {@link #client(Vertx, String,PoolOptions)} with a default {@code poolOptions}.
   */
  static SqlClient client(Vertx vertx, String connectionUri) {
    return client(vertx, PgConnectOptions.fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #client(Vertx, PgConnectOptions, PoolOptions)} with {@code connectOptions} build from the environment variables.
   */
  static SqlClient client(Vertx vertx, PoolOptions poolOptions) {
    return client(vertx, PgConnectOptions.fromEnv(), poolOptions);
  }

  /**
   * Like {@link #client(Vertx, PgConnectOptions, PoolOptions)} with {@code connectOptions} build from {@code connectionUri}.
   */
  static SqlClient client(Vertx vertx, String connectionUri, PoolOptions poolOptions) {
    return client(vertx, PgConnectOptions.fromUri(connectionUri), poolOptions);
  }

  /**
   * Create a client backed by a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param poolOptions the options for creating the backing pool
   * @return the client
   */
  static SqlClient client(PgConnectOptions connectOptions, PoolOptions poolOptions) {
    return client(null, connectOptions, poolOptions);
  }

  /**
   * Like {@link #client(PgConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static SqlClient client(Vertx vertx, PgConnectOptions connectOptions, PoolOptions poolOptions) {
    return client(vertx, PoolConfig.create(poolOptions).connectingTo(connectOptions));
  }

  /**
   * Like {@link #client(PoolConfig)} with a specific {@link Vertx} instance.
   */
  static SqlClient client(Vertx vertx, PoolConfig config) {
    return PgPoolImpl.create((VertxInternal) vertx, true, config);
  }

  /**
   * Create a client backed by a connection pool to the database configured with the given {@code config}.
   *
   * @param config the pool config for creating the backing pool
   * @return the client
   */
  static SqlClient client(PoolConfig config) {
    return client(null, config);
  }
}

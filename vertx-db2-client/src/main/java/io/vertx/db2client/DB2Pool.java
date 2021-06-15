/*
 * Copyright (C) 2019,2020 IBM Corporation
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
 */
package io.vertx.db2client;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.db2client.impl.DB2PoolImpl;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolConfig;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

import static io.vertx.db2client.DB2ConnectOptions.fromUri;

/**
 * A pool of DB2 connections.
 */
@VertxGen
public interface DB2Pool extends Pool {

  /**
   * Like {@link #pool(String, PoolOptions)} with a default {@code poolOptions}.
   */
  static DB2Pool pool(String connectionUri) {
    return pool(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #pool(DB2ConnectOptions, PoolOptions)} with
   * {@code connectOptions} build from {@code connectionUri}.
   */
  static DB2Pool pool(String connectionUri, PoolOptions poolOptions) {
    return pool(fromUri(connectionUri), poolOptions);
  }

  /**
   * Like {@link #pool(Vertx, String,PoolOptions)} with a default
   * {@code poolOptions}..
   */
  static DB2Pool pool(Vertx vertx, String connectionUri) {
    return pool(vertx, fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #pool(Vertx, DB2ConnectOptions, PoolOptions)} with
   * {@code connectOptions} build from {@code connectionUri}.
   */
  static DB2Pool pool(Vertx vertx, String connectionUri, PoolOptions poolOptions) {
    return pool(vertx, fromUri(connectionUri), poolOptions);
  }

  /**
   * Create a connection pool to the DB2 server configured with the given
   * {@code connectOptions} and {@code poolOptions}.
   *
   * @param connectOptions the options for the connection
   * @param poolOptions    the options for creating the pool
   * @return the connection pool
   */
  static DB2Pool pool(DB2ConnectOptions connectOptions, PoolOptions poolOptions) {
    return pool(null, connectOptions, poolOptions);
  }

  /**
   * Like {@link #pool(DB2ConnectOptions, PoolOptions)} with a specific
   * {@link Vertx} instance.
   */
  static DB2Pool pool(Vertx vertx, DB2ConnectOptions connectOptions, PoolOptions poolOptions) {
    return pool(vertx, PoolConfig.create(poolOptions).connectOptions(connectOptions));
  }

  /**
   * Create a connection pool to the DB2 server configured with the given
   * {@code config}.
   *
   * @param config the pool config
   * @return the connection pool
   */
  static DB2Pool pool(PoolConfig config) {
    return pool(null, config);
  }

  /**
   * Like {@link #pool(PoolConfig)} with a specific
   * {@link Vertx} instance.
   */
  static DB2Pool pool(Vertx vertx, PoolConfig config) {
    return DB2PoolImpl.create((VertxInternal) vertx, false, config);
  }

  /**
   * Like {@link #client(String, PoolOptions)} with a default {@code poolOptions}.
   */
  static SqlClient client(String connectionUri) {
    return client(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #client(DB2ConnectOptions, PoolOptions)} with
   * {@code connectOptions} build from {@code connectionUri}.
   */
  static SqlClient client(String connectionUri, PoolOptions poolOptions) {
    return client(fromUri(connectionUri), poolOptions);
  }

  /**
   * Like {@link #client(Vertx, String,PoolOptions)} with a default
   * {@code poolOptions}..
   */
  static SqlClient client(Vertx vertx, String connectionUri) {
    return client(vertx, fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #client(Vertx, DB2ConnectOptions, PoolOptions)} with
   * {@code connectOptions} build from {@code connectionUri}.
   */
  static SqlClient client(Vertx vertx, String connectionUri, PoolOptions poolOptions) {
    return client(vertx, fromUri(connectionUri), poolOptions);
  }

  /**
   * Create a pooled client to the DB2 server configured with the given
   * {@code connectOptions} and {@code poolOptions}.
   *
   * @param connectOptions the options for the connection
   * @param poolOptions    the options for creating the pool
   * @return the connection pool
   */
  static SqlClient client(DB2ConnectOptions connectOptions, PoolOptions poolOptions) {
    return client(null, connectOptions, poolOptions);
  }

  /**
   * Like {@link #client(DB2ConnectOptions, PoolOptions)} with a specific
   * {@link Vertx} instance.
   */
  static SqlClient client(Vertx vertx, DB2ConnectOptions connectOptions, PoolOptions poolOptions) {
    return client(vertx, PoolConfig.create(poolOptions).connectOptions(connectOptions));
  }

  /**
   * Create a pooled client to the DB2 server configured with the given
   * {@code config}.
   *
   * @param config the pool config
   * @return the connection pool
   */
  static SqlClient client(PoolConfig config) {
    return client(null, config);
  }

  /**
   * Like {@link #client(PoolConfig)} with a specific
   * {@link Vertx} instance.
   */
  static SqlClient client(Vertx vertx, PoolConfig config) {
    return DB2PoolImpl.create((VertxInternal) vertx, true, config);
  }
}

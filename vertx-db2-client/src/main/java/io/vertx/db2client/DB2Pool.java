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
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.ContextInternal;
import io.vertx.db2client.impl.DB2PoolImpl;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

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
    if (Vertx.currentContext() != null) {
      throw new IllegalStateException(
          "Running in a Vertx context => use DB2Pool#pool(Vertx, DB2ConnectOptions, PoolOptions) instead");
    }
    VertxOptions vertxOptions = new VertxOptions();
    Vertx vertx = Vertx.vertx(vertxOptions);
    return DB2PoolImpl.create((ContextInternal) vertx.getOrCreateContext(), true, connectOptions, poolOptions);
  }

  /**
   * Like {@link #pool(DB2ConnectOptions, PoolOptions)} with a specific
   * {@link Vertx} instance.
   */
  static DB2Pool pool(Vertx vertx, DB2ConnectOptions connectOptions, PoolOptions poolOptions) {
    return DB2PoolImpl.create((ContextInternal) vertx.getOrCreateContext(), false, connectOptions, poolOptions);
  }
}

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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.db2client.impl.Db2PoolOptions;
import io.vertx.db2client.spi.DB2Driver;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.vertx.db2client.DB2ConnectOptions.fromUri;

/**
 * A pool of DB2 connections.
 */
@VertxGen
public interface DB2Pool extends Pool {

  /**
   * Like {@link #pool(String, PoolOptions)} with default options.
   */
  static DB2Pool pool(String connectionUri) {
    return pool(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #pool(DB2ConnectOptions, PoolOptions)} with
   * {@code database} build from {@code connectionUri}.
   */
  static DB2Pool pool(String connectionUri, PoolOptions options) {
    return pool(fromUri(connectionUri), options);
  }

  /**
   * Like {@link #pool(Vertx, String,PoolOptions)} with default options.
   */
  static DB2Pool pool(Vertx vertx, String connectionUri) {
    return pool(vertx, fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #pool(Vertx, DB2ConnectOptions, PoolOptions)} with
   * {@code database} build from {@code connectionUri}.
   */
  static DB2Pool pool(Vertx vertx, String connectionUri, PoolOptions options) {
    return pool(vertx, fromUri(connectionUri), options);
  }

  /**
   * Create a connection pool to the DB2 {@code database} configured with the given {@code options}.
   *
   * @param database the options for the connection
   * @param options    the options for creating the pool
   * @return the connection pool
   */
  static DB2Pool pool(DB2ConnectOptions database, PoolOptions options) {
    return pool(null, database, options);
  }

  /**
   * Like {@link #pool(DB2ConnectOptions, PoolOptions)} with a specific
   * {@link Vertx} instance.
   */
  static DB2Pool pool(Vertx vertx, DB2ConnectOptions database, PoolOptions options) {
    return pool(vertx, Collections.singletonList(database), options);
  }

  /**
   * Create a connection pool to the DB2 {@code databases} with round-robin selection.
   * Round-robin is applied when a new connection is created by the pool.
   *
   * @param databases the list of servers
   * @param options the options for creating the pool
   * @return the connection pool
   */
  static DB2Pool pool(List<DB2ConnectOptions> databases, PoolOptions options) {
    return pool(null, databases, options);
  }

  /**
   * Like {@link #pool(List, PoolOptions)} with a specific
   * {@link Vertx} instance.
   */
  static DB2Pool pool(Vertx vertx, List<DB2ConnectOptions> databases, PoolOptions options) {
    return (DB2Pool) DB2Driver.INSTANCE.createPool(vertx, databases, options);
  }

  /**
   * Create a connection pool to the DB2 {@code databases}. The supplier is called
   * to provide the options when a new connection is created by the pool.
   *
   * @param databases the databases supplier
   * @param poolOptions the options for creating the pool
   * @return the connection pool
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static DB2Pool pool(Supplier<Future<DB2ConnectOptions>> databases, PoolOptions poolOptions) {
    return pool(null, databases, poolOptions);
  }


  /**
   * Like {@link #pool(Supplier, PoolOptions)} with a specific {@link Vertx} instance.
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static DB2Pool pool(Vertx vertx, Supplier<Future<DB2ConnectOptions>> databases, PoolOptions poolOptions) {
    return (DB2Pool) DB2Driver.INSTANCE.createPool(vertx, databases, poolOptions);
  }

  /**
   * Like {@link #client(String, PoolOptions)} with default options.
   */
  static SqlClient client(String connectionUri) {
    return client(connectionUri, new PoolOptions());
  }

  /**
   * Like {@link #client(DB2ConnectOptions, PoolOptions)} with
   * {@code database} build from {@code connectionUri}.
   */
  static SqlClient client(String connectionUri, PoolOptions options) {
    return client(fromUri(connectionUri), options);
  }

  /**
   * Like {@link #client(Vertx, String, PoolOptions)} with default options.
   */
  static SqlClient client(Vertx vertx, String connectionUri) {
    return client(vertx, fromUri(connectionUri), new PoolOptions());
  }

  /**
   * Like {@link #client(Vertx, DB2ConnectOptions, PoolOptions)} with
   * {@code database} build from {@code connectionUri}.
   */
  static SqlClient client(Vertx vertx, String connectionUri, PoolOptions options) {
    return client(vertx, fromUri(connectionUri), options);
  }

  /**
   * Create a pooled client to the DB2 {@code database} configured with the given {@code options}.
   *
   * @param database the options for the connection
   * @param options    the options for creating the pool
   * @return the connection pool
   */
  static SqlClient client(DB2ConnectOptions database, PoolOptions options) {
    return client(null, database, options);
  }

  /**
   * Like {@link #client(DB2ConnectOptions, PoolOptions)} with a specific
   * {@link Vertx} instance.
   */
  static SqlClient client(Vertx vertx, DB2ConnectOptions database, PoolOptions options) {
    return client(vertx, Collections.singletonList(database), options);
  }

  /**
   * Create a client backed by a connection pool to the DB2 {@code databases} with round-robin selection.
   * Round-robin is applied when a new connection is created by the pool.
   *
   * @param databases the list of servers
   * @param options the options for creating the pool
   * @return the pooled client
   */
  static SqlClient client(List<DB2ConnectOptions> databases, PoolOptions options) {
    return client(null, databases, options);
  }

  /**
   * Like {@link #client(List, PoolOptions)} with a specific
   * {@link Vertx} instance.
   */
  static SqlClient client(Vertx vertx, List<DB2ConnectOptions> databases, PoolOptions options) {
    return DB2Driver.INSTANCE.createPool(vertx, databases, new Db2PoolOptions(options).setPipelined(true));
  }

  @Override
  DB2Pool connectHandler(Handler<SqlConnection> handler);

  @Fluent
  DB2Pool connectionProvider(Function<Context, Future<SqlConnection>> provider);
}

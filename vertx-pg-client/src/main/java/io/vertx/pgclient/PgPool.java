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

import io.vertx.pgclient.impl.PgPoolImpl;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.List;
import java.util.stream.Collector;

/**
 * A pool of PostgreSQL connections.
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
    if (Vertx.currentContext() != null) {
      throw new IllegalStateException("Running in a Vertx context => use PgPool#pool(Vertx, PgConnectOptions, PoolOptions) instead");
    }
    VertxOptions vertxOptions = new VertxOptions();
    if (connectOptions.isUsingDomainSocket()) {
      vertxOptions.setPreferNativeTransport(true);
    }
    Vertx vertx = Vertx.vertx(vertxOptions);
    return new PgPoolImpl(vertx.getOrCreateContext(), true, connectOptions, poolOptions);
  }

  /**
   * Like {@link #pool(PgConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static PgPool pool(Vertx vertx, PgConnectOptions connectOptions, PoolOptions poolOptions) {
    return new PgPoolImpl(vertx.getOrCreateContext(), false, connectOptions, poolOptions);
  }

  PgPool preparedQuery(String sql, Handler<AsyncResult<RowSet>> handler);

  @GenIgnore
  <R> PgPool preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);
  PgPool query(String sql, Handler<AsyncResult<RowSet>> handler);

  @GenIgnore
  <R> PgPool query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);
  PgPool preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet>> handler);

  @GenIgnore
  <R> PgPool preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);
  PgPool preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet>> handler);

  @GenIgnore
  <R> PgPool preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

}

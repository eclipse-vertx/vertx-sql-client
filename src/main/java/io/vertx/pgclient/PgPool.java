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
 * A pool of connection.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgPool extends Pool {

  /**
   * Like {@link #pool(PgPoolOptions)} with options build from the environment variables.
   */
  static PgPool pool() {
    return pool(PgPoolOptions.fromEnv());
  }

  /**
   * Like {@link #pool(PgPoolOptions)} with options build from {@code connectionUri}.
   */
  static PgPool pool(String connectionUri) {
    return pool(PgPoolOptions.fromUri(connectionUri));
  }

  /**
   * Like {@link #pool(Vertx, PgPoolOptions)} with options build from the environment variables.
   */
  static PgPool pool(Vertx vertx) {
    return pool(vertx, PgPoolOptions.fromEnv());
  }

  /**
   * Like {@link #pool(Vertx, PgPoolOptions)} with options build from {@code connectionUri}.
   */
  static PgPool pool(Vertx vertx, String connectionUri) {
    return pool(vertx, PgPoolOptions.fromUri(connectionUri));
  }

  /**
   * Create a connection pool to the database configured with the given {@code options}.
   *
   * @param options the options for creating the pool
   * @return the connection pool
   */
  static PgPool pool(PgPoolOptions options) {
    if (Vertx.currentContext() != null) {
      throw new IllegalStateException("Running in a Vertx context => use PgPool#pool(Vertx, PgPoolOptions) instead");
    }
    VertxOptions vertxOptions = new VertxOptions();
    if (options.isUsingDomainSocket()) {
      vertxOptions.setPreferNativeTransport(true);
    }
    Vertx vertx = Vertx.vertx(vertxOptions);
    return new PgPoolImpl(vertx.getOrCreateContext(), true, options);
  }

  /**
   * Like {@link #pool(PgPoolOptions)} with a specific {@link Vertx} instance.
   */
  static PgPool pool(Vertx vertx, PgPoolOptions options) {
    return new PgPoolImpl(vertx.getOrCreateContext(), false, options);
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

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

package io.reactiverse.pgclient;

import io.reactiverse.pgclient.impl.Connection;
import io.reactiverse.pgclient.impl.PgConnectionFactory;
import io.reactiverse.pgclient.impl.PgConnectionImpl;
import io.reactiverse.pgclient.impl.PgPoolImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.*;

import java.util.List;
import java.util.stream.Collector;

/**
 * Defines the client operations with a Postgres Database.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgClient {

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
    return new PgPoolImpl(vertx, true, options);
  }

  /**
   * Like {@link #pool(PgPoolOptions)} with a specific {@link Vertx} instance.
   */
  static PgPool pool(Vertx vertx, PgPoolOptions options) {
    return new PgPoolImpl(vertx, false, options);
  }

  /**
   * Connects to the database and returns the connection if that succeeds.
   * <p/>
   * The connection interracts directly with the database is not a proxy, so closing the
   * connection will close the underlying connection to the database.
   *
   * @param vertx the vertx instance
   * @param options the connect options
   * @param handler the handler called with the connection or the failure
   */
  static void connect(Vertx vertx, PgConnectOptions options, Handler<AsyncResult<PgConnection>> handler) {
    Context ctx = Vertx.currentContext();
    if (ctx != null) {
      PgConnectionFactory client = new PgConnectionFactory(ctx, false, options);
      client.connect(ar -> {
        if (ar.succeeded()) {
          Connection conn = ar.result();
          PgConnectionImpl p = new PgConnectionImpl(ctx, conn);
          conn.init(p);
          handler.handle(Future.succeededFuture(p));
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
        }
      });
    } else {
      vertx.runOnContext(v -> {
        if (options.isUsingDomainSocket() && !vertx.isNativeTransportEnabled()) {
          handler.handle(Future.failedFuture("Native transport is not available"));
        } else {
          connect(vertx, options, handler);
        }
      });
    }
  }

  /**
   * Like {@link #connect(Vertx, PgConnectOptions, Handler)} with options build from the environment variables.
   */
  static void connect(Vertx vertx, Handler<AsyncResult<PgConnection>> handler) {
    connect(vertx, PgConnectOptions.fromEnv(), handler);
  }

  /**
   * Like {@link #connect(Vertx, PgConnectOptions, Handler)} with options build from {@code connectionUri}.
   */
  static void connect(Vertx vertx, String connectionUri, Handler<AsyncResult<PgConnection>> handler) {
    connect(vertx, PgConnectOptions.fromUri(connectionUri), handler);
  }

  /**
   * Execute a simple query.
   *
   * @param sql the query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient query(String sql, Handler<AsyncResult<PgRowSet>> handler);

  /**
   * Execute a simple query.
   *
   * @param sql the query SQL
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> PgClient query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient preparedQuery(String sql, Handler<AsyncResult<PgRowSet>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> PgClient preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgRowSet>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> PgClient preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  /**
   * Prepare and execute a createBatch.
   *
   * @param sql the prepared query SQL
   * @param batch the batch of tuples
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<PgRowSet>> handler);

  /**
   * Prepare and execute a createBatch.
   *
   * @param sql the prepared query SQL
   * @param batch the batch of tuples
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> PgClient preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

}

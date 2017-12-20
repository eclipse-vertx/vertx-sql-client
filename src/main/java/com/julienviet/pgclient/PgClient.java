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

package com.julienviet.pgclient;

import com.julienviet.pgclient.impl.*;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.*;

import java.util.List;

/**
 * Defines the client operations with a Postgres Database.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgClient {

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
    return new PgPoolImpl(Vertx.vertx(), true, options);
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
        connect(vertx, options, handler);
      });
    }
  }

  /**
   * Execute a simple query.
   *
   * @param sql the query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient query(String sql, Handler<AsyncResult<PgResult<Row>>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient preparedQuery(String sql, Handler<AsyncResult<PgResult<Row>>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgResult<Row>>> handler);

  /**
   * Prepare and execute a createBatch.
   *
   * @param sql the prepared query SQL
   * @param batch the batch of tuples
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<PgResult<Row>>> handler);

}

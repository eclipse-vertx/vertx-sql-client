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

import com.julienviet.pgclient.impl.PgPoolImpl;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.List;

/**
 * A pool of connection.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgPool extends PgClient {

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

  @Override
  default PgPool preparedQuery(String sql, Handler<AsyncResult<PgResult<Row>>> handler) {
    return (PgPool) PgClient.super.preparedQuery(sql, handler);
  }

  @Override
  PgPool query(String sql, Handler<AsyncResult<PgResult<Row>>> handler);

  @Override
  PgPool preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgResult<Row>>> handler);

  @Override
  PgPool preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<PgBatchResult<Row>>> handler);

  /**
   * Obtain a connection from the pool.
   *
   * @param handler the handler that will get the connection result
   */
  void connect(Handler<AsyncResult<PgConnection>> handler);

  /**
   * Close the pool and release the associated resources.
   */
  void close();

}

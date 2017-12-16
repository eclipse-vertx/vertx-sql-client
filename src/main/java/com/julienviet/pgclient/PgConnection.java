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

import com.julienviet.pgclient.impl.PgConnectionFactory;
import com.julienviet.pgclient.impl.PgConnectionImpl;
import com.julienviet.pgclient.impl.SocketConnection;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.List;

/**
 * A connection to Postgres.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
@VertxGen
public interface PgConnection extends PgClient {

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
      client.connect(ar -> handler.handle(ar.map(conn -> {
        PgConnectionImpl p = new PgConnectionImpl(ctx, conn);
        conn.init(p);
        return p;
      })));
    } else {
      vertx.runOnContext(v -> {
        connect(vertx, options, handler);
      });
    }
  }

  /**
   * Create a simple query.
   *
   * @param sql the query SQL
   * @return the query ready to be executed
   */
  PgQuery createQuery(String sql);

  @Override
  PgConnection preparedQuery(String sql, Handler<AsyncResult<PgResult<Row>>> handler);

  @Override
  PgConnection query(String sql, Handler<AsyncResult<PgResult<Row>>> handler);

  @Override
  PgConnection preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgResult<Row>>> handler);

  @Override
  PgConnection preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<PgBatchResult<Row>>> handler);

  /**
   * Create a prepared statement.
   *
   * @param sql the sql
   * @param handler the handler notified with the prepared statement asynchronously
   */
  @Fluent
  PgConnection prepare(String sql, Handler<AsyncResult<PgPreparedStatement>> handler);

  /**
   * Set an handler called with connection errors.
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgConnection exceptionHandler(Handler<Throwable> handler);

  /**
   * Set an handler called when the connection is closed.
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgConnection closeHandler(Handler<Void> handler);

  /**
   * Begin a transaction.
   *
   * @return a reference to this, so the API can be used fluently
   */
  PgTransaction begin();

  boolean isSSL();

  /**
   * Close the current connection after all the pending commands have been processed.
   */
  void close();

}

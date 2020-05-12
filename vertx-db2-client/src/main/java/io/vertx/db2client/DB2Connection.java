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

import static io.vertx.db2client.DB2ConnectOptions.fromUri;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.db2client.impl.DB2ConnectionImpl;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.SqlConnection;

/**
 * A connection to DB2 server.
 */
@VertxGen
public interface DB2Connection extends SqlConnection {

  /**
   * Create a connection to DB2 server with the given {@code connectOptions}.
   *
   * @param vertx          the vertx instance
   * @param connectOptions the options for the connection
   * @param handler        the handler called with the connection or the failure
   */
  static void connect(Vertx vertx, DB2ConnectOptions connectOptions, Handler<AsyncResult<DB2Connection>> handler) {
    DB2ConnectionImpl.connect(vertx, connectOptions, handler);
  }

  /**
   * Like {@link #connect(Vertx, DB2ConnectOptions, Handler)} with options build
   * from {@code connectionUri}.
   */
  static void connect(Vertx vertx, String connectionUri, Handler<AsyncResult<DB2Connection>> handler) {
    connect(vertx, fromUri(connectionUri), handler);
  }

  @Override
  DB2Connection prepare(String sql, Handler<AsyncResult<PreparedStatement>> handler);

  @Override
  DB2Connection exceptionHandler(Handler<Throwable> handler);

  @Override
  DB2Connection closeHandler(Handler<Void> handler);

  /**
   * Send a PING command to check if the server is alive.
   *
   * @param handler the handler notified when the server responses to client
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  DB2Connection ping(Handler<AsyncResult<Void>> handler);

  /**
   * Send a DEBUG command to dump debug information to the server's stdout.
   *
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  DB2Connection debug(Handler<AsyncResult<Void>> handler);

}

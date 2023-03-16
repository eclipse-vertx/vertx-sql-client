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
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.db2client.impl.DB2ConnectionImpl;
import io.vertx.sqlclient.SqlConnection;

import static io.vertx.db2client.DB2ConnectOptions.fromUri;

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
   * @return a future notified with the connection or the failure
   */
  static Future<DB2Connection> connect(Vertx vertx, DB2ConnectOptions connectOptions) {
    return DB2ConnectionImpl.connect(vertx, connectOptions);
  }

  /**
   * Like {@link #connect(Vertx, DB2ConnectOptions)} with options build
   * from {@code connectionUri}.
   */
  static Future<DB2Connection> connect(Vertx vertx, String connectionUri) {
    return connect(vertx, fromUri(connectionUri));
  }

  @Override
  DB2Connection exceptionHandler(Handler<Throwable> handler);

  @Override
  DB2Connection closeHandler(Handler<Void> handler);

  /**
   * Send a PING command to check if the server is alive.
   *
   * @return a future notified with the server response
   */
  Future<Void> ping();

  /**
   * Send a DEBUG command to dump debug information to the server's stdout.
   *
   * @return a future notified with the execution result
   */
  Future<Void> debug();

  /**
   * Cast a {@link SqlConnection} to {@link DB2Connection}.
   *
   * This is mostly useful for Vert.x generated APIs like RxJava/Mutiny.
   *
   * @param sqlConnection the connection to cast
   * @return a {@link DB2Connection instance}
   */
  static DB2Connection cast(SqlConnection sqlConnection) {
    return (DB2Connection) sqlConnection;
  }
}

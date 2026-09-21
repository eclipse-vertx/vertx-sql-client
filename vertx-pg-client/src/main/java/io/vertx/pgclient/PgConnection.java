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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.internal.ContextInternal;
import io.vertx.pgclient.impl.PgConnectionImpl;
import io.vertx.sqlclient.SqlConnection;

/**
 * A connection to Postgres.
 * <P>
 *   The connection object supports all the operations defined in the {@link SqlConnection} interface,
 *   it also provides additional support:
 *   <ul>
 *     <li>Notification</li>
 *     <li>Request Cancellation</li>
 *   </ul>
 * </P>
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
@VertxGen
public interface PgConnection extends SqlConnection {

  /**
   * Connects to the database and returns the connection if that succeeds.
   * <p/>
   * The connection interracts directly with the database is not a proxy, so closing the
   * connection will close the underlying connection to the database.
   *
   * @param vertx the vertx instance
   * @param options the connect options
   * @return a future notified with the connection or the failure
   */
  static Future<PgConnection> connect(Vertx vertx, PgConnectOptions options) {
    return PgConnectionImpl.connect((ContextInternal) vertx.getOrCreateContext(), options);
  }

  /**
   * Like {@link #connect(Vertx, PgConnectOptions)} with options build from the environment variables.
   */
  static Future<PgConnection> connect(Vertx vertx) {
    return connect(vertx, PgConnectOptions.fromEnv());
  }

  /**
   * Like {@link #connect(Vertx, PgConnectOptions)} with options build from {@code connectionUri}.
   */
  static Future<PgConnection> connect(Vertx vertx, String connectionUri) {
    return connect(vertx, PgConnectOptions.fromUri(connectionUri));
  }

  /**
   * Set a handler called when the connection receives notification on a channel.
   * <p/>
   * The handler is called with the {@link PgNotification} and has access to the channel name
   * and the notification payload.
   *
   * @param handler the handler
   * @return the transaction instance
   */
  @Fluent
  PgConnection notificationHandler(Handler<PgNotification> handler);

  /**
   *Set a handler called when the connection receives a notice from the server.
   *
   * @param handler
   * @return
   */
  @Fluent
  PgConnection noticeHandler(Handler<PgNotice> handler);

  /**
   * Send a request cancellation message to tell the server to cancel processing request in this connection.
   * <br>Note: Use this with caution because the cancellation signal may or may not have any effect.
   *
   * @return a future notified if cancelling request is sent
   */
  Future<Void> cancelRequest();

  /**
   * @return The process ID of the target backend
   */
  int processId();

  /**
   * @return The secret key for the target backend
   */
  int secretKey();

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  PgConnection exceptionHandler(Handler<Throwable> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  PgConnection closeHandler(Handler<Void> handler);

  /**
   * Cast a {@link SqlConnection} to {@link PgConnection}.
   *
   * This is mostly useful for Vert.x generated APIs like RxJava/Mutiny.
   *
   * @param sqlConnection the connection to cast
   * @return a {@link PgConnection instance}
   */
  static PgConnection cast(SqlConnection sqlConnection) {
    return (PgConnection) sqlConnection;
  }

  /**
   * Execute a {@code COPY ... TO STDOUT} statement and stream the raw data produced by the server.
   * <p/>
   * The returned future is notified when PostgreSQL has entered COPY OUT mode, with a
   * {@link PgCopyOut} emitting the copy data as {@link io.vertx.core.buffer.Buffer} chunks.
   * A connection can have a single COPY operation in progress at a time.
   *
   * @param sql a single {@code COPY ... TO STDOUT} statement
   * @return a future notified with the stream or the failure
   */
  Future<PgCopyOut> copyOut(String sql);

  /**
   * Like {@link #copyOut(String)} but with the given {@code options}.
   *
   * @param sql a single {@code COPY ... TO STDOUT} statement
   * @param options the copy out options
   * @return a future notified with the stream or the failure
   */
  Future<PgCopyOut> copyOut(String sql, PgCopyOutOptions options);

  /**
   * Execute a {@code COPY ... FROM STDIN} statement and stream raw data to the server.
   * <p/>
   * The returned future is notified when PostgreSQL has entered COPY IN mode, with a
   * {@link PgCopyIn} accepting the copy data as {@link io.vertx.core.buffer.Buffer} chunks.
   * Call {@link PgCopyIn#end()} to signal the end of the data, or
   * {@link PgCopyIn#abort(String)} to roll the copy back. A connection can have a single
   * COPY operation in progress at a time.
   *
   * @param sql a single {@code COPY ... FROM STDIN} statement
   * @return a future notified with the stream or the failure
   */
  Future<PgCopyIn> copyIn(String sql);

  /**
   * Like {@link #copyIn(String)} but with the given {@code options}.
   *
   * @param sql a single {@code COPY ... FROM STDIN} statement
   * @param options the copy in options
   * @return a future notified with the stream or the failure
   */
  Future<PgCopyIn> copyIn(String sql, PgCopyInOptions options);
}

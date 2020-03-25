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

import java.util.stream.Collector;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.db2client.impl.DB2ConnectionImpl;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;

import static io.vertx.db2client.DB2ConnectOptions.fromUri;

/**
 * A connection to DB2 server.
 */
@VertxGen
public interface DB2Connection extends SqlConnection {
    /**
     * Create a connection to DB2 server with the given {@code connectOptions}.
     *
     * @param vertx the vertx instance
     * @param connectOptions the options for the connection
     * @param handler the handler called with the connection or the failure
     */
    static void connect(Vertx vertx, DB2ConnectOptions connectOptions, Handler<AsyncResult<DB2Connection>> handler) {
      Future<DB2Connection> fut = connect(vertx, connectOptions);
      if (handler != null) {
        fut.onComplete(handler);
      }
    }

    /**
     * Like {@link #connect(Vertx, DB2ConnectOptions, Handler)} but returns a {@code Future} of the asynchronous result
     */
    static Future<DB2Connection> connect(Vertx vertx, DB2ConnectOptions connectOptions) {
      return DB2ConnectionImpl.connect(vertx, connectOptions);
    }

    /**
     * Like {@link #connect(Vertx, DB2ConnectOptions, Handler)} with options build from {@code connectionUri}.
     */
    static void connect(Vertx vertx, String connectionUri, Handler<AsyncResult<DB2Connection>> handler) {
      connect(vertx, fromUri(connectionUri), handler);
    }

    /**
     * Like {@link #connect(Vertx, String, Handler)} but returns a {@code Future} of the asynchronous result
     */
    static Future<DB2Connection> connect(Vertx vertx, String connectionUri) {
      return connect(vertx, fromUri(connectionUri));
    }

    @Override
    DB2Connection prepare(String sql, Handler<AsyncResult<PreparedQuery<RowSet<Row>>>> handler);

    @Override
    DB2Connection exceptionHandler(Handler<Throwable> handler);

    @Override
    DB2Connection closeHandler(Handler<Void> handler);

    @Override
    DB2Connection preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

    @GenIgnore
    @Override
    <R> DB2Connection preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

    @Override
    DB2Connection query(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

    @GenIgnore
    @Override
    <R> DB2Connection query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

    @Override
    DB2Connection preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler);

    @GenIgnore
    @Override
    <R> DB2Connection preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

    /**
     * Send a PING command to check if the server is alive.
     *
     * @param handler the handler notified when the server responses to client
     * @return a reference to this, so the API can be used fluently
     */
    @Fluent
    DB2Connection ping(Handler<AsyncResult<Void>> handler);

    /**
     * Like {@link #ping(Handler)} but returns a {@code Future} of the asynchronous result
     */
    Future<Void> ping();

    /**
     * Send a DEBUG command to dump debug information to the server's stdout.
     *
     * @param handler the handler notified with the execution result
     * @return a reference to this, so the API can be used fluently
     */
    @Fluent
    DB2Connection debug(Handler<AsyncResult<Void>> handler);

    /**
     * Like {@link #debug(Handler)} but returns a {@code Future} of the asynchronous result
     */
    Future<Void> debug();

}

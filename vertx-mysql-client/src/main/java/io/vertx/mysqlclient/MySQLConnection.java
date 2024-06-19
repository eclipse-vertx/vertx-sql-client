/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.internal.ContextInternal;
import io.vertx.mysqlclient.impl.MySQLConnectionImpl;
import io.vertx.sqlclient.SqlConnection;

import static io.vertx.mysqlclient.MySQLConnectOptions.fromUri;

/**
 * An interface which represents a connection to MySQL server.
 * <P>
 *   The connection object supports all the operations defined in the {@link SqlConnection} interface,
 *   in addition it provides MySQL utility command support:
 *   <ul>
 *     <li>COM_PING</li>
 *     <li>COM_CHANGE_USER</li>
 *     <li>COM_RESET_CONNECTION</li>
 *     <li>COM_DEBUG</li>
 *     <li>COM_INIT_DB</li>
 *     <li>COM_STATISTICS</li>
 *     <li>COM_SET_OPTION</li>
 *   </ul>
 * </P>
 */
@VertxGen
public interface MySQLConnection extends SqlConnection {

  /**
   * Create a connection to MySQL server with the given {@code connectOptions}.
   *
   * @param vertx the vertx instance
   * @param connectOptions the options for the connection
   * @return a future notified with the connection or the failure
   */
  static Future<MySQLConnection> connect(Vertx vertx, MySQLConnectOptions connectOptions) {
    return MySQLConnectionImpl.connect((ContextInternal) vertx.getOrCreateContext(), connectOptions);
  }

  /**
   * Like {@link #connect(Vertx, MySQLConnectOptions)} with options built from {@code connectionUri}.
   */
  static Future<MySQLConnection> connect(Vertx vertx, String connectionUri) {
    return connect(vertx, fromUri(connectionUri));
  }

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  MySQLConnection exceptionHandler(Handler<Throwable> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  MySQLConnection closeHandler(Handler<Void> handler);

  /**
   * Send a PING command to check if the server is alive.
   *
   * @return a future notified with the server response
   */
  Future<Void> ping();

  /**
   * Send a INIT_DB command to change the default schema of the connection.
   *
   * @param schemaName name of the schema to change to
   * @return a future notified with the execution result
   */
  Future<Void> specifySchema(String schemaName);

  /**
   * Send a STATISTICS command to get a human readable string of the server internal status.
   *
   * @return a future notified with the execution result
   */
  Future<String> getInternalStatistics();


  /**
   * Send a SET_OPTION command to set options for the current connection.
   *
   * @param option the options to set
   * @return a future notified with the execution result
   */
  Future<Void> setOption(MySQLSetOption option);

  /**
   * Send a RESET_CONNECTION command to reset the session state.
   *
   * @return a future notified with the execution result
   */
  Future<Void> resetConnection();

  /**
   * Send a DEBUG command to dump debug information to the server's stdout.
   *
   * @return a future notified with the execution result
   */
  Future<Void> debug();

  /**
   * Send a CHANGE_USER command to change the user of the current connection, this operation will also reset connection state.
   *
   * @return a future notified with the execution result
   */
  Future<Void> changeUser(MySQLAuthOptions options);

  /**
   * Cast a {@link SqlConnection} to {@link MySQLConnection}.
   *
   * This is mostly useful for Vert.x generated APIs like RxJava/Mutiny.
   *
   * @param sqlConnection the connection to cast
   * @return a {@link MySQLConnection instance}
   */
  static MySQLConnection cast(SqlConnection sqlConnection) {
    return (MySQLConnection) sqlConnection;
  }
}

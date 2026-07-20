/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.oracleclient.impl.OracleConnectionImpl;
import io.vertx.sqlclient.SqlConnection;

import static io.vertx.oracleclient.OracleConnectOptions.fromUri;

/**
 * A connection to Oracle Database.
 */
@VertxGen
public interface OracleConnection extends SqlConnection {

  /**
   * Create a connection to Oracle with the given {@code connectOptions}.
   *
   * @param vertx the vertx instance
   * @param connectOptions the options for the connection
   * @return a future notified with with the connection or the failure
   */
  static Future<OracleConnection> connect(Vertx vertx, OracleConnectOptions connectOptions) {
    return OracleConnectionImpl.connect(vertx, connectOptions);
  }

  /**
   * Like {@link #connect(Vertx, OracleConnectOptions)} with options built from {@code connectionUri}.
   */
  static Future<OracleConnection> connect(Vertx vertx, String connectionUri) {
    return connect(vertx, fromUri(connectionUri));
  }

  Object createArray(String typeName, Object elements);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  OracleConnection exceptionHandler(Handler<Throwable> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  OracleConnection closeHandler(Handler<Void> handler);

  /**
   * Cast a {@link SqlConnection} to {@link OracleConnection}.
   * <p>
   * This is mostly useful for Vert.x generated APIs like RxJava/Mutiny.
   *
   * @param sqlConnection the connection to cast
   * @return a {@link OracleConnection instance}
   */
  static OracleConnection cast(SqlConnection sqlConnection) {
    return (OracleConnection) sqlConnection;
  }
}

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

package io.vertx.sqlclient.spi.connection;

import io.vertx.core.Completable;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.spi.protocol.CommandScheduler;

/**
 * A connection capable of scheduling commands.
 */
public interface Connection extends CommandScheduler {

  /**
   * @return {@code true} when {@code error} is an indeterminate data type reported when preparing a statement
   */
  default boolean isIndeterminatePreparedStatementError(Throwable error) {
    return false;
  }

  /**
   * @return The connection tracing policy
   */
  TracingPolicy tracingPolicy();

  /**
   * @return The connection client metrics
   */
  ClientMetrics metrics();

  /**
   * @return the known server address
   */
  SocketAddress server();

  /**
   * @return a database specific discriminant / identifier
   */
  default String system() {
    return "other_sql";
  }

  /**
   * @return the database name
   */
  String database();

  /**
   * @return the database user
   */
  String user();

  /**
   * Initialize the connection with the context for its usage.
   *
   * @param context the context
   */
  void init(ConnectionContext context);

  void close(ConnectionContext holder, Completable<Void> promise);

  /**
   * @return whether the underlying transport uses TLS
   */
  boolean isSsl();

  /**
   * @return whether the connection is valid
   */
  boolean isValid();

  /**
   * @return the connection pipelining limit, that is how many queries can be scheduled on this connection
   * @implNote returns {@literal 1}, the connection does not support pipelining
   */
  default int pipeliningLimit() {
    return 1;
  }

  /**
   * @return the metadata
   */
  DatabaseMetadata databaseMetadata();

  /**
   * @return the most unwrapped connection (e.g. for pooled connections)
   */
  default Connection unwrap() {
    return this;
  }

}

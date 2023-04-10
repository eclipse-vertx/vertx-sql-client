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

package io.vertx.sqlclient.impl;

import io.vertx.core.Promise;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.sqlclient.impl.command.CommandScheduler;
import io.vertx.sqlclient.spi.DatabaseMetadata;

public interface Connection extends CommandScheduler  {

  /**
   * @return {@code true} when {@code error} is an indeterminate data type reported when preparing a statement
   */
  default boolean isIndeterminatePreparedStatementError(Throwable error) {
    return false;
  }

  TracingPolicy tracingPolicy();

  SocketAddress server();

  String database();

  String user();

  ClientMetrics metrics();

  void init(Holder holder);

  boolean isSsl();

  boolean isValid();

  DatabaseMetadata getDatabaseMetaData();

  void close(Holder holder, Promise<Void> promise);

  int getProcessId();

  int getSecretKey();

  interface Holder {

    void handleEvent(Object event);

    void handleClosed();

    void handleException(Throwable err);

  }

  default Connection unwrap() {
    return this;
  }

}

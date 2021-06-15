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

import io.vertx.core.Handler;
import io.vertx.sqlclient.PoolConfig;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;

public class PoolConfigImpl implements PoolConfig {

  private final PoolOptions options;
  private SqlConnectOptions connectOptions;
  private Handler<SqlConnection> connectHook;

  public PoolConfigImpl(PoolOptions options) {
    this.options = options;
  }

  @Override
  public PoolOptions options() {
    return options;
  }

  @Override
  public PoolConfig connectOptions(SqlConnectOptions options) {
    this.connectOptions = options;
    return this;
  }

  @Override
  public Handler<SqlConnection> connectHandler() {
    return connectHook;
  }

  @Override
  public PoolConfig connectHandler(Handler<SqlConnection> handler) {
    connectHook = handler;
    return this;
  }

  @Override
  public SqlConnectOptions determineConnectOptions() {
    return connectOptions;
  }
}

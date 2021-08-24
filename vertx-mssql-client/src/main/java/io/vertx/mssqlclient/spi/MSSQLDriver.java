/*
 * Copyright (C) 2020 IBM Corporation
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
package io.vertx.mssqlclient.spi;

import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLPool;
import io.vertx.mssqlclient.impl.MSSQLConnectionFactory;
import io.vertx.mssqlclient.impl.MSSQLPoolImpl;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

public class MSSQLDriver implements Driver {

  private static final String ACCEPT_URI_REGEX = "(sqlserver)://.*";

  @Override
  public MSSQLPool createPool(Vertx vertx, List<? extends SqlConnectOptions> databases, PoolOptions options) {
    return MSSQLPoolImpl.create((VertxInternal) vertx, databases, options);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof MSSQLConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  @Override
  public ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database) {
    return new MSSQLConnectionFactory((VertxInternal) vertx, MSSQLConnectOptions.wrap(database));
  }

  @Override
  public boolean acceptsUri(String connectionUri) {
    return connectionUri.matches(ACCEPT_URI_REGEX);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends SqlConnectOptions> T getConnectOptions(String connectionUri) {
    return (T) MSSQLConnectOptions.fromUri(connectionUri);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends SqlConnectOptions> T getConnectOptions(String connectionUri, JsonObject json) {
    MSSQLConnectOptions fromUri = MSSQLConnectOptions.fromUri(connectionUri);
    return (T) new MSSQLConnectOptions(fromUri.toJson().mergeIn(json));
  }
}

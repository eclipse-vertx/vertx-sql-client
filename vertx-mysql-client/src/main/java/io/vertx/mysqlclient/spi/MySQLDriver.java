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
package io.vertx.mysqlclient.spi;

import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.mysqlclient.impl.MySQLConnectionFactory;
import io.vertx.mysqlclient.impl.MySQLPoolImpl;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

public class MySQLDriver implements Driver {

  private static final String ACCEPT_URI_REGEX = "(mysql|mariadb)://.*";

  @Override
  public MySQLPool createPool(Vertx vertx, List<? extends SqlConnectOptions> databases, PoolOptions options) {
    return MySQLPoolImpl.create((VertxInternal) vertx, databases, options);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof MySQLConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  @Override
  public ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database) {
    return new MySQLConnectionFactory((VertxInternal) vertx, MySQLConnectOptions.wrap(database));
  }

  @Override
  public boolean acceptsUri(String connectionUri) {
    return connectionUri.matches(ACCEPT_URI_REGEX);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends SqlConnectOptions> T getConnectOptions(String connectionUri) {
    return (T) MySQLConnectOptions.fromUri(connectionUri);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends SqlConnectOptions> T getConnectOptions(String connectionUri, JsonObject json) {
    MySQLConnectOptions fromUri = MySQLConnectOptions.fromUri(connectionUri);
    return (T) new MySQLConnectOptions(fromUri.toJson().mergeIn(json));
  }
}

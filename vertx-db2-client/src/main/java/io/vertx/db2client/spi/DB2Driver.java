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
package io.vertx.db2client.spi;

import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Pool;
import io.vertx.db2client.impl.DB2ConnectionFactory;
import io.vertx.db2client.impl.DB2PoolImpl;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.Driver;
import io.vertx.sqlclient.spi.ConnectionFactory;

import java.util.List;

public class DB2Driver implements Driver {

  private static final String ACCEPT_URI_REGEX = "(db2)://.*";

  @Override
  public DB2Pool createPool(Vertx vertx, List<? extends SqlConnectOptions> databases, PoolOptions options) {
    return DB2PoolImpl.create((VertxInternal) vertx, false, databases, options);
  }

  public DB2Pool createClient(Vertx vertx, List<? extends SqlConnectOptions> servers, PoolOptions options) {
    return DB2PoolImpl.create((VertxInternal) vertx, true, servers, options);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof DB2ConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  @Override
  public ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database) {
    return new DB2ConnectionFactory((VertxInternal) vertx, DB2ConnectOptions.wrap(database));
  }
  
  @Override
  public boolean acceptsUri(String uri) {
    return uri.matches(ACCEPT_URI_REGEX);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T extends SqlConnectOptions> T getConnectOptions(String connectionUri) {
    return (T) DB2ConnectOptions.fromUri(connectionUri);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends SqlConnectOptions> T getConnectOptions(String connectionUri, JsonObject json) {
    DB2ConnectOptions fromUri = DB2ConnectOptions.fromUri(connectionUri);
    return (T) new DB2ConnectOptions(fromUri.toJson().mergeIn(json));
  }
}

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

import io.vertx.core.Vertx;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.impl.MSSQLConnectionFactory;
import io.vertx.mssqlclient.impl.MSSQLConnectionImpl;
import io.vertx.mssqlclient.impl.MSSQLConnectionUriParser;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.GenericDriver;

public class MSSQLDriver extends GenericDriver<MSSQLConnectOptions> {

  private static final String DISCRIMINANT = "mssqlclient";

  public static final MSSQLDriver INSTANCE = new MSSQLDriver();

  @Override
  protected String discriminant() {
    return DISCRIMINANT;
  }

  @Override
  public MSSQLConnectOptions downcast(SqlConnectOptions connectOptions) {
    return connectOptions instanceof MSSQLConnectOptions ? (MSSQLConnectOptions) connectOptions : new MSSQLConnectOptions(connectOptions);
  }

  @Override
  public ConnectionFactory<MSSQLConnectOptions> createConnectionFactory(Vertx vertx, NetClientOptions transportOptions) {
    return new MSSQLConnectionFactory((VertxInternal) vertx);
  }

  @Override
  public MSSQLConnectOptions parseConnectionUri(String uri) {
    JsonObject conf = MSSQLConnectionUriParser.parse(uri, false);
    return conf == null ? null : new MSSQLConnectOptions(conf);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof MSSQLConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  @Override
  public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
    queryBuilder.append('@').append('P').append(1 + index);
    return index;
  }

  @Override
  public SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory<MSSQLConnectOptions> factory, Connection conn) {
    return new MSSQLConnectionImpl(context, factory, conn);
  }
}

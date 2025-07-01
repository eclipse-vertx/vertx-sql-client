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
package io.vertx.oracleclient.spi;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.impl.*;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import io.vertx.sqlclient.spi.connection.ConnectionFactory;
import io.vertx.sqlclient.spi.DriverBase;

import java.util.function.Function;

public class OracleDriver extends DriverBase<OracleConnectOptions> {

  private static final String DISCRIMINANT = "oracleclient";

  private static final Function<Connection, Future<Void>> AFTER_ACQUIRE = conn -> ((OracleJdbcConnection) conn).afterAcquire();
  private static final Function<Connection, Future<Void>> BEFORE_RECYCLE = conn -> ((OracleJdbcConnection) conn).beforeRecycle();

  public static final OracleDriver INSTANCE = new OracleDriver();

  public OracleDriver() {
    super(DISCRIMINANT, AFTER_ACQUIRE, BEFORE_RECYCLE);
  }

  @Override
  public OracleConnectOptions downcast(SqlConnectOptions connectOptions) {
    return connectOptions instanceof OracleConnectOptions ? (OracleConnectOptions) connectOptions : new OracleConnectOptions(connectOptions);
  }

  @Override
  public OracleConnectOptions parseConnectionUri(String uri) {
    JsonObject conf = OracleConnectionUriParser.parse(uri, false);
    return conf == null ? null : new OracleConnectOptions(conf);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof OracleConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  @Override
  public ConnectionFactory<OracleConnectOptions> createConnectionFactory(Vertx vertx, NetClientOptions transportOptions) {
    return new OracleConnectionFactory((VertxInternal) vertx);
  }

  @Override
  public SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory<OracleConnectOptions> factory, Connection connection) {
    return new OracleConnectionImpl(context, factory, connection);
  }
}

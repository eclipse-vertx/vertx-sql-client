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
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.internal.CloseFuture;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.impl.*;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.internal.pool.PoolImpl;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.GenericDriver;

import java.util.function.Function;
import java.util.function.Supplier;

public class OracleDriver extends GenericDriver<OracleConnectOptions> {

  private static final String DISCRIMINANT = "oracleclient";

  public static final OracleDriver INSTANCE = new OracleDriver();

  @Override
  protected String discriminant() {
    return DISCRIMINANT;
  }

  @Override
  public OracleConnectOptions downcast(SqlConnectOptions connectOptions) {
    return connectOptions instanceof OracleConnectOptions ? (OracleConnectOptions) connectOptions : new OracleConnectOptions(connectOptions);
  }

  @Override
  protected Pool newPool(VertxInternal vertx, Handler<SqlConnection> connectHandler, Supplier<Future<OracleConnectOptions>> databases, PoolOptions poolOptions, NetClientOptions transportOptions, CloseFuture closeFuture) {
    Function<Connection, Future<Void>> afterAcquire = conn -> ((OracleJdbcConnection) conn).afterAcquire();
    Function<Connection, Future<Void>> beforeRecycle = conn -> ((OracleJdbcConnection) conn).beforeRecycle();
    ConnectionFactory<OracleConnectOptions> factory = createConnectionFactory(vertx, null);
    PoolImpl pool = new PoolImpl(vertx, this,  false, poolOptions, afterAcquire, beforeRecycle,
      factory, databases, connectHandler, closeFuture);
    pool.init();
    closeFuture.add(factory);
    return pool;
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
  public SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory<OracleConnectOptions> factory, Connection conn) {
    return new OracleConnectionImpl(context, factory, conn);
  }
}

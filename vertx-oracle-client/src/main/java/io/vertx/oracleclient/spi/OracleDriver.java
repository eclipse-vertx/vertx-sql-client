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
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.CloseablePool;
import io.vertx.sqlclient.impl.PoolImpl;
import io.vertx.sqlclient.impl.SqlConnectionInternal;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

import java.util.function.Function;
import java.util.function.Supplier;

public class OracleDriver implements Driver<OracleConnectOptions> {

  private static final String SHARED_CLIENT_KEY = "__vertx.shared.oracleclient";

  public static final OracleDriver INSTANCE = new OracleDriver();

  @Override
  public OracleConnectOptions downcast(SqlConnectOptions connectOptions) {
    return connectOptions instanceof OracleConnectOptions ? (OracleConnectOptions) connectOptions : new OracleConnectOptions(connectOptions);
  }

  @Override
  public Pool newPool(Vertx vertx, Supplier<Future<OracleConnectOptions>> databases, PoolOptions options, NetClientOptions transportOptions, Handler<SqlConnection> connectHandler, CloseFuture closeFuture) {
    VertxInternal vx = (VertxInternal) vertx;
    PoolImpl pool;
    if (options.isShared()) {
      pool = vx.createSharedResource(SHARED_CLIENT_KEY, options.getName(), closeFuture, cf -> newPoolImpl(vx, connectHandler, databases, options, cf));
    } else {
      pool = newPoolImpl(vx, connectHandler, databases, options, closeFuture);
    }
    return new CloseablePool<>(vx, closeFuture, pool);
  }

  private PoolImpl newPoolImpl(VertxInternal vertx, Handler<SqlConnection> connectHandler, Supplier<Future<OracleConnectOptions>> databases, PoolOptions options, CloseFuture closeFuture) {
    Function<Connection, Future<Void>> afterAcquire = conn -> ((OracleJdbcConnection) conn).afterAcquire();
    Function<Connection, Future<Void>> beforeRecycle = conn -> ((OracleJdbcConnection) conn).beforeRecycle();
    ConnectionFactory<OracleConnectOptions> factory = createConnectionFactory(vertx, null);
    PoolImpl pool = new PoolImpl(vertx, this,  false, options, afterAcquire, beforeRecycle, context -> factory.connect(context, databases.get()), connectHandler, closeFuture);
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

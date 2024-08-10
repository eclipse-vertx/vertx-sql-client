/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
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
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.impl.*;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolImpl;
import io.vertx.sqlclient.impl.SingletonSupplier;
import io.vertx.sqlclient.impl.SqlConnectionInternal;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

import java.util.function.Function;
import java.util.function.Supplier;

public class OracleDriver implements Driver {

  private static final String SHARED_CLIENT_KEY = "__vertx.shared.oracleclient";

  public static final OracleDriver INSTANCE = new OracleDriver();

  @Override
  public Pool newPool(Vertx vertx, Supplier<? extends Future<? extends SqlConnectOptions>> databases, PoolOptions options, CloseFuture closeFuture) {
    VertxInternal vx = (VertxInternal) vertx;
    PoolImpl pool;
    if (options.isShared()) {
      pool = vx.createSharedClient(SHARED_CLIENT_KEY, options.getName(), closeFuture, cf -> newPoolImpl(vx, databases, options, cf));
    } else {
      pool = newPoolImpl(vx, databases, options, closeFuture);
    }
    return new OraclePoolImpl(vx, closeFuture, pool);
  }

  private PoolImpl newPoolImpl(VertxInternal vertx, Supplier<? extends Future<? extends SqlConnectOptions>> databases, PoolOptions options, CloseFuture closeFuture) {
    Function<Connection, Future<Void>> afterAcquire = conn -> ((OracleJdbcConnection) conn).afterAcquire();
    Function<Connection, Future<Void>> beforeRecycle = conn -> ((OracleJdbcConnection) conn).beforeRecycle();
    PoolImpl pool = new PoolImpl(vertx, this,  false, options, afterAcquire, beforeRecycle, closeFuture);
    ConnectionFactory factory = createConnectionFactory(vertx, databases);
    pool.connectionProvider(context -> factory.connect(context, databases.get()));
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
  public ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database) {
    return new OracleConnectionFactory((VertxInternal) vertx, SingletonSupplier.wrap(database));
  }

  @Override
  public ConnectionFactory createConnectionFactory(Vertx vertx, Supplier<? extends Future<? extends SqlConnectOptions>> database) {
    return new OracleConnectionFactory((VertxInternal) vertx, database);
  }

  @Override
  public SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory factory, Connection conn) {
    return new OracleConnectionImpl(context, factory, conn);
  }
}

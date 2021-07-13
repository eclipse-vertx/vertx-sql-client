/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracle.impl;

import io.vertx.core.Promise;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.net.NetClientOptions;
import io.vertx.oracle.OracleConnectOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.SqlConnectionFactoryBase;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.datasource.OracleDataSource;

import java.util.concurrent.CompletionStage;

import static io.vertx.oracle.impl.OracleDatabaseHelper.createDataSource;

public class OracleConnectionFactory extends SqlConnectionFactoryBase implements ConnectionFactory {

  private final OracleConnectOptions options;
  private final OracleDataSource datasource;

  protected OracleConnectionFactory(VertxInternal vertx, OracleConnectOptions options) {
    super(vertx, options);
    this.options = options;
    this.datasource = createDataSource(options);
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions options) {

  }

  @Override
  protected void configureNetClientOptions(NetClientOptions netClientOptions) {

  }

  @Override
  protected void doConnectInternal(Promise<Connection> promise) {
    PromiseInternal<Connection> promiseInternal = (PromiseInternal<Connection>) promise;
    EventLoopContext context = ConnectionFactory.asEventLoopContext(promiseInternal.context());
    CompletionStage<OracleConnection> stage = Helper
      .getOrHandleSQLException(() -> datasource.createConnectionBuilder().buildAsyncOracle());

    Helper.contextualize(stage, context)
      .map(c -> new CommandHandler(context, options, c))
      .onComplete(ar -> promise.handle(ar.map(x -> x)));
  }

  public OracleConnectOptions options() {
    return options;
  }
}

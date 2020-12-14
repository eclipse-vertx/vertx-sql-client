/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.sqlclient.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.sqlclient.SqlConnectOptions;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

/**
 * An abstract connection factory for creating database connections
 */
public abstract class SqlConnectionFactoryBase implements ConnectionFactory {

  protected final NetClient netClient;
  protected final EventLoopContext context;
  protected final SocketAddress socketAddress;
  protected final String username;
  protected final String password;
  protected final String database;
  protected final Map<String, String> properties;

  // cache
  protected final boolean cachePreparedStatements;
  protected final int preparedStatementCacheSize;
  protected final Predicate<String> preparedStatementCacheSqlFilter;

  // close hook
  protected final CloseFuture clientCloseFuture = new CloseFuture();

  // auto-retry
  private final int reconnectAttempts;
  private final long reconnectInterval;

  protected SqlConnectionFactoryBase(EventLoopContext context, SqlConnectOptions options) {
    this.context = context;
    this.socketAddress = options.getSocketAddress();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.database = options.getDatabase();
    this.properties = options.getProperties() == null ? null : Collections.unmodifiableMap(options.getProperties());

    this.cachePreparedStatements = options.getCachePreparedStatements();
    this.preparedStatementCacheSize = options.getPreparedStatementCacheMaxSize();
    this.preparedStatementCacheSqlFilter = options.getPreparedStatementCacheSqlFilter();

    this.reconnectAttempts = options.getReconnectAttempts();
    this.reconnectInterval = options.getReconnectInterval();

    initializeConfiguration(options);

    NetClientOptions netClientOptions = new NetClientOptions(options);
    configureNetClientOptions(netClientOptions);
    netClientOptions.setReconnectAttempts(0); // auto-retry is handled on the protocol level instead of network level
    this.netClient = context.owner().createNetClient(netClientOptions, clientCloseFuture);
  }

  @Override
  public void connect(Promise<Connection> promise) {
    context.emit(promise, p -> doConnectWithRetry(promise, reconnectAttempts));
  }

  @Override
  public void close(Promise<Void> promise) {
    clientCloseFuture.close(promise);
  }

  private void doConnectWithRetry(Promise<Connection> promise, int remainingAttempts) {
    Promise<Connection> promise0 = context.promise();
    promise0.future().onComplete(ar -> {
      if (ar.succeeded()) {
        promise.complete(ar.result());
      } else {
        if (remainingAttempts >= 0) {
          context.owner().setTimer(reconnectInterval, id -> {
            doConnectWithRetry(promise, remainingAttempts - 1);
          });
        } else {
          promise.fail(ar.cause());
        }
      }
    });
    doConnectInternal(promise0);
  }

  /**
   * Initialize the configuration after the common configuration have been initialized.
   *
   * @param options the concrete options for initializing configuration by a specific connection factory.
   */
  protected abstract void initializeConfiguration(SqlConnectOptions options);

  /**
   * Apply the configuration to the {@link NetClientOptions NetClientOptions} for connecting to the database.
   *
   * @param netClientOptions NetClient options to apply
   */
  protected abstract void configureNetClientOptions(NetClientOptions netClientOptions);

  /**
   * Perform establishing connection to the server.
   *
   * @param promise the result handler
   */
  protected abstract void doConnectInternal(Promise<Connection> promise);

}

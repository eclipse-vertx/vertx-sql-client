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

import io.vertx.core.Promise;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
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

  protected final VertxInternal vertx;
  protected final NetClient netClient;
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

  protected SqlConnectionFactoryBase(VertxInternal vertx, SqlConnectOptions optionsBase) {
    this.vertx = vertx;
    this.properties = optionsBase.getProperties() == null ? null : Collections.unmodifiableMap(optionsBase.getProperties());

    this.cachePreparedStatements = optionsBase.getCachePreparedStatements();
    this.preparedStatementCacheSize = optionsBase.getPreparedStatementCacheMaxSize();
    this.preparedStatementCacheSqlFilter = optionsBase.getPreparedStatementCacheSqlFilter();

    this.reconnectAttempts = optionsBase.getReconnectAttempts();
    this.reconnectInterval = optionsBase.getReconnectInterval();

    initializeConfiguration(optionsBase);

    NetClientOptions netClientOptions = new NetClientOptions(optionsBase);
    configureNetClientOptions(netClientOptions);
    netClientOptions.setReconnectAttempts(0); // auto-retry is handled on the protocol level instead of network level
    this.netClient = vertx.createNetClient(netClientOptions, clientCloseFuture);
  }

  @Override
  public void connect(SocketAddress server, String user, String password, String database, Promise<Connection> promise) {
    PromiseInternal<Connection> promiseInternal = (PromiseInternal<Connection>) promise;
    ContextInternal context = promiseInternal.context();
    context.emit(promise, p -> doConnectWithRetry(server, user, password, database, promiseInternal, reconnectAttempts));
  }

  @Override
  public void close(Promise<Void> promise) {
    clientCloseFuture.close(promise);
  }

  private void doConnectWithRetry(SocketAddress server, String username, String password, String database, PromiseInternal<Connection> promise, int remainingAttempts) {
    ContextInternal context = promise.context();
    Promise<Connection> promise0 = context.promise();
    promise0.future().onComplete(ar -> {
      if (ar.succeeded()) {
        promise.complete(ar.result());
      } else {
        if (remainingAttempts >= 0) {
          context.owner().setTimer(reconnectInterval, id -> {
            doConnectWithRetry(server, username, password, database, promise, remainingAttempts - 1);
          });
        } else {
          promise.fail(ar.cause());
        }
      }
    });
    doConnectInternal(server, username, password, database, promise0);
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
   * @param server
   * @param username
   * @param password
   * @param database
   * @param promise the result handler
   */
  protected abstract void doConnectInternal(SocketAddress server, String username, String password, String database, Promise<Connection> promise);

}

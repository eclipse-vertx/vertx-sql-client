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
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.ConnectionFactory;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

/**
 * An base connection factory for creating database connections
 */
public abstract class ConnectionFactoryBase implements ConnectionFactory {

  public static final String NATIVE_TRANSPORT_REQUIRED = "The Vertx instance must use a native transport in order to connect to connect through domain sockets";

  protected final VertxInternal vertx;
  protected final NetClient netClient;
  protected final Map<String, String> properties;
  protected final SqlConnectOptions options;
  protected final SocketAddress server;
  protected final String user;
  protected final String password;
  protected final String database;

  // cache
  protected final boolean cachePreparedStatements;
  protected final int preparedStatementCacheSize;
  protected final Predicate<String> preparedStatementCacheSqlFilter;

  // close hook
  protected final CloseFuture clientCloseFuture = new CloseFuture();

  // auto-retry
  private final int reconnectAttempts;
  private final long reconnectInterval;

  protected ConnectionFactoryBase(VertxInternal vertx, SqlConnectOptions options) {

    // check we can do domain sockets
    if (options.isUsingDomainSocket() && !vertx.isNativeTransportEnabled()) {
      throw new IllegalArgumentException(NATIVE_TRANSPORT_REQUIRED);
    }

    this.vertx = vertx;
    this.properties = options.getProperties() == null ? null : Collections.unmodifiableMap(options.getProperties());
    this.server = options.getSocketAddress();
    this.options = options;
    this.user = options.getUser();
    this.password = options.getPassword();
    this.database = options.getDatabase();

    this.cachePreparedStatements = options.getCachePreparedStatements();
    this.preparedStatementCacheSize = options.getPreparedStatementCacheMaxSize();
    this.preparedStatementCacheSqlFilter = options.getPreparedStatementCacheSqlFilter();

    this.reconnectAttempts = options.getReconnectAttempts();
    this.reconnectInterval = options.getReconnectInterval();

    initializeConfiguration(options);

    NetClientOptions netClientOptions = new NetClientOptions(options);
    configureNetClientOptions(netClientOptions);
    netClientOptions.setReconnectAttempts(0); // auto-retry is handled on the protocol level instead of network level
    this.netClient = vertx.createNetClient(netClientOptions, clientCloseFuture);
  }

  public static EventLoopContext asEventLoopContext(ContextInternal ctx) {
    if (ctx instanceof EventLoopContext) {
      return (EventLoopContext) ctx;
    } else {
      return ctx.owner().createEventLoopContext(ctx.nettyEventLoop(), ctx.workerPool(), ctx.classLoader());
    }
  }

  public Future<Connection> connect(EventLoopContext context) {
    PromiseInternal<Connection> promise = context.promise();
    context.emit(promise, p -> doConnectWithRetry(server, user, password, database, p, reconnectAttempts));
    return promise.future();
  }

  @Override
  public void close(Promise<Void> promise) {
    clientCloseFuture.close(promise);
  }

  private void doConnectWithRetry(SocketAddress server, String username, String password, String database, PromiseInternal<Connection> promise, int remainingAttempts) {
    EventLoopContext ctx = (EventLoopContext) promise.context();
    doConnectInternal(server, username, password, database, ctx).onComplete(ar -> {
      if (ar.succeeded()) {
        promise.complete(ar.result());
      } else {
        if (remainingAttempts > 0) {
          ctx.owner().setTimer(reconnectInterval, id -> {
            doConnectWithRetry(server, username, password, database, promise, remainingAttempts - 1);
          });
        } else {
          promise.fail(ar.cause());
        }
      }
    });
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
   * Establish a connection to the server.
   */
  protected abstract Future<Connection> doConnectInternal(SocketAddress server, String username, String password, String database, EventLoopContext context);

}

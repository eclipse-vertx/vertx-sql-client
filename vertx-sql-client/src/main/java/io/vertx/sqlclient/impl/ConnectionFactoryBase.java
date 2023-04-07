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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.impl.NetClientBuilder;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.spi.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * An base connection factory for creating database connections
 */
public abstract class ConnectionFactoryBase<C extends SqlConnectOptions> implements ConnectionFactory<C> {

  public static final String NATIVE_TRANSPORT_REQUIRED = "The Vertx instance must use a native transport in order to connect to connect through domain sockets";

  protected final VertxInternal vertx;
  private final Map<JsonObject, NetClient> clients;
  protected final Supplier<C> options;

  // close hook
  protected final CloseFuture clientCloseFuture = new CloseFuture();

  protected ConnectionFactoryBase(VertxInternal vertx, Supplier<C> options) {
    this.vertx = vertx;
    this.options = options;
    this.clients = new HashMap<>();
  }

  private NetClient createNetClient(NetClientOptions options) {
    options.setReconnectAttempts(0); // auto-retry is handled on the protocol level instead of network level
    return new NetClientBuilder(vertx, options).closeFuture(clientCloseFuture).build();
  }

  protected NetClient netClient(NetClientOptions options) {
    if (options.getClass() != NetClientOptions.class) {
      options = new NetClientOptions(options);
    }
    JsonObject key = options.toJson();
    NetClient client;
    synchronized (this) {
      client = clients.get(key);
      if (client == null) {
        client = createNetClient(options);
        clients.put(key, client);
      }
    }
    return client;
  }

  public static EventLoopContext asEventLoopContext(ContextInternal ctx) {
    if (ctx instanceof EventLoopContext) {
      return (EventLoopContext) ctx;
    } else {
      return ctx.owner().createEventLoopContext(ctx.nettyEventLoop(), ctx.workerPool(), ctx.classLoader());
    }
  }

  public Future<Connection> connect(EventLoopContext context, C options) {
    PromiseInternal<Connection> promise = context.promise();
    context.emit(promise, p -> doConnectWithRetry(options, p, options.getReconnectAttempts()));
    return promise.future();
  }

  @Override
  public Future<SqlConnection> connect(Context context) {
    return connect(context, options.get());
  }

  @Override
  public void close(Promise<Void> promise) {
    clientCloseFuture.close(promise);
  }

  private void doConnectWithRetry(C options, PromiseInternal<Connection> promise, int remainingAttempts) {
    EventLoopContext ctx = (EventLoopContext) promise.context();
    doConnectInternal(options, ctx).onComplete(ar -> {
      if (ar.succeeded()) {
        promise.complete(ar.result());
      } else {
        if (remainingAttempts > 0) {
          ctx.owner().setTimer(options.getReconnectInterval(), id -> {
            doConnectWithRetry(options, promise, remainingAttempts - 1);
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
  protected abstract Future<Connection> doConnectInternal(C options, EventLoopContext context);

}

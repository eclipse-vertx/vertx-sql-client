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

import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.internal.CloseSequence;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.spi.ConnectionFactory;

/**
 * An base connection factory for creating database connections
 */
public abstract class ConnectionFactoryBase<C extends SqlConnectOptions> implements ConnectionFactory<C> {

  public static final String NATIVE_TRANSPORT_REQUIRED = "The Vertx instance must use a native transport in order to connect to connect through domain sockets";

  protected final VertxInternal vertx;
  protected final NetClient client;
  protected final NetClientOptions tcpOptions;

  // close hook
  protected final CloseSequence clientCloseFuture = new CloseSequence(this::doClose);

  protected ConnectionFactoryBase(VertxInternal vertx) {
    this(vertx, new NetClientOptions());
  }

  protected ConnectionFactoryBase(VertxInternal vertx, NetClientOptions tcpOptions) {
    this.vertx = vertx;
    this.client = vertx.createNetClient(new NetClientOptions(tcpOptions).setReconnectAttempts(0)); // auto-retry is handled on the protocol level instead of network level
    this.tcpOptions = tcpOptions;
  }

  private void doClose(Completable<Void> p) {
    client.close().onComplete(ar -> p.succeed());
  }

  public static ContextInternal asEventLoopContext(ContextInternal ctx) {
    return ctx.owner().contextBuilder()
      .withEventLoop(ctx.nettyEventLoop())
      .withWorkerPool(ctx.workerPool())
      .build();
  }

  public Future<Connection> connect(ContextInternal context, C options) {
    PromiseInternal<Connection> promise = context.promise();
    context.emit(promise, p -> doConnectWithRetry(options, p, options.getReconnectAttempts()));
    return promise.future();
  }

  @Override
  public void close(Completable<Void> promise) {
    clientCloseFuture.close(promise);
  }

  private void doConnectWithRetry(C options, PromiseInternal<Connection> promise, int remainingAttempts) {
    ContextInternal ctx = promise.context();
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
   * Establish a connection to the server.
   */
  protected abstract Future<Connection> doConnectInternal(C options, ContextInternal context);

}

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
package io.vertx.oracleclient.impl.commands;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.internal.ContextInternal;
import io.vertx.oracleclient.impl.Helper.SQLBlockingCodeHandler;
import io.vertx.sqlclient.internal.command.CommandBase;
import io.vertx.sqlclient.internal.command.CommandResponse;
import oracle.jdbc.OracleConnection;

import java.util.concurrent.Flow;

import static io.vertx.oracleclient.impl.FailureUtil.sanitize;
import static io.vertx.oracleclient.impl.Helper.SQLBlockingTaskHandler;

public abstract class OracleCommand<T> {

  protected final OracleConnection oracleConnection;
  protected final ContextInternal connectionContext;
  private CommandResponse<T> response;

  protected OracleCommand(OracleConnection oracleConnection, ContextInternal connectionContext) {
    this.oracleConnection = oracleConnection;
    this.connectionContext = connectionContext;
  }

  public final Future<Void> processCommand(CommandBase<T> cmd) {
    return execute().andThen(ar -> {
      if (ar.succeeded()) {
        response = CommandResponse.success(ar.result());
      } else {
        response = CommandResponse.failure(ar.cause());
      }
      response.cmd = cmd;
    }).mapEmpty();
  }

  protected abstract Future<T> execute();

  public final <U> Future<U> executeBlocking(SQLBlockingCodeHandler<U> blockingCodeHandler) {
    return connectionContext.executeBlocking(blockingCodeHandler, false);
  }

  public final Future<Void> executeBlocking(SQLBlockingTaskHandler blockingTaskHandler) {
    return connectionContext.executeBlocking(blockingTaskHandler, false);
  }

  public final <U> Future<U> first(Flow.Publisher<U> publisher) {
    Promise<U> promise = connectionContext.promise();
    publisher.subscribe(new Flow.Subscriber<>() {
      volatile Flow.Subscription subscription;

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(1);
      }

      @Override
      public void onNext(U item) {
        promise.tryComplete(item);
        subscription.cancel();
      }

      @Override
      public void onError(Throwable throwable) {
        promise.fail(sanitize(throwable));
      }

      @Override
      public void onComplete() {
        // Use tryComplete as the completion signal can be sent even if we cancelled.
        // Also, for Publisher<Void> we would get in this case.
        promise.tryComplete(null);
      }
    });
    return promise.future();
  }

  public final void fireResponse() {
    response.fire();
  }
}

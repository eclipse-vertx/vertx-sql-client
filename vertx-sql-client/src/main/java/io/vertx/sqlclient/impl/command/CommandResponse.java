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

package io.vertx.sqlclient.impl.command;

import io.vertx.core.Future;
import io.vertx.core.AsyncResult;
import io.vertx.core.impl.NoStackTraceThrowable;

public class CommandResponse<R> {

  public static <R> CommandResponse<R> failure(String msg) {
    return failure(new NoStackTraceThrowable(msg));
  }

  public static <R> CommandResponse<R> failure(Throwable cause) {
    return new CommandResponse<>(Future.failedFuture(cause));
  }

  public static <R> CommandResponse<R> success(R result) {
    return new CommandResponse<>(Future.succeededFuture(result));
  }

  // The connection that executed the command
  public CommandBase<R> cmd;
  private final AsyncResult<R> res;

  public CommandResponse(AsyncResult<R> res) {
    this.res = res;
  }

  public AsyncResult<R> toAsyncResult() {
    return res;
  }

  public void fire() {
    if (cmd.handler != null) {
      cmd.handler.handle(toAsyncResult());
    }
  }
}

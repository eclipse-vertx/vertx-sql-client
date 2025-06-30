/*
 * Copyright (c) 2011-2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.sqlclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.sqlclient.internal.command.CommandBase;

public class CommandMessage<R, C extends CommandBase<R>> {

  public Completable<R> handler;
  public final C cmd;

  public CommandMessage(C cmd) {
    this.cmd = cmd;
  }

  public final void fail(Throwable err) {
    complete(Future.failedFuture(err));
  }

  public final void fail(String failureMsg) {
    complete(Future.failedFuture(failureMsg));
  }

  public final void complete(AsyncResult<R> resp) {
    if (handler != null) {
      handler.complete(resp.result(), resp.cause());
    }
  }
}

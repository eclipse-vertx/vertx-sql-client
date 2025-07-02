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

package io.vertx.sqlclient.codec;

import io.vertx.core.VertxException;

public class CommandResponse<R> {

  public static <R> CommandResponse<R> failure(String msg) {
    return failure(VertxException.noStackTrace(msg));
  }

  public static <R> CommandResponse<R> failure(Throwable cause) {
    return new CommandResponse<>(null, cause);
  }

  public static <R> CommandResponse<R> success(R result) {
    return new CommandResponse<>(result, null);
  }

  // The connection that executed the command
  final R result;
  final Throwable failure;

  private CommandResponse(R result, Throwable failure) {
    this.result = result;
    this.failure = failure;
  }
}

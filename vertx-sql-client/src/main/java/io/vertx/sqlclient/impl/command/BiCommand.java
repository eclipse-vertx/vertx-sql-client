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

import io.vertx.core.AsyncResult;

import java.util.function.Function;

public class BiCommand<T, R> extends CommandBase<R> {

  public final CommandBase<T> first;
  public final Function<T, AsyncResult<CommandBase<R>>> then;

  public BiCommand(CommandBase<T> first, Function<T, AsyncResult<CommandBase<R>>> then) {
    this.first = first;
    this.then = then;
  }
}

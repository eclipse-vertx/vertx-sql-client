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
package io.vertx.sqlclient.impl;

import io.vertx.core.Future;

import java.util.function.Supplier;

public class SingletonSupplier<C> implements Supplier<Future<C>> {

  public static <C> Supplier<Future<C>> wrap(C connectOptions) {
    return new SingletonSupplier<>(connectOptions);
  }

  private final C instance;
  private final Future<C> fut;

  private SingletonSupplier(C instance) {
    this.instance = instance;
    this.fut = Future.succeededFuture(instance);
  }

  public C unwrap() {
    return instance;
  }

  @Override
  public Future<C> get() {
    return fut;
  }
}

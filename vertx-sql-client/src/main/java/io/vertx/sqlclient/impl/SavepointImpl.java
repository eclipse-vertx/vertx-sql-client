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

import io.vertx.core.Future;
import io.vertx.sqlclient.Savepoint;

public class SavepointImpl implements Savepoint {

  private enum State {
    ACTIVE,
    PENDING,
    RELEASED
  }

  private final TransactionImpl transaction;
  private final String name;
  private State state;

  public SavepointImpl(TransactionImpl transaction, String name) {
    this.transaction = transaction;
    this.name = name;
    this.state = State.ACTIVE;
  }

  @Override
  public Future<Void> rollback() {
    return execute(false, () -> transaction.rollbackToSavepoint(name));
  }

  @Override
  public Future<Void> release() {
    return execute(true, () -> transaction.releaseSavepoint(name));
  }

  private Future<Void> execute(boolean release, Action action) {
    synchronized (this) {
      if (state == State.RELEASED) {
        return transaction.failedFuture("Savepoint already released");
      }
      if (state == State.PENDING) {
        return transaction.failedFuture("Savepoint command already in progress");
      }
      state = State.PENDING;
    }
    return action.execute().andThen(ar -> {
      synchronized (SavepointImpl.this) {
        if (ar.succeeded()) {
          state = release ? State.RELEASED : State.ACTIVE;
        } else {
          state = State.ACTIVE;
        }
      }
    });
  }

  @FunctionalInterface
  private interface Action {
    Future<Void> execute();
  }
}

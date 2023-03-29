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
import io.vertx.core.impl.ContextInternal;
import oracle.jdbc.OracleConnection;

import java.util.Objects;

public class OracleCloseConnectionCommand extends OracleCommand<Void> {

  private final Promise<Void> closePromise;

  public OracleCloseConnectionCommand(OracleConnection oracleConnection, ContextInternal connectionContext, Promise<Void> closePromise) {
    super(oracleConnection, connectionContext);
    this.closePromise = Objects.requireNonNull(closePromise);
  }

  @Override
  protected Future<Void> execute() {
    return executeBlocking(() -> oracleConnection.closeAsyncOracle())
      .compose(publisher -> first(publisher))
      .andThen(closePromise);
  }
}

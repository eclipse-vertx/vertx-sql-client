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
import io.vertx.core.impl.ContextInternal;
import oracle.jdbc.OracleConnection;

public class OracleCloseStatementCommand extends OracleCommand<Void> {

  public OracleCloseStatementCommand(OracleConnection oracleConnection, ContextInternal connectionContext) {
    super(oracleConnection, connectionContext);
  }

  @Override
  protected Future<Void> execute() {
    return Future.succeededFuture();
  }
}

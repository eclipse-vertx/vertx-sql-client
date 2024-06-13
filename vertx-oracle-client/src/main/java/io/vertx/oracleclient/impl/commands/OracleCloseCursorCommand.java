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
import io.vertx.core.internal.ContextInternal;
import io.vertx.oracleclient.impl.RowReader;
import oracle.jdbc.OracleConnection;

public class OracleCloseCursorCommand extends OracleCommand<Void> {

  private final RowReader<?, ?> reader;

  public OracleCloseCursorCommand(OracleConnection oracleConnection, ContextInternal connectionContext, RowReader<?, ?> reader) {
    super(oracleConnection, connectionContext);
    this.reader = reader;
  }

  @Override
  protected Future<Void> execute() {
    if (reader == null) {
      return Future.succeededFuture();
    }
    return reader.close();
  }
}

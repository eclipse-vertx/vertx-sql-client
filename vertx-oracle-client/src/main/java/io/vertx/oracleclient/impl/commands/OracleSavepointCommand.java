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
package io.vertx.oracleclient.impl.commands;

import io.vertx.core.Future;
import io.vertx.core.internal.ContextInternal;
import io.vertx.sqlclient.spi.protocol.SavepointCommand;
import oracle.jdbc.OracleConnection;

import java.sql.Statement;

/**
 * Runs a savepoint statement on the JDBC connection.
 *
 * <p>Oracle names savepoints with the standard syntax, but has no statement that
 * releases one, so {@link SavepointCommand.Kind#RELEASE} never reaches this command.
 */
public class OracleSavepointCommand<R> extends OracleCommand<R> {

  private final SavepointCommand<R> op;

  private OracleSavepointCommand(OracleConnection oracleConnection, ContextInternal connectionContext, SavepointCommand<R> op) {
    super(oracleConnection, connectionContext);
    this.op = op;
  }

  public static <U> OracleSavepointCommand<U> create(OracleConnection oracleConnection, ContextInternal connectionContext, SavepointCommand<U> cmd) {
    return new OracleSavepointCommand<>(oracleConnection, connectionContext, cmd);
  }

  @Override
  protected Future<R> execute() {
    if (op.kind() == SavepointCommand.Kind.RELEASE) {
      return connectionContext.failedFuture(new UnsupportedOperationException(
        "Releasing a savepoint is not supported by Oracle"));
    }
    String sql = op.sql();
    return executeBlocking(() -> {
      try (Statement statement = oracleConnection.createStatement()) {
        statement.execute(sql);
      }
    }).map(op.result());
  }
}

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
import io.vertx.oracleclient.impl.Helper.SQLFutureMapper;
import io.vertx.sqlclient.spi.protocol.TxCommand;
import oracle.jdbc.OracleConnection;

import static io.vertx.sqlclient.spi.protocol.TxCommand.Kind.BEGIN;
import static io.vertx.sqlclient.spi.protocol.TxCommand.Kind.COMMIT;

public class OracleTransactionCommand<R> extends OracleCommand<R> {

  private final TxCommand<R> op;

  private OracleTransactionCommand(OracleConnection oracleConnection, ContextInternal connectionContext, TxCommand<R> op) {
    super(oracleConnection, connectionContext);
    this.op = op;
  }

  public static <U> OracleTransactionCommand<U> create(OracleConnection oracleConnection, ContextInternal connectionContext, TxCommand<U> cmd) {
    return new OracleTransactionCommand<>(oracleConnection, connectionContext, cmd);
  }

  @Override
  protected Future<R> execute() {
    Future<Void> result;
    if (op.kind() == BEGIN) {
      result = begin();
    } else if (op.kind() == COMMIT) {
      result = commit();
    } else {
      result = rollback();
    }
    return result.map(op.result());
  }

  private Future<Void> begin() {
    return executeBlocking(() -> {
      int isolation = oracleConnection.getTransactionIsolation();
      oracleConnection.setAutoCommit(false);
      oracleConnection.setTransactionIsolation(isolation);
    });
  }

  private Future<Void> commit() {
    return executeBlocking(() -> oracleConnection.getAutoCommit())
      .compose((SQLFutureMapper<Boolean, Void>) autoCommit -> {
        return autoCommit ? Future.succeededFuture() : first(oracleConnection.commitAsyncOracle());
      })
      .eventually(() -> executeBlocking(() -> oracleConnection.setAutoCommit(true)));
  }

  private Future<Void> rollback() {
    return executeBlocking(() -> oracleConnection.getAutoCommit())
      .compose((SQLFutureMapper<Boolean, Void>) autoCommit -> {
        return autoCommit ? Future.succeededFuture() : first(oracleConnection.rollbackAsyncOracle());
      })
      .eventually(() -> executeBlocking(() -> oracleConnection.setAutoCommit(true)));
  }
}

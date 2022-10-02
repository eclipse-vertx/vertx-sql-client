/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
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
import io.vertx.oracleclient.impl.Helper;
import io.vertx.oracleclient.impl.Helper.SQLFutureMapper;
import io.vertx.sqlclient.impl.command.TxCommand;
import oracle.jdbc.OracleConnection;

import static io.vertx.oracleclient.impl.Helper.executeBlocking;
import static io.vertx.sqlclient.impl.command.TxCommand.Kind.BEGIN;
import static io.vertx.sqlclient.impl.command.TxCommand.Kind.COMMIT;

public class OracleTransactionCommand<R> extends AbstractCommand<R> {

  private final TxCommand<R> op;

  public OracleTransactionCommand(TxCommand<R> op) {
    this.op = op;
  }

  @Override
  public Future<R> execute(OracleConnection conn, ContextInternal context) {
    Future<Void> result;
    if (op.kind == BEGIN) {
      result = begin(conn, context);
    } else if (op.kind == COMMIT) {
      result = commit(conn, context);
    } else {
      result = rollback(conn, context);
    }
    return result.map(op.result);
  }

  private Future<Void> begin(OracleConnection conn, ContextInternal context) {
    return executeBlocking(context, () -> {
      int isolation = conn.getTransactionIsolation();
      conn.setAutoCommit(false);
      conn.setTransactionIsolation(isolation);
    });
  }

  private Future<Void> commit(OracleConnection conn, ContextInternal context) {
    return executeBlocking(context, () -> conn.getAutoCommit())
      .compose((SQLFutureMapper<Boolean, Void>) autoCommit -> {
        return autoCommit ? Future.succeededFuture() : Helper.first(conn.commitAsyncOracle(), context);
      })
      .eventually(v -> executeBlocking(context, () -> conn.setAutoCommit(true)));
  }

  private Future<Void> rollback(OracleConnection conn, ContextInternal context) {
    return executeBlocking(context, () -> conn.getAutoCommit())
      .compose((SQLFutureMapper<Boolean, Void>) autoCommit -> {
        return autoCommit ? Future.succeededFuture() : Helper.first(conn.rollbackAsyncOracle(), context);
      })
      .eventually(v -> executeBlocking(context, () -> conn.setAutoCommit(true)));
  }
}

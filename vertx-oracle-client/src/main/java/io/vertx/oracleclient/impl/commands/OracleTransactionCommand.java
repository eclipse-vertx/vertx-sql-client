/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl.commands;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.impl.ContextInternal;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.impl.Helper;
import io.vertx.sqlclient.impl.command.TxCommand;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;

import java.sql.SQLException;
import java.util.concurrent.Flow;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;
import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

public class OracleTransactionCommand<R> extends AbstractCommand<R> {

  private final TxCommand<R> op;

  public OracleTransactionCommand(TxCommand<R> op, OracleConnectOptions options) {
    super(options);
    this.op = op;
  }

  @Override
  public Future<R> execute(OracleConnection conn, ContextInternal context) {
    if (op.kind == TxCommand.Kind.BEGIN) {
      return begin(conn, context)
        .map(x -> op.result);
    } else if (op.kind == TxCommand.Kind.COMMIT) {
      return commit(conn, context)
        .map(x -> op.result)
        .onComplete(x -> Helper.runOrHandleSQLException(() -> conn.setAutoCommit(false)));
    } else {
      return rollback(conn, context)
        .map(x -> op.result)
        .onComplete(x -> Helper.runOrHandleSQLException(() -> conn.setAutoCommit(false)));
    }
  }

  private Future<Void> begin(OracleConnection conn, ContextInternal context) {
    int isolation = Helper.getOrHandleSQLException(conn::getTransactionIsolation);
    String isolationLevel;
    switch (isolation) {
      case TRANSACTION_READ_COMMITTED:
        isolationLevel = "READ COMMITTED";
        break;
      case TRANSACTION_SERIALIZABLE:
        isolationLevel = "SERIALIZABLE";
        break;
      default:
        throw new IllegalArgumentException("Invalid isolation level: " + isolation);
    }

    try {
      conn.setAutoCommit(false);
      Flow.Publisher<Boolean> publisher = conn
        .prepareStatement("SET TRANSACTION ISOLATION LEVEL " + isolationLevel)
        .unwrap(OraclePreparedStatement.class)
        .executeAsyncOracle();
      return Helper.first(publisher, context)
        .map(x -> null);
    } catch (SQLException e) {
      return context.failedFuture(e);
    }
  }

  private Future<Void> commit(OracleConnection conn, ContextInternal context) {
    try {
      if (conn.getAutoCommit()) {
        return context.succeededFuture();
      } else {
        return Helper.first(conn.commitAsyncOracle(), context);
      }
    } catch (SQLException e) {
      return context.failedFuture(e);
    }
  }

  private Future<Void> rollback(OracleConnection conn, ContextInternal context) {
    try {
      if (conn.getAutoCommit()) {
        return context.succeededFuture();
      } else {
        return Helper.first(conn.rollbackAsyncOracle(), context);
      }
    } catch (SQLException e) {
      return context.failedFuture(e);
    }
  }
}

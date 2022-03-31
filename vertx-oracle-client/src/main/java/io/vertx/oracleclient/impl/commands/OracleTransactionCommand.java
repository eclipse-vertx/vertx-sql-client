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
import io.vertx.oracleclient.impl.Helper.SQLBlockingCodeHandler;
import io.vertx.oracleclient.impl.Helper.SQLFutureMapper;
import io.vertx.sqlclient.impl.command.TxCommand;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;

import java.util.concurrent.Flow;

import static io.vertx.sqlclient.impl.command.TxCommand.Kind.BEGIN;
import static io.vertx.sqlclient.impl.command.TxCommand.Kind.COMMIT;
import static java.sql.Connection.TRANSACTION_READ_COMMITTED;
import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

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
    return context.executeBlocking((SQLBlockingCodeHandler<String>) prom -> {
      int isolation = conn.getTransactionIsolation();
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
      conn.setAutoCommit(false);
      prom.complete(isolationLevel);
    }, false).compose((SQLFutureMapper<String, Boolean>) isolationLevel -> {
      Flow.Publisher<Boolean> publisher = conn
        .prepareStatement("SET TRANSACTION ISOLATION LEVEL " + isolationLevel)
        .unwrap(OraclePreparedStatement.class)
        .executeAsyncOracle();
      return Helper.first(publisher, context);
    }).mapEmpty();
  }

  private Future<Void> commit(OracleConnection conn, ContextInternal context) {
    return context.executeBlocking((SQLBlockingCodeHandler<Boolean>) prom -> {
      prom.complete(conn.getAutoCommit());
    }, false).compose((SQLFutureMapper<Boolean, Void>) autoCommit -> {
      return autoCommit ? Future.succeededFuture() : Helper.first(conn.commitAsyncOracle(), context);
    }).eventually(v -> context.executeBlocking((SQLBlockingCodeHandler<Boolean>) prom -> {
      conn.setAutoCommit(true);
      prom.complete();
    }, false));
  }

  private Future<Void> rollback(OracleConnection conn, ContextInternal context) {
    return context.executeBlocking((SQLBlockingCodeHandler<Boolean>) prom -> {
      prom.complete(conn.getAutoCommit());
    }, false).compose((SQLFutureMapper<Boolean, Void>) autoCommit -> {
      return autoCommit ? Future.succeededFuture() : Helper.first(conn.rollbackAsyncOracle(), context);
    }).eventually(v -> context.executeBlocking((SQLBlockingCodeHandler<Boolean>) prom -> {
      conn.setAutoCommit(true);
      prom.complete();
    }, false));
  }
}

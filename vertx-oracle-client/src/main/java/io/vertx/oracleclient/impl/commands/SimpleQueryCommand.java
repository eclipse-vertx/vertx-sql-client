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

import io.vertx.core.Future;
import io.vertx.core.impl.ContextInternal;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.impl.Helper;
import io.vertx.sqlclient.Row;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;

import java.sql.SQLException;
import java.util.stream.Collector;

public class SimpleQueryCommand<C, R> extends QueryCommand<C, R> {

  private final String sql;

  public SimpleQueryCommand(OracleConnectOptions options, String sql,
    Collector<Row, C, R> collector) {
    super(options, collector);
    this.sql = sql;
  }

  private Future<Boolean> execute(OraclePreparedStatement sqlStatement, ContextInternal context) {
    try {
      return Helper.first(sqlStatement.executeAsyncOracle(), context);
    } catch (SQLException throwables) {
      return context.failedFuture(throwables);
    }
  }

  private void closeQuietly(OraclePreparedStatement c) {
    if (c == null) {
      return;
    }
    try {
      c.close();
    } catch (Exception e) {
      // Ignore it.
    }
  }

  @Override
  public Future<OracleResponse<R>> execute(OracleConnection conn, ContextInternal context) {
    OraclePreparedStatement ps = null;
    try {
      ps = (OraclePreparedStatement) conn.prepareStatement(sql);
      applyStatementOptions(ps);
      final OraclePreparedStatement ref = ps;
      return execute(ps, context)
        .compose(mayBeResult -> {
          try {
            return context.succeededFuture(decode(ref, mayBeResult, false));
          } catch (SQLException throwables) {
            return context.failedFuture(throwables);
          } finally {
            closeQuietly(ref);
          }
        });
    } catch (SQLException e) {
      closeQuietly(ps);
      return context.failedFuture(e);
    }
  }
}

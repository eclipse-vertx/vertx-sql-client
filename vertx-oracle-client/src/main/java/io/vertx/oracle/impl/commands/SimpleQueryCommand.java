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
package io.vertx.oracle.impl.commands;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.oracle.OracleConnectOptions;
import io.vertx.oracle.impl.Helper;
import io.vertx.sqlclient.Row;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;

import java.sql.SQLException;
import java.util.Collections;
import java.util.stream.Collector;

public class SimpleQueryCommand<C, R> extends QueryCommand<C, R> {

  private final String sql;

  public SimpleQueryCommand(OracleConnectOptions options, String sql,
    Collector<Row, C, R> collector) {
    super(options, collector);
    this.sql = sql;
  }

  private Future<Boolean> execute(OraclePreparedStatement sqlStatement, Context context) {
    try {
      return Helper.first(sqlStatement.executeAsyncOracle(), context);
    } catch (SQLException throwables) {
      return Future.failedFuture(throwables);
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
  public Future<OracleResponse<R>> execute(OracleConnection conn, Context context) {
    OraclePreparedStatement ps = null;
    try {
      ps = (OraclePreparedStatement) conn.prepareStatement(sql);
      applyStatementOptions(ps);
      final OraclePreparedStatement ref = ps;
      return execute(ps, context)
        .compose(mayBeResult -> {
          try {
            return Future.succeededFuture(decode(ref, mayBeResult, false, Collections.emptyList()));
          } catch (SQLException throwables) {
            return Future.failedFuture(throwables);
          } finally {
            closeQuietly(ref);
          }
        });
    } catch (SQLException e) {
      closeQuietly(ps);
      return Future.failedFuture(e);
    }
  }
}

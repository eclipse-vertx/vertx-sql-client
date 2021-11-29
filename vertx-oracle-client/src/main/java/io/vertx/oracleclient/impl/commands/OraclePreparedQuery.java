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

import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.OraclePrepareOptions;
import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collector;

public class OraclePreparedQuery<C, R> extends QueryCommand<C, R> {

  private final ExtendedQueryCommand<R> query;
  private final Tuple params;

  public OraclePreparedQuery(OracleConnectOptions options, ExtendedQueryCommand<R> query, Collector<Row, C, R> collector, Tuple params) {
    super(new ResultDecoder<>(collector));
    this.query = query;
    this.params = params;
  }

  @Override
  protected OraclePrepareOptions prepareOptions() {
    PrepareOptions prepareOptions = query.options();
    return prepareOptions instanceof OraclePrepareOptions ? (OraclePrepareOptions) prepareOptions : null;
  }

  @Override
  protected String query() {
    return query.sql();
  }

  @Override
  protected void applyStatementOptions(Statement statement) throws SQLException {
    int fetch = query.fetch();
    if (fetch > 0) {
      statement.setFetchSize(fetch);
    }
  }

  @Override
  protected void fillStatement(PreparedStatement ps, Connection conn) throws SQLException {
    for (int i = 0; i < params.size(); i++) {
      // we must convert types (to comply to JDBC)
      Object value = adaptType(conn, params.getValue(i));
      ps.setObject(i + 1, value);
    }
  }
}

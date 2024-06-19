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
import io.vertx.oracleclient.OraclePrepareOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collector;

public class OracleSimpleQueryCommand<C, R> extends OracleQueryCommand<C, R> {

  private final String sql;
  private final QueryResultHandler<R> resultHandler;

  private OracleSimpleQueryCommand(OracleConnection oracleConnection, ContextInternal connectionContext, SimpleQueryCommand<R> cmd, Collector<Row, C, R> collector) {
    super(oracleConnection, connectionContext, collector);
    sql = cmd.sql();
    resultHandler = cmd.resultHandler();
  }

  public static <U> OracleSimpleQueryCommand<?, U> create(OracleConnection oracleConnection, ContextInternal connectionContext, SimpleQueryCommand<U> cmd) {
    return new OracleSimpleQueryCommand<>(oracleConnection, connectionContext, cmd, cmd.collector());
  }

  @Override
  protected OraclePrepareOptions prepareOptions() {
    return null;
  }

  @Override
  protected boolean returnAutoGeneratedKeys(Connection conn, OraclePrepareOptions options) {
    return false;
  }

  @Override
  protected String query() {
    return sql;
  }

  @Override
  protected void fillStatement(PreparedStatement ps, Connection conn) throws SQLException {
  }

  @Override
  protected Future<Boolean> doExecute(OraclePreparedStatement ps, boolean returnAutoGeneratedKeys) {
    return executeBlocking(ps::executeAsyncOracle)
      .compose(pub -> first(pub))
      .compose(returnedResultSet -> executeBlocking(() -> decode(ps, returnedResultSet, returnAutoGeneratedKeys)))
      .map(oracleResponse -> {
        oracleResponse.handle(resultHandler);
        return false;
      });
  }
}

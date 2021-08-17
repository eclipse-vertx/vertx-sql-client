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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.impl.Helper;
import io.vertx.oracleclient.impl.OracleColumnDesc;
import io.vertx.oracleclient.impl.OracleRow;
import io.vertx.oracleclient.impl.RowReader;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

import static io.vertx.oracleclient.impl.Helper.unwrapOraclePreparedStatement;

public class OracleCursorQueryCommand<C, R> extends QueryCommand<C, R> {
  private final ExtendedQueryCommand<R> command;
  private final Tuple params;

  public OracleCursorQueryCommand(OracleConnectOptions options, ExtendedQueryCommand<R> command, Tuple params) {
    super(options, null);
    this.command = command;
    this.params = params;
  }

  @Override
  public Future<OracleResponse<R>> execute(OracleConnection conn, ContextInternal context) {
    Future<PreparedStatement> future = prepare(command, conn, false, context); // TODO returnAutoGenerateKeys
    return future
      .flatMap(ps -> {
        try {
          fillStatement(ps, conn);
        } catch (SQLException throwables) {
          Helper.closeQuietly(ps);
          return context.failedFuture(throwables);
        }

        return createRowReader(ps, context)
          .compose(rr -> rr.read(command.fetch()))
          .map(x -> (OracleResponse<R>) null)
          .onComplete(ar ->
            Helper.closeQuietly(ps)
          );
      });

  }

  public Future<RowReader> createRowReader(PreparedStatement sqlStatement, ContextInternal context) {
    OraclePreparedStatement oraclePreparedStatement =
      unwrapOraclePreparedStatement(sqlStatement);
    try {
      Flow.Publisher<OracleResultSet> publisher = oraclePreparedStatement.executeQueryAsyncOracle();
      return Helper.first(publisher, context)
        .compose(ors -> {
          try {
            RowDesc description = OracleColumnDesc.rowDesc(ors.getMetaData());
            List<String> types = new ArrayList<>();
            for (int i = 1; i <= ors.getMetaData().getColumnCount(); i++) {
              types.add(ors.getMetaData().getColumnClassName(i));
            }
            return RowReader.create(ors.publisherOracle(
              or -> Helper.getOrHandleSQLException(() -> transform(types, description, or))),
              context,
              (QueryResultHandler<RowSet<Row>>) command.resultHandler(), description);
          } catch (SQLException e) {
            return context.failedFuture(e);
          }
        });
    } catch (SQLException throwables) {
      return context.failedFuture(throwables);
    }
  }

  private static Row transform(List<String> ors, RowDesc desc, oracle.jdbc.OracleRow or) throws SQLException {
    Row row = new OracleRow(desc);
    for (int i = 1; i <= desc.columnNames().size(); i++) {
      Object res = QueryCommand.convertSqlValue(or.getObject(i, getType(ors.get(i - 1))));
      row.addValue(res);
    }
    return row;
  }

  private static Class<?> getType(String cn) {
    try {
      return OraclePreparedQuery.class.getClassLoader().loadClass(cn);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private void fillStatement(PreparedStatement ps, Connection conn) throws SQLException {

    for (int i = 0; i < params.size(); i++) {
      // we must convert types (to comply to JDBC)
      Object value = adaptType(conn, params.getValue(i));
      ps.setObject(i + 1, value);
    }
  }

  private Object adaptType(Connection conn, Object value) throws SQLException {
    // we must convert types (to comply to JDBC)

    if (value instanceof LocalTime) {
      // -> java.sql.Time
      LocalTime time = (LocalTime) value;
      return Time.valueOf(time);
    } else if (value instanceof LocalDate) {
      // -> java.sql.Date
      LocalDate date = (LocalDate) value;
      return Date.valueOf(date);
    } else if (value instanceof Instant) {
      // -> java.sql.Timestamp
      Instant timestamp = (Instant) value;
      return Timestamp.from(timestamp);
    } else if (value instanceof Buffer) {
      // -> java.sql.Blob
      Buffer buffer = (Buffer) value;
      Blob blob = conn.createBlob();
      blob.setBytes(1, buffer.getBytes());
      return blob;
    }

    return value;
  }
}

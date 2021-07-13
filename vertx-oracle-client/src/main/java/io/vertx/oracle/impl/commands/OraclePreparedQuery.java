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
import io.vertx.core.buffer.Buffer;
import io.vertx.oracle.OracleConnectOptions;
import io.vertx.oracle.SqlOutParam;
import io.vertx.oracle.impl.Helper;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

import static io.vertx.oracle.impl.Helper.completeOrFail;
import static io.vertx.oracle.impl.Helper.unwrapOraclePreparedStatement;

public class OraclePreparedQuery<C, R> extends QueryCommand<C, R> {

  private final ExtendedQueryCommand<R> query;
  private final Tuple params;
  private final List<Integer> outParams;

  private static List<Integer> countOut(Tuple tuple) {
    List<Integer> total = new ArrayList<>();
    if (tuple != null) {
      for (int i = 0; i < tuple.size(); i++) {
        if (tuple.getValue(i) instanceof SqlOutParam) {
          total.add(i + 1);
        }
      }
    }

    return total;
  }

  public OraclePreparedQuery(OracleConnectOptions options, ExtendedQueryCommand<R> query,
    Collector<Row, C, R> collector, Tuple params) {
    super(options, collector);
    this.query = query;
    this.params = params;
    this.outParams = countOut(params);
  }

  @Override
  public Future<OracleResponse<R>> execute(OracleConnection conn, Context context) {
    boolean returnAutoGeneratedKeys = returnAutoGeneratedKeys(conn);

    Future<PreparedStatement> future = prepare(context, conn, returnAutoGeneratedKeys);
    return future
      .flatMap(ps -> {
        try {
          fillStatement(ps, conn);
        } catch (SQLException throwables) {
          Helper.closeQuietly(ps);
          return Future.failedFuture(throwables);
        }

        return execute(ps, context)
          .map(res -> {
            return Helper.getOrHandleSQLException(
              () -> decode(ps, res, returnAutoGeneratedKeys, outParams));
          })
          .onComplete(ar ->
            Helper.closeQuietly(ps)
          );

      });

  }

  public Future<Boolean> execute(PreparedStatement sqlStatement, Context context) {
    OraclePreparedStatement oraclePreparedStatement =
      unwrapOraclePreparedStatement(sqlStatement);
    try {
      return Helper.first(oraclePreparedStatement.executeAsyncOracle(), context);
    } catch (SQLException throwables) {
      return Future.failedFuture(throwables);
    }
  }

  private Future<PreparedStatement> prepare(Context context, Connection conn, boolean returnAutoGeneratedKeys) {
    final String sql = query.sql();
    if (outParams.size() > 0) {
      // TODO Is this blocking?
      return completeOrFail(() -> conn.prepareCall(sql));
    } else {
      return prepare(query, conn, returnAutoGeneratedKeys, context);
    }
  }

  private void fillStatement(PreparedStatement ps, Connection conn) throws SQLException {

    for (int i = 0; i < params.size(); i++) {
      // we must convert types (to comply to JDBC)
      Object value = adaptType(conn, params.getValue(i));

      if (value instanceof SqlOutParam) {
        SqlOutParam outValue = (SqlOutParam) value;

        if (outValue.in()) {
          ps.setObject(i + 1, adaptType(conn, outValue.value()));
        }

        ((CallableStatement) ps)
          .registerOutParameter(i + 1, outValue.type());
      } else {
        ps.setObject(i + 1, value);
      }
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
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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.oracleclient.OraclePrepareOptions;
import io.vertx.oracleclient.data.Blob;
import io.vertx.oracleclient.impl.Helper;
import io.vertx.oracleclient.impl.OracleRow;
import io.vertx.oracleclient.impl.OracleRowDesc;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDesc;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleTypes;
import oracle.sql.TIMESTAMPTZ;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.BiConsumer;
import java.util.stream.Collector;

import static io.vertx.oracleclient.impl.Helper.closeQuietly;

public abstract class QueryCommand<C, R> extends AbstractCommand<OracleResponse<R>> {

  private final Collector<Row, C, R> collector;

  protected QueryCommand(Collector<Row, C, R> collector) {
    this.collector = collector;
  }

  @Override
  public Future<OracleResponse<R>> execute(OracleConnection conn, ContextInternal context) {
    OraclePrepareOptions options = prepareOptions();
    boolean returnAutoGeneratedKeys = returnAutoGeneratedKeys(conn, options);
    Future<OraclePreparedStatement> psFuture = prepare(conn, options, returnAutoGeneratedKeys, context);
    return psFuture.compose(ps -> {
      return doExecute(ps, context, returnAutoGeneratedKeys).onComplete(ar -> {
        closeQuietly(ps);
      });
    });
  }

  protected abstract OraclePrepareOptions prepareOptions();

  protected boolean returnAutoGeneratedKeys(Connection conn, OraclePrepareOptions options) {
    boolean autoGeneratedKeys = options != null && options.isAutoGeneratedKeys();
    boolean autoGeneratedIndexes = options != null
      && options.getAutoGeneratedKeysIndexes() != null
      && options.getAutoGeneratedKeysIndexes().size() > 0;
    // even though the user wants it, the DBMS may not support it
    if (autoGeneratedKeys || autoGeneratedIndexes) {
      try {
        DatabaseMetaData dbmd = conn.getMetaData();
        if (dbmd != null) {
          return dbmd.supportsGetGeneratedKeys();
        }
      } catch (SQLException ignore) {
      }
    }
    return false;
  }

  protected abstract String query();

  private Future<OraclePreparedStatement> prepare(Connection conn, OraclePrepareOptions options, boolean returnAutoGeneratedKeys, Context context) {
    return context.executeBlocking(prom -> {
      String query = query();
      PreparedStatement ps = null;
      try {
        boolean autoGeneratedIndexes = isAutoGeneratedIndexes(options);
        if (returnAutoGeneratedKeys && !autoGeneratedIndexes) {
          ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        } else if (autoGeneratedIndexes) {
          // convert json array to int or string array
          JsonArray indexes = options.getAutoGeneratedKeysIndexes();
          if (indexes.getValue(0) instanceof Number) {
            int[] keys = new int[indexes.size()];
            for (int i = 0; i < keys.length; i++) {
              keys[i] = indexes.getInteger(i);
            }
            ps = conn.prepareStatement(query, keys);
          } else if (indexes.getValue(0) instanceof String) {
            String[] keys = new String[indexes.size()];
            for (int i = 0; i < keys.length; i++) {
              keys[i] = indexes.getString(i);
            }
            ps = conn.prepareStatement(query, keys);
          } else {
            prom.fail("Invalid type of index, only [int, String] allowed");
            return;
          }
        } else {
          ps = conn.prepareStatement(query());
        }

        applyStatementOptions(ps);

        fillStatement(ps, conn);

        prom.complete(ps.unwrap(OraclePreparedStatement.class));

      } catch (SQLException e) {
        closeQuietly(ps);
        prom.fail(e);
      }
    }, false);
  }

  private boolean isAutoGeneratedIndexes(OraclePrepareOptions options) {
    return options != null
      && options.getAutoGeneratedKeysIndexes() != null
      && options.getAutoGeneratedKeysIndexes().size() > 0;
  }

  protected abstract void fillStatement(PreparedStatement ps, Connection conn) throws SQLException;

  protected Object adaptType(Connection conn, Object value) throws SQLException {
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
    } else if (value instanceof Blob) {
      // -> java.sql.Blob
      Blob blob = (Blob) value;
      java.sql.Blob javaBlob = conn.createBlob();
      javaBlob.setBytes(1, blob.bytes);
      return javaBlob;
    } else if (value instanceof Buffer) {
      // -> RAW
      Buffer buffer = (Buffer) value;
      return buffer.getBytes();
    }

    return value;
  }

  protected abstract Future<OracleResponse<R>> doExecute(OraclePreparedStatement ps, ContextInternal context, boolean returnAutoGeneratedKeys);

  protected OracleResponse<R> decode(Statement statement, boolean returnedResultSet, boolean returnedKeys) throws SQLException {

    OracleResponse<R> response = new OracleResponse<>(statement.getUpdateCount());
    if (returnedResultSet) {
      // normal return only
      while (returnedResultSet) {
        try (ResultSet rs = statement.getResultSet()) {
          decodeResultSet(rs, response);
        }
        if (returnedKeys) {
          decodeReturnedKeys(statement, response);
        }
        returnedResultSet = statement.getMoreResults();
      }
    } else {
      collector.accumulator();
      // first rowset includes the output results
      C container = collector.supplier().get();

      response.empty(collector.finisher().apply(container));
      if (returnedKeys) {
        decodeReturnedKeys(statement, response);
      }
    }

    return response;
  }

  protected OracleResponse<R> decode(Statement statement, int[] returnedBatchResult, boolean returnedKeys) throws SQLException {
    OracleResponse<R> response = new OracleResponse<>(returnedBatchResult.length);

    BiConsumer<C, Row> accumulator = collector.accumulator();

    RowDesc desc = OracleRowDesc.EMPTY;
    C container = collector.supplier().get();
    for (int result : returnedBatchResult) {
      Row row = new OracleRow(desc);
      row.addValue(result);
      accumulator.accept(container, row);
    }

    response
      .push(collector.finisher().apply(container), desc, returnedBatchResult.length);

    if (returnedKeys) {
      decodeReturnedKeys(statement, response);
    }

    return response;
  }

  private void decodeResultSet(ResultSet rs, OracleResponse<R> response) throws SQLException {
    BiConsumer<C, Row> accumulator = collector.accumulator();

    C container = collector.supplier().get();
    int size = 0;
    ResultSetMetaData metaData = rs.getMetaData();
    RowDesc desc = OracleRowDesc.create(metaData);
    while (rs.next()) {
      size++;
      Row row = new OracleRow(desc);
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        Object res = Helper.convertSqlValue(rs.getObject(i));
        row.addValue(res);
      }
      accumulator.accept(container, row);
    }

    response.push(collector.finisher().apply(container), desc, size);
  }

  private void decodeReturnedKeys(Statement statement, OracleResponse<R> response) throws SQLException {
    ResultSet keysRS = statement.getGeneratedKeys();
    if (keysRS != null) {
      if (keysRS.next()) {
        ResultSetMetaData metaData = keysRS.getMetaData();
        if (metaData != null) {
          int cols = metaData.getColumnCount();
          if (cols > 0) {
            RowDesc keysDesc = OracleRowDesc.create(metaData);

            OracleRow keys = new OracleRow(keysDesc);
            for (int i = 1; i <= cols; i++) {
              Class<?> colJdbcClass = getJdbcClass(i, metaData);
              final Object value;
              if (colJdbcClass != null) {
                value = keysRS.getObject(i, colJdbcClass);
              } else {
                value = keysRS.getObject(i);
              }
              Object res = Helper.convertSqlValue(value);
              keys.addValue(res);
            }

            response.returnedKeys(keys);
          }
        }
      }
    }
  }

  private static Class<?> getJdbcClass(
          int index, ResultSetMetaData resultSetMetaData) throws SQLException {
    int jdbcType = resultSetMetaData.getColumnType(index);
    switch (jdbcType) {
      case Types.DATE:
      case Types.TIMESTAMP:
        return Timestamp.class;
      case OracleTypes.TIMESTAMPTZ:
      case Types.TIMESTAMP_WITH_TIMEZONE:
        return TIMESTAMPTZ.class;
      default:
        return null;
    }
  }
}

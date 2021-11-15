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
import io.vertx.core.json.JsonArray;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.OraclePrepareOptions;
import io.vertx.oracleclient.impl.OracleColumnDesc;
import io.vertx.oracleclient.impl.OracleRow;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneOffset;
import java.util.function.BiConsumer;
import java.util.stream.Collector;

import static io.vertx.oracleclient.impl.Helper.completeOrFail;

public abstract class QueryCommand<C, R> extends AbstractCommand<OracleResponse<R>> {

  private final Collector<Row, C, R> collector;
  private final OracleConnectOptions options;

  public QueryCommand(OracleConnectOptions options, Collector<Row, C, R> collector) {
    this.options = options;
    this.collector = collector;
  }

  protected OracleResponse<R> decode(Statement statement, boolean returnedResultSet, boolean returnedKeys) throws SQLException {

    final OracleResponse<R> response = new OracleResponse<>(statement.getUpdateCount());
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

  //    protected OracleResponse<R> decode(Statement statement, RowReader.ReadRows rr, boolean returnedKeys,
  //            List<Integer> out) throws SQLException {
  //
  //        final OracleResponse<R> response = new OracleResponse<>(statement.getUpdateCount());
  //        BiConsumer<C, Row> accumulator = collector.accumulator();
  //        C container = collector.supplier().get();
  //
  //        for (Row row : rr.getRows()) {
  //            accumulator.accept(container, row);
  //        }
  //
  //        response
  //                .push(collector.finisher().apply(container), rr.getRowDescription(), rr.getRows().size());
  //
  //        if (returnedKeys) {
  //            decodeReturnedKeys(statement, response);
  //        }
  //
  //        if (out.size() > 0) {
  //            decodeOutput((CallableStatement) statement, out, response);
  //        }
  //
  //        return response;
  //    }

  protected OracleResponse<R> decode(Statement statement, int[] returnedBatchResult, boolean returnedKeys)
    throws SQLException {
    final OracleResponse<R> response = new OracleResponse<>(returnedBatchResult.length);

    BiConsumer<C, Row> accumulator = collector.accumulator();

    RowDesc desc = RowDesc.EMPTY;
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
    RowDesc desc = OracleColumnDesc.rowDesc(metaData);
    while (rs.next()) {
      size++;
      Row row = new OracleRow(desc);
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        Object res = convertSqlValue(rs.getObject(i));
        row.addValue(res);
      }
      accumulator.accept(container, row);
    }

    response
      .push(collector.finisher().apply(container), desc, size);
  }

  private void decodeReturnedKeys(Statement statement, OracleResponse<R> response) throws SQLException {
    Row keys = null;

    ResultSet keysRS = statement.getGeneratedKeys();

    if (keysRS != null) {
      ResultSetMetaData metaData = keysRS.getMetaData();
      if (metaData != null) {
        int cols = metaData.getColumnCount();
        if (cols > 0) {
          RowDesc keysDesc = OracleColumnDesc.rowDesc(metaData);

          if (keysRS.next()) {
            keys = new OracleRow(keysDesc);
            for (int i = 1; i <= cols; i++) {
              Object res = convertSqlValue(keysRS.getObject(i));
              keys.addValue(res);
            }
          }
          response.returnedKeys(keys);
        }
      }
    }
  }

  public static Object convertSqlValue(Object value) throws SQLException {
    if (value == null) {
      return null;
    }

    // valid json types are just returned as is
    if (value instanceof Boolean || value instanceof String || value instanceof byte[]) {
      return value;
    }

    // numeric values
    if (value instanceof Number) {
      if (value instanceof BigDecimal) {
        BigDecimal d = (BigDecimal) value;
        if (d.scale() == 0) {
          return ((BigDecimal) value).toBigInteger();
        } else {
          // we might loose precision here
          return ((BigDecimal) value).doubleValue();
        }
      }

      return value;
    }

    // JDBC temporal values

    if (value instanceof Time) {
      return ((Time) value).toLocalTime();
    }

    if (value instanceof Date) {
      return ((Date) value).toLocalDate();
    }

    if (value instanceof Timestamp) {
      return ((Timestamp) value).toInstant().atOffset(ZoneOffset.UTC);
    }

    // large objects
    if (value instanceof Clob) {
      Clob c = (Clob) value;
      try {
        // result might be truncated due to downcasting to int
        return c.getSubString(1, (int) c.length());
      } finally {
        try {
          c.free();
        } catch (AbstractMethodError | SQLFeatureNotSupportedException e) {
          // ignore since it is an optional feature since 1.6 and non existing before 1.6
        }
      }
    }

    if (value instanceof Blob) {
      Blob b = (Blob) value;
      try {
        // result might be truncated due to downcasting to int
        return b.getBytes(1, (int) b.length());
      } finally {
        try {
          b.free();
        } catch (AbstractMethodError | SQLFeatureNotSupportedException e) {
          // ignore since it is an optional feature since 1.6 and non existing before 1.6
        }
      }
    }

    // arrays
    if (value instanceof Array) {
      Array a = (Array) value;
      try {
        Object arr = a.getArray();
        if (arr != null) {
          int len = java.lang.reflect.Array.getLength(arr);
          Object[] castedArray = new Object[len];
          for (int i = 0; i < len; i++) {
            castedArray[i] = convertSqlValue(java.lang.reflect.Array.get(arr, i));
          }
          return castedArray;
        }
      } finally {
        a.free();
      }
    }

    // RowId
    if (value instanceof RowId) {
      return ((RowId) value).getBytes();
    }

    // Struct
    if (value instanceof Struct) {
      return Tuple.of(((Struct) value).getAttributes());
    }

    // fallback to String
    return value.toString();
  }

  boolean returnAutoGeneratedKeys(Connection conn, OraclePrepareOptions options) {
    boolean autoGeneratedKeys = options == null || options.isAutoGeneratedKeys();
    boolean autoGeneratedIndexes = options != null && options.getAutoGeneratedKeysIndexes() != null
      && options.getAutoGeneratedKeysIndexes().size() > 0;
    //        // even though the user wants it, the DBMS may not support it
    //        if (autoGeneratedKeys || autoGeneratedIndexes) {
    //            try {
    //                DatabaseMetaData dbmd = conn.getMetaData();
    //                if (dbmd != null) {
    //                    return dbmd.supportsGetGeneratedKeys();
    //                }
    //            } catch (SQLException e) {
    //                // ignore...
    //            }
    //        }
    // TODO Oracle does not support this in batch???
    return false;
  }

  protected Future<PreparedStatement> prepare(ExtendedQueryCommand<R> query, Connection conn,
    boolean returnAutoGeneratedKeys, Context context) {
    OraclePrepareOptions options = query.options() instanceof OraclePrepareOptions ? (OraclePrepareOptions) query.options() : null;
    boolean autoGeneratedIndexes = options != null && options.getAutoGeneratedKeysIndexes() != null
      && options.getAutoGeneratedKeysIndexes().size() > 0;

    String sql = query.sql();
    int fetch = query.fetch();
    if (returnAutoGeneratedKeys && !autoGeneratedIndexes) {
      return completeOrFail(() -> {
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        configureFetch(fetch, statement);
        if (query.cursorId() != null) {
          statement.setCursorName(query.cursorId());
        }
        return statement;
      });
    } else if (autoGeneratedIndexes) {
      return context.executeBlocking(promise -> createPreparedStatement(conn, sql, options, fetch, promise));
    } else {
      return completeOrFail(() -> {
        PreparedStatement statement = conn.prepareStatement(sql);
        configureFetch(fetch, statement);
        return statement;
      });
    }
  }

  private void configureFetch(int fetch, PreparedStatement statement) throws SQLException {
    if (fetch > 0) {
      statement.setFetchSize(fetch);
    }
  }

  protected void createPreparedStatement(Connection conn, String sql, OraclePrepareOptions options, int fetch,
    io.vertx.core.Promise<PreparedStatement> promise) {
    // convert json array to int or string array
    JsonArray indexes = options.getAutoGeneratedKeysIndexes();
    try {
      if (indexes.getValue(0) instanceof Number) {
        int[] keys = new int[indexes.size()];
        for (int i = 0; i < keys.length; i++) {
          keys[i] = indexes.getInteger(i);
        }
        promise.complete(conn.prepareStatement(sql, keys));
      } else if (indexes.getValue(0) instanceof String) {
        String[] keys = new String[indexes.size()];
        for (int i = 0; i < keys.length; i++) {
          keys[i] = indexes.getString(i);
        }
        PreparedStatement statement = conn.prepareStatement(sql, keys);
        configureFetch(fetch, statement);
        promise.complete(statement);
      } else {
        promise.fail(new SQLException("Invalid type of index, only [int, String] allowed"));
      }
    } catch (SQLException e) {
      promise.fail(e);
    } catch (RuntimeException e) {
      // any exception due to type conversion
      promise.fail(new SQLException(e));
    }
  }
}

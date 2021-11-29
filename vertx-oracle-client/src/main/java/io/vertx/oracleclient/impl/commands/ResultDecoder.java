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

import io.vertx.oracleclient.impl.Helper;
import io.vertx.oracleclient.impl.OracleColumnDesc;
import io.vertx.oracleclient.impl.OracleRow;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDesc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.BiConsumer;
import java.util.stream.Collector;

class ResultDecoder<C, R> {

  private final Collector<Row, C, R> collector;

  ResultDecoder(Collector<Row, C, R> collector) {
    this.collector = collector;
  }

  OracleResponse<R> decode(Statement statement, boolean returnedResultSet, boolean returnedKeys) throws SQLException {

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

  OracleResponse<R> decode(Statement statement, int[] returnedBatchResult, boolean returnedKeys) throws SQLException {
    OracleResponse<R> response = new OracleResponse<>(returnedBatchResult.length);

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
        Object res = Helper.convertSqlValue(rs.getObject(i));
        row.addValue(res);
      }
      accumulator.accept(container, row);
    }

    response.push(collector.finisher().apply(container), desc, size);
  }

  private void decodeReturnedKeys(Statement statement, OracleResponse<R> response) throws SQLException {
    Row keys = null;

    ResultSet keysRS = statement.getGeneratedKeys();

    if (keysRS != null) {
      if (keysRS.next()) {
        ResultSetMetaData metaData = keysRS.getMetaData();
        if (metaData != null) {
          int cols = metaData.getColumnCount();
          if (cols > 0) {
            RowDesc keysDesc = OracleColumnDesc.rowDesc(metaData);

            keys = new OracleRow(keysDesc);
            for (int i = 1; i <= cols; i++) {
              Object res = Helper.convertSqlValue(keysRS.getObject(i));
              keys.addValue(res);
            }

            response.returnedKeys(keys);
          }
        }
      }
    }
  }
}

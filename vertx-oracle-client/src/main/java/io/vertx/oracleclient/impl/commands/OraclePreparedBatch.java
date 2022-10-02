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
import io.vertx.oracleclient.OraclePrepareOptions;
import io.vertx.oracleclient.impl.Helper;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import oracle.jdbc.OraclePreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Collector;

public class OraclePreparedBatch<C, R> extends QueryCommand<C, R> {

  private final ExtendedQueryCommand<R> query;
  private final List<Tuple> listParams;

  public OraclePreparedBatch(ExtendedQueryCommand<R> query, Collector<Row, C, R> collector, List<Tuple> listParams) {
    super(collector);
    this.query = query;
    this.listParams = listParams;
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
    for (Tuple params : listParams) {
      for (int i = 0; i < params.size(); i++) {
        // we must convert types (to comply to JDBC)
        Object value = adaptType(conn, params.getValue(i));
        ps.setObject(i + 1, value);
      }
      ps.addBatch();
    }
  }

  @Override
  protected Future<OracleResponse<R>> doExecute(OraclePreparedStatement ps, ContextInternal context, boolean returnAutoGeneratedKeys) {
    Flow.Publisher<Long> publisher;
    try {
      publisher = ps.executeBatchAsyncOracle();
    } catch (SQLException e) {
      return context.failedFuture(e);
    }
    return Helper.collect(publisher, context).map(list -> {
      int[] res = new int[list.size()];
      for (int i = 0; i < list.size(); i++) {
        res[i] = list.get(i).intValue();
      }
      return res;
    }).compose(returnedBatchResult -> {
      return Helper.executeBlocking(context, () -> decode(ps, returnedBatchResult, returnAutoGeneratedKeys));
    });
  }
}

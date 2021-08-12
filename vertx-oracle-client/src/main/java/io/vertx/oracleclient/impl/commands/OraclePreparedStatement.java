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

import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.TupleInternal;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OraclePreparedStatement implements io.vertx.sqlclient.impl.PreparedStatement {

  private final String sql;
  private final RowDesc rowDesc;
  private final ParamDesc paramDesc;

  public OraclePreparedStatement(String sql, java.sql.PreparedStatement preparedStatement) throws SQLException {

    List<String> columnNames = new ArrayList<>();
    ResultSetMetaData metaData = preparedStatement.getMetaData();
    if (metaData != null) {
      // Not a SELECT
      int cols = metaData.getColumnCount();
      for (int i = 1; i <= cols; i++) {
        columnNames.add(metaData.getColumnLabel(i));
      }
    }

    this.sql = sql;
    this.rowDesc = new RowDesc(columnNames);
    this.paramDesc = new ParamDesc();
  }

  @Override
  public ParamDesc paramDesc() {
    return paramDesc;
  }

  @Override
  public RowDesc rowDesc() {
    return rowDesc;
  }

  @Override
  public String sql() {
    return sql;
  }

  @Override
  public String prepare(TupleInternal values) {
    return null;
  }

}

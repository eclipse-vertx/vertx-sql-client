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

import io.vertx.oracleclient.impl.OracleRowDesc;
import io.vertx.sqlclient.internal.RowDesc;
import io.vertx.sqlclient.internal.PreparedStatement;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class OraclePreparedStatement implements PreparedStatement {

  private final String sql;
  private final RowDesc rowDesc;

  public OraclePreparedStatement(String sql, java.sql.PreparedStatement preparedStatement) throws SQLException {
    ResultSetMetaData metaData = preparedStatement.getMetaData();
    RowDesc rowDesc;
    if (metaData != null) {
      rowDesc = OracleRowDesc.create(metaData);
    } else {
      rowDesc = OracleRowDesc.EMPTY;
    }
    this.sql = sql;
    this.rowDesc = rowDesc;
  }

  @Override
  public RowDesc rowDesc() {
    return rowDesc;
  }

  @Override
  public String sql() {
    return sql;
  }
}

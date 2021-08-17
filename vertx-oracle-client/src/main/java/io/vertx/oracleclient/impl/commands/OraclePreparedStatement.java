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

import io.vertx.oracleclient.impl.OracleColumnDesc;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.TupleInternal;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class OraclePreparedStatement implements io.vertx.sqlclient.impl.PreparedStatement {

  private final String sql;
  private final RowDesc rowDesc;
  private final ParamDesc paramDesc;

  public OraclePreparedStatement(String sql, java.sql.PreparedStatement preparedStatement) throws SQLException {
    ResultSetMetaData metaData = preparedStatement.getMetaData();
    RowDesc rowDesc;
    if (metaData != null) {
      rowDesc = OracleColumnDesc.rowDesc(metaData);
    } else {
      rowDesc = RowDesc.EMPTY;
    }
    this.sql = sql;
    this.rowDesc = rowDesc;
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

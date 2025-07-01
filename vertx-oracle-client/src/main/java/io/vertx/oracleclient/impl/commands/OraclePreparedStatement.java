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

import io.vertx.oracleclient.impl.OracleRowDescriptor;
import io.vertx.sqlclient.internal.RowDescriptorBase;
import io.vertx.sqlclient.internal.PreparedStatement;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class OraclePreparedStatement implements PreparedStatement {

  private final String sql;
  private final RowDescriptorBase rowDescriptor;

  public OraclePreparedStatement(String sql, java.sql.PreparedStatement preparedStatement) throws SQLException {
    ResultSetMetaData metaData = preparedStatement.getMetaData();
    RowDescriptorBase rowDescriptor;
    if (metaData != null) {
      rowDescriptor = OracleRowDescriptor.create(metaData);
    } else {
      rowDescriptor = OracleRowDescriptor.EMPTY;
    }
    this.sql = sql;
    this.rowDescriptor = rowDescriptor;
  }

  @Override
  public RowDescriptorBase rowDesc() {
    return rowDescriptor;
  }

  @Override
  public String sql() {
    return sql;
  }
}

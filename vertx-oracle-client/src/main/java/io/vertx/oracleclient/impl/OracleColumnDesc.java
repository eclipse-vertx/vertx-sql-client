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
package io.vertx.oracleclient.impl;

import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.RowDesc;

import java.sql.JDBCType;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OracleColumnDesc implements ColumnDescriptor {

  public static RowDesc rowDesc(ResultSetMetaData metaData) throws SQLException {
    int cols = metaData.getColumnCount();
    List<String> columnNames = new ArrayList<>(cols);
    List<ColumnDescriptor> columnDescriptors = new ArrayList<>(cols);
    for (int i = 1; i <= cols; i++) {
      columnNames.add(metaData.getColumnLabel(i));
      columnDescriptors.add(new OracleColumnDesc(metaData, i));
    }
    return new RowDesc(columnNames, columnDescriptors);
  }

  private final String name;
  private final String typeName;
  private final JDBCType type;

  public OracleColumnDesc(ResultSetMetaData md, int idx) throws SQLException {
    this.name = md.getColumnLabel(idx);
    this.typeName = md.getColumnTypeName(idx);
    this.type = find(md.getColumnType(idx));
  }

  private static JDBCType find(int vendorTypeNumber) {
    for (JDBCType jdbcType : JDBCType.values()) {
      if (jdbcType.getVendorTypeNumber() == vendorTypeNumber) {
        return jdbcType;
      }
    }
    return JDBCType.OTHER;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean isArray() {
    return type == JDBCType.ARRAY;
  }

  @Override
  public String typeName() {
    return typeName;
  }

  @Override
  public JDBCType jdbcType() {
    return type;
  }
}

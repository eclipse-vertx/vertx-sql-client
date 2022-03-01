/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.RowDesc;
import oracle.sql.TIMESTAMPTZ;

import java.sql.JDBCType;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OracleColumnDesc implements ColumnDescriptor {

  private static final IntObjectMap<JDBCType> TYPES_BY_VENDOR_TYPE_NUMBER;
  private static final Map<String, JDBCType> TYPES_BY_CLASSNAME;

  static {
    TYPES_BY_VENDOR_TYPE_NUMBER = new IntObjectHashMap<>();
    for (JDBCType type : JDBCType.values()) {
      TYPES_BY_VENDOR_TYPE_NUMBER.put(type.getVendorTypeNumber(), type);
    }
    TYPES_BY_CLASSNAME = new HashMap<>();
    TYPES_BY_CLASSNAME.put(TIMESTAMPTZ.class.getName(), JDBCType.TIMESTAMP_WITH_TIMEZONE);
  }

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
    name = md.getColumnLabel(idx);
    typeName = md.getColumnTypeName(idx);
    type = find(md, idx);
  }

  private JDBCType find(ResultSetMetaData md, int idx) throws SQLException {
    JDBCType res;
    if ((res = TYPES_BY_VENDOR_TYPE_NUMBER.get(md.getColumnType(idx))) == null) {
      if ((res = TYPES_BY_CLASSNAME.get(md.getColumnClassName(idx))) == null) {
        res = JDBCType.OTHER;
      }
    }
    return res;
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

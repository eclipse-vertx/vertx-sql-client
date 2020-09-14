/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.desc.ColumnDescriptor;

import java.sql.JDBCType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class RowDesc {

  private final List<String> columnNames;
  private final List<ColumnDescriptor> columnDescriptors;

  public RowDesc(List<String> columnNames) {
    this(columnNames, columnNames.stream().map(colName -> new ColumnDescriptor() {
      @Override
      public String name() {
        return colName;
      }
      @Override
      public JDBCType jdbcType() {
        return JDBCType.OTHER;
      }
      @Override
      public boolean isArray() {
        return false;
      }
    }).collect(Collectors.toList()));
  }

  public RowDesc(List<String> columnNames, List<ColumnDescriptor> columnDescriptors) {
    this.columnNames = columnNames;
    this.columnDescriptors = columnDescriptors;
  }

  public int columnIndex(String columnName) {
    if (columnName == null) {
      throw new NullPointerException("Column name must not be null");
    }
    return columnNames.indexOf(columnName);
  }

  public List<String> columnNames() {
    return columnNames;
  }

  public List<ColumnDescriptor> columnDescriptor() {
    return columnDescriptors;
  }

  @Override
  public String toString() {
    return "RowDesc{" +
      "columns=" + columnNames +
      '}';
  }
}

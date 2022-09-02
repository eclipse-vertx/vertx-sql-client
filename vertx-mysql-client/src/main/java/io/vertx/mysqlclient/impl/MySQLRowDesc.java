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
package io.vertx.mysqlclient.impl;

import io.vertx.mysqlclient.impl.datatype.DataFormat;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.RowDesc;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

public class MySQLRowDesc extends RowDesc {

  public final ColumnDefinition[] columnDefinitions;
  private final DataFormat dataFormat;

  private MySQLRowDesc(List<String> columnNames, List<ColumnDescriptor> columnDescriptors, ColumnDefinition[] columnDefinitions, DataFormat dataFormat) {
    super(columnNames, columnDescriptors);
    this.columnDefinitions = columnDefinitions;
    this.dataFormat = dataFormat;
  }


  public static MySQLRowDesc create(ColumnDefinition[] columnDefinitions, DataFormat dataFormat) {
    if (columnDefinitions.length == 0) {
      return new MySQLRowDesc(Collections.emptyList(), Collections.emptyList(), columnDefinitions, dataFormat);
    }
    List<String> columnNames = new ColumnNames(columnDefinitions);
    List<ColumnDescriptor> columnDescriptors = new ColumnDescriptors(columnDefinitions);
    return new MySQLRowDesc(columnNames, columnDescriptors, columnDefinitions, dataFormat);
  }

  public int size() {
    return columnDefinitions.length;
  }

  public ColumnDefinition get(int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException();
    }
    return columnDefinitions[index];
  }

  public DataFormat dataFormat() {
    return dataFormat;
  }

  private static class ColumnNames extends AbstractList<String> {
    private final ColumnDefinition[] columnDefinitions;

    public ColumnNames(ColumnDefinition[] columnDefinitions) {
      this.columnDefinitions = columnDefinitions;
    }

    @Override
    public String get(int index) {
      if (index < 0 || index >= columnDefinitions.length) {
        throw new IndexOutOfBoundsException();
      }
      return columnDefinitions[index].name();
    }

    @Override
    public int size() {
      return columnDefinitions.length;
    }
  }

  private static class ColumnDescriptors extends AbstractList<ColumnDescriptor> {
    private final ColumnDefinition[] columnDefinitions;

    public ColumnDescriptors(ColumnDefinition[] columnDefinitions) {
      this.columnDefinitions = columnDefinitions;
    }

    @Override
    public ColumnDescriptor get(int index) {
      if (index < 0 || index >= columnDefinitions.length) {
        throw new IndexOutOfBoundsException();
      }
      return columnDefinitions[index];
    }

    @Override
    public int size() {
      return columnDefinitions.length;
    }
  }
}

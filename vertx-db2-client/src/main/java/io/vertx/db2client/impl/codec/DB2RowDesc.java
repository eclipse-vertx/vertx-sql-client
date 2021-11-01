/*
 * Copyright (C) 2019,2020 IBM Corporation
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
 */
package io.vertx.db2client.impl.codec;

import io.vertx.db2client.impl.drda.ColumnMetaData;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.RowDesc;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;

class DB2RowDesc extends RowDesc {

  private final ColumnMetaData columnDefinitions;

  DB2RowDesc(ColumnMetaData columnDefinitions) {
    super(columnDefinitions.getColumnNames(), columns(columnDefinitions));
    this.columnDefinitions = columnDefinitions;
  }

  ColumnMetaData columnDefinitions() {
    return columnDefinitions;
  }

  private static List<ColumnDescriptor> columns(ColumnMetaData md) {
    List<String> names = md.getColumnNames();
    List<JDBCType> types = md.getJdbcTypes();
    List<ColumnDescriptor> columns = new ArrayList<>(names.size());
    for (int i = 0; i < names.size(); i++) {
      columns.add(new DB2ColumnDesc(names.get(i), types.get(i)));
    }
    return columns;
  }

  static class DB2ColumnDesc implements ColumnDescriptor {

    private final String name;
    private final JDBCType type;

    public DB2ColumnDesc(String name, JDBCType type) {
      this.name = name;
      this.type = type;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public JDBCType jdbcType() {
      return type;
    }

    @Override
    public boolean isArray() {
      // Array don't seem supported for the moment
      return false;
    }

    @Override
    public String typeName() {
      return null;
    }
  }
}

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
import io.vertx.sqlclient.internal.RowDescriptorBase;

import java.sql.JDBCType;
import java.util.List;

class DB2RowDescriptor extends RowDescriptorBase {

  private final ColumnMetaData columnMetaData;

  private DB2RowDescriptor(DB2ColumnDesc[] columnDescs, ColumnMetaData columnMetaData) {
    super(columnDescs);
    this.columnMetaData = columnMetaData;
  }

  ColumnMetaData columnDefinitions() {
    return columnMetaData;
  }

  static DB2RowDescriptor create(ColumnMetaData md) {
    List<String> names = md.getColumnNames();
    List<JDBCType> types = md.getJdbcTypes();
    DB2ColumnDesc[] columns = new DB2ColumnDesc[names.size()];
    for (int i = 0; i < columns.length; i++) {
      columns[i] = new DB2ColumnDesc(names.get(i), types.get(i));
    }
    return new DB2RowDescriptor(columns, md);
  }
}

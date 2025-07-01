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
import io.vertx.sqlclient.internal.RowDescriptor;

public class MySQLRowDescriptor extends RowDescriptor {

  private final ColumnDefinition[] columnDefinitions;
  private final DataFormat dataFormat;

  private MySQLRowDescriptor(ColumnDefinition[] columnDefinitions, DataFormat dataFormat) {
    super(columnDefinitions);
    this.columnDefinitions = columnDefinitions;
    this.dataFormat = dataFormat;
  }

  public static MySQLRowDescriptor create(ColumnDefinition[] columnDefinitions, DataFormat dataFormat) {
    return new MySQLRowDescriptor(columnDefinitions, dataFormat);
  }

  public ColumnDefinition[] columnDefinitions() {
    return columnDefinitions;
  }

  public ColumnDefinition get(int index) {
    return columnDefinitions[index];
  }

  public DataFormat dataFormat() {
    return dataFormat;
  }
}

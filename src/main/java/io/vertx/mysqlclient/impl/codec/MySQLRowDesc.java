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
package io.vertx.mysqlclient.impl.codec;

import io.vertx.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.vertx.sqlclient.impl.RowDesc;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MySQLRowDesc extends RowDesc {

  final ColumnDefinition[] columnDefinitions;

  MySQLRowDesc(ColumnDefinition[] columnDefinitions) {
    super(Collections.unmodifiableList(Stream.of(columnDefinitions)
      .map(ColumnDefinition::getName)
      .collect(Collectors.toList())));
    this.columnDefinitions = columnDefinitions;
  }
}

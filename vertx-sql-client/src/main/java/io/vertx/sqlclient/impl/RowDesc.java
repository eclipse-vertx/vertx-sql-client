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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class RowDesc {

  private final List<String> columnNames;
  private final Map<String, Integer> columns; // key - column name, value - column index (if multiple columns with same names then last)

  public RowDesc(List<String> columnNames) {
    this.columnNames = columnNames;
    this.columns = Collections.unmodifiableMap(IntStream.range(0, columnNames.size())
        .boxed()
        .collect(Collectors.toMap(columnNames::get, Function.identity(), (v1, v2) -> v2)));
  }

  public int columnIndex(String columnName) {
    if (columnName == null) {
      throw new NullPointerException("Column name must not be null");
    }
    return columns.getOrDefault(columnName, -1);
  }

  public List<String> columnNames() {
    return columnNames;
  }

  @Override
  public String toString() {
    return "RowDesc{" +
      "columns=" + columnNames +
      '}';
  }
}

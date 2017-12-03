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

package com.julienviet.pgclient.codec.decoder.message;

import com.julienviet.pgclient.codec.Column;
import com.julienviet.pgclient.codec.decoder.InboundMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class RowDescription implements InboundMessage {

  private final Column[] columns;
  private final List<String> columnNames;

  public RowDescription(Column[] columns) {
    this.columns = columns;
    this.columnNames = Collections.unmodifiableList(Stream.of(columns)
      .map(Column::getName)
      .collect(Collectors.toList()));
  }

  public Column[] columns() {
    return columns;
  }

  public List<String> columnNames() {
    return columnNames;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RowDescription that = (RowDescription) o;
    return Arrays.equals(columns, that.columns);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(columns);
  }


  @Override
  public String toString() {
    return "RowDescription{" +
      "columns=" + Arrays.toString(columns) +
      '}';
  }
}

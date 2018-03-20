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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgIterator;
import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.Row;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class PgResultImpl implements PgResult<Row> {

  final int updated;
  final List<String> columnNames;
  final RowImpl rows;
  final int size;
  PgResult<Row> next;

  public PgResultImpl(int updated) {
    this.updated = updated;
    this.rows = null;
    this.size = 0;
    this.columnNames = Collections.emptyList();
  }

  public PgResultImpl(int updated, List<String> columnNames, RowImpl rows, int size) {
    this.updated = updated;
    this.columnNames = columnNames;
    this.rows = rows;
    this.size = size;
  }

  @Override
  public List<String> columnsNames() {
    return columnNames;
  }

  @Override
  public int updatedCount() {
    return updated;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public PgIterator<Row> iterator() {
    return new PgIterator<Row>() {
      RowImpl current = rows;
      @Override
      public boolean hasNext() {
        return current != null;
      }
      @Override
      public Row next() {
        if (current == null) {
          throw new NoSuchElementException();
        }
        RowImpl r = current;
        current = current.next;
        return r;
      }
    };
  }

  @Override
  public PgResult<Row> next() {
    return next;
  }
}

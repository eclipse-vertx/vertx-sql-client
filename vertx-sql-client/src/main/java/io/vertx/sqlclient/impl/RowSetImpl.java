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

import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Row;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collector;

class RowSetImpl extends SqlResultBase<RowSet, RowSetImpl> implements RowSet {

  static Collector<Row, RowSetImpl, RowSet> COLLECTOR = Collector.of(
    RowSetImpl::new,
    (set, row) -> {
      if (set.head == null) {
        set.head = set.tail = (RowInternal) row;
      } else {
        set.tail.setNext((RowInternal) row);;
        set.tail = set.tail.getNext();
      }
    },
    (set1, set2) -> null, // Shall not be invoked as this is sequential
    (set) -> set
  );

  static Function<RowSet, RowSetImpl> FACTORY = rs -> (RowSetImpl) rs;

  private RowInternal head;
  private RowInternal tail;

  @Override
  public RowSet value() {
    return this;
  }

  @Override
  public RowIterator iterator() {
    return new RowIterator() {
      RowInternal current = head;
      @Override
      public boolean hasNext() {
        return current != null;
      }
      @Override
      public Row next() {
        if (current == null) {
          throw new NoSuchElementException();
        }
        RowInternal r = current;
        current = current.getNext();
        return r;
      }
    };
  }
}

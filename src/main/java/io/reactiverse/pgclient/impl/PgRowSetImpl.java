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
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collector;

class PgRowSetImpl extends PgResultBase<PgRowSet, PgRowSetImpl> implements PgRowSet {

  static Collector<Row, PgRowSetImpl, PgRowSet> COLLECTOR = Collector.of(
    PgRowSetImpl::new,
    (set, row) -> {
      if (set.head == null) {
        set.head = set.tail = (RowImpl) row;
      } else {
        set.tail.next = (RowImpl) row;
        set.tail = set.tail.next;
      }
    },
    (set1, set2) -> null, // Shall not be invoked as this is sequential
    (set) -> set
  );

  static Function<PgRowSet, PgRowSetImpl> FACTORY = rs -> (PgRowSetImpl) rs;

  private RowImpl head;
  private RowImpl tail;

  @Override
  public PgRowSet value() {
    return this;
  }

  @Override
  public PgIterator iterator() {
    return new PgIterator() {
      RowImpl current = head;
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
}

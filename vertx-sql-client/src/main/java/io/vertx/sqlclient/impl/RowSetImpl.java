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
import io.vertx.sqlclient.SqlResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collector;

class RowSetImpl<R> extends SqlResultBase<RowSet<R>> implements RowSet<R> {

  static Collector<Row, RowSetImpl<Row>, RowSet<Row>> COLLECTOR = Collector.of(
    RowSetImpl::new,
    (set, row) -> {
      set.list.add(row);
    },
    (set1, set2) -> null, // Shall not be invoked as this is sequential
    (set) -> set
  );

  static <U> Collector<Row, RowSetImpl<U>, RowSet<U>> collector(Function<Row, U> mapper) {
    return Collector.of(
      RowSetImpl::new,
      (set, row) -> {
        set.list.add(mapper.apply(row));
      },
      (set1, set2) -> null, // Shall not be invoked as this is sequential
      (set) -> set
    );
  }

  static Function<RowSet<Row>, RowSetImpl<Row>> FACTORY = rs -> (RowSetImpl) rs;

  static <U> Function<RowSet<U>, RowSetImpl<U>> factory() {
    return rs -> (RowSetImpl) rs;
  };

  private ArrayList<R> list = new ArrayList<>();

  @Override
  public RowSet<R> value() {
    return this;
  }

  @Override
  public RowIterator<R> iterator() {
    Iterator<R> i = list.iterator();
    return new RowIterator<R>() {
      @Override
      public boolean hasNext() {
        return i.hasNext();
      }
      @Override
      public R next() {
        return i.next();
      }
    };
  }

  @Override
  public RowSetImpl<R> next() {
    return (RowSetImpl<R>) super.next();
  }
}

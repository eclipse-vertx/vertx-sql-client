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

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Row;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Collector;

class RowSetImpl<R> extends SqlResultBase<RowSet<R>, RowSetImpl<R>> implements RowSet<R> {

  static Collector<Row, RowSetImpl<Row>, RowSet<Row>> COLLECTOR = Collector.of(
    RowSetImpl::new,
    (set, row) -> {
      set.list.add(row);
    },
    (set1, set2) -> null, // Shall not be invoked as this is sequential
    (set) -> set
  );

  static <R> Collector<Row, RowSetImpl<R>, RowSet<R>> mappingCollector(Function<JsonObject, R> f) {
    return Collector.of(
      RowSetImpl::new,
      (set, row) -> {
        JsonObject o = new JsonObject();
        int size = row.size();
        for (int idx = 0;idx < size;idx++) {
          String key = row.getColumnName(idx);
          Object value = row.getValue(idx);
          o.put(key, value);
        }
        R apply = f.apply(o);
        set.list.add(apply);
      },
      (set1, set2) -> null, // Shall not be invoked as this is sequential
      (set) -> set
    );
  }

  private static final Function FACTORY = rs -> (RowSetImpl) rs;

  static <R> Function<RowSet<R>, RowSetImpl<R>> rowSetAdapter() {
    return FACTORY;
  }

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
}

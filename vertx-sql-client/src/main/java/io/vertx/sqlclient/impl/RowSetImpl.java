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

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.impl.accumulator.Accumulator;
import io.vertx.sqlclient.impl.accumulator.ChunkedAccumulator;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collector;

class RowSetImpl<R> extends SqlResultBase<RowSet<R>> implements RowSet<R> {

  static Collector<Row, RowSetImpl<Row>, RowSet<Row>> COLLECTOR = Collector.of(
    RowSetImpl::new,
    RowSetImpl::add,
    (set1, set2) -> null, // Shall not be invoked as this is sequential
    (set) -> set
  );

  static <U> Collector<Row, RowSetImpl<U>, RowSet<U>> collector(Function<Row, U> mapper) {
    return Collector.of(
      RowSetImpl::new,
      (set, row) -> {
        set.add(mapper.apply(row));
      },
      (set1, set2) -> null, // Shall not be invoked as this is sequential
      (set) -> set
    );
  }

  static Function<RowSet<Row>, RowSetImpl<Row>> FACTORY = rs -> (RowSetImpl<Row>) rs;

  static <U> Function<RowSet<U>, RowSetImpl<U>> factory() {
    return rs -> (RowSetImpl<U>) rs;
  }

  private R firstRow;
  private Accumulator<R> rowAccumulator;

  @Override
  public RowSet<R> value() {
    return this;
  }

  private void add(R row) {
    if (rowAccumulator != null) {
      rowAccumulator.accept(row);
    } else if (firstRow != null) {
      rowAccumulator = new ChunkedAccumulator<>(IntUnaryOperator.identity());
      rowAccumulator.accept(firstRow);
      rowAccumulator.accept(row);
      firstRow = null;
    } else {
      firstRow = row;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public RowIterator<R> iterator() {
    if (rowAccumulator != null) {
      return rowAccumulator.iterator();
    }
    if (firstRow != null) {
      return singletonRowIterator(firstRow);
    }
    return (RowIterator<R>) EmptyRowIterator.INSTANCE;
  }

  @Override
  public RowSetImpl<R> next() {
    return (RowSetImpl<R>) super.next();
  }

  private static final class EmptyRowIterator<ROW> implements RowIterator<ROW> {

    static final EmptyRowIterator<Object> INSTANCE = new EmptyRowIterator<>();

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public ROW next() {
      throw new NoSuchElementException();
    }
  }

  private static <ROW> RowIterator<ROW> singletonRowIterator(ROW row) {
    return new RowIterator<ROW>() {

      boolean hasNext = true;

      @Override
      public boolean hasNext() {
        return hasNext;
      }

      @Override
      public ROW next() {
        if (hasNext) {
          hasNext = false;
          return row;
        }
        throw new NoSuchElementException();
      }
    };
  }
}

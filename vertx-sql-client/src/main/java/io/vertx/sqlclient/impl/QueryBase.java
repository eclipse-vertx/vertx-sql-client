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

import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
abstract class QueryBase<T, R extends SqlResult<T>> implements Query<R> {

  protected final QueryExecutor<T, ?, R> builder;

  public QueryBase(QueryExecutor<T, ?, R> builder) {
    this.builder = builder;
  }

  protected abstract <T2, R2  extends SqlResult<T2>> QueryBase<T2, R2> copy(QueryExecutor<T2, ?, R2> builder);

  @Override
  public <U> Query<SqlResult<U>> collecting(Collector<Row, ?, U> collector) {
    Objects.requireNonNull(collector, "Supplied collector must not be null");
    return copy(new QueryExecutor<>(builder.tracer(), SqlResultImpl::new, collector));
  }

  @Override
  public <U> Query<RowSet<U>> mapping(Function<Row, U> mapper) {
    Objects.requireNonNull(mapper, "Supplied mapper must not be null");
    return copy(new QueryExecutor<>(builder.tracer(), RowSetImpl.factory(), RowSetImpl.collector(mapper)));
  }
}

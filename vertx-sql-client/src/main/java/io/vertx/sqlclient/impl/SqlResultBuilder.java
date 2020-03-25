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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.ExtendedBatchQueryCommand;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * A query result handler for building a {@link SqlResult}.
 */
class SqlResultBuilder<T, R extends SqlResultBase<T, R>, L extends SqlResult<T>> {

  private final Function<T, R> factory;
  private final Collector<Row, ?, T> collector;

  public SqlResultBuilder(Function<T, R> factory,
                          Collector<Row, ?, T> collector) {
    this.factory = factory;
    this.collector = collector;
  }

  SqlResultHandler<T, R, L> createHandler(Handler<AsyncResult<L>> resultHandler) {
    return new SqlResultHandler<>(factory, resultHandler);
  }

  SimpleQueryCommand<T> createSimpleQuery(String sql,
                                          boolean singleton,
                                          SqlResultHandler<T, R, L> handler) {
    return new SimpleQueryCommand<>(sql, singleton, collector, handler);
  }

  ExtendedQueryCommand<T> createExtendedQuery(PreparedStatement preparedStatement,
                                              Tuple args,
                                              SqlResultHandler<T, R, L> handler) {
    return new ExtendedQueryCommand<>(
      preparedStatement,
      args,
      collector,
      handler);
  }

  ExtendedQueryCommand<T> createExtendedQuery(PreparedStatement preparedStatement,
                                              Tuple args,
                                              int fetch,
                                              String cursorId,
                                              boolean suspended,
                                              SqlResultHandler<T, R, L> handler) {
    return new ExtendedQueryCommand<>(
      preparedStatement,
      args,
      fetch,
      cursorId,
      suspended,
      collector,
      handler);
  }

  ExtendedBatchQueryCommand<T> createBatchCommand(PreparedStatement preparedStatement,
                                                  List<Tuple> argsList,
                                                  SqlResultHandler<T, R, L> handler) {
    return new ExtendedBatchQueryCommand<>(preparedStatement, argsList, collector, handler);
  }
}

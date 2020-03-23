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

import io.vertx.core.Promise;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.CommandScheduler;
import io.vertx.sqlclient.impl.command.ExtendedBatchQueryCommand;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * A query result handler for building a {@link SqlResult}.
 */
class SqlResultBuilder<T, R extends SqlResultBase<T>, L extends SqlResult<T>> {

  private final Function<T, R> factory;
  private final Collector<Row, ?, T> collector;

  public SqlResultBuilder(Function<T, R> factory,
                          Collector<Row, ?, T> collector) {
    this.factory = factory;
    this.collector = collector;
  }

  void execute(CommandScheduler scheduler,
                                    String sql,
                                    boolean autoCommit,
                                    boolean singleton,
                                    Promise<L> resultHandler) {
    SqlResultHandler<T, R, L> handler = new SqlResultHandler<>(factory, resultHandler);
    SimpleQueryCommand<T> cmd = new SimpleQueryCommand<>(sql, singleton, autoCommit, collector, handler);
    scheduler.schedule(cmd, handler);
  }

  SqlResultHandler<T, R, L> execute(CommandScheduler scheduler,
                                    PreparedStatement preparedStatement,
                                    boolean autoCommit,
                                    Tuple args,
                                    int fetch,
                                    String cursorId,
                                    boolean suspended,
                                    Promise<L> resultHandler) {
    String msg = preparedStatement.prepare((TupleInternal) args);
    if (msg != null) {
      resultHandler.fail(msg);
      return null;
    }
    SqlResultHandler<T, R, L> handler = new SqlResultHandler<>(factory, resultHandler);
    ExtendedQueryCommand<T> cmd = new ExtendedQueryCommand<>(
      preparedStatement,
      args,
      fetch,
      cursorId,
      suspended,
      autoCommit,
      collector,
      handler);
    scheduler.schedule(cmd, handler);
    return handler;
  }

  void batch(CommandScheduler scheduler,
             PreparedStatement preparedStatement,
             boolean autoCommit,
             List<Tuple> argsList,
             Promise<L> resultHandler) {
    for  (Tuple args : argsList) {
      String msg = preparedStatement.prepare((TupleInternal)args);
      if (msg != null) {
        resultHandler.fail(msg);
        return;
      }
    }
    SqlResultHandler<T, R, L> handler = new SqlResultHandler<>(factory, resultHandler);
    ExtendedBatchQueryCommand<T> cmd = new ExtendedBatchQueryCommand<>(preparedStatement, argsList, autoCommit, collector, handler);
    scheduler.schedule(cmd, handler);
  }
}

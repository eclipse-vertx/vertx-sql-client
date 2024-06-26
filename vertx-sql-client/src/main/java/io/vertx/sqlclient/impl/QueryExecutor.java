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

import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.internal.command.CommandScheduler;
import io.vertx.sqlclient.internal.command.ExtendedQueryCommand;
import io.vertx.sqlclient.internal.command.SimpleQueryCommand;
import io.vertx.sqlclient.internal.PreparedStatement;
import io.vertx.sqlclient.internal.TupleInternal;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Executes query.
 */
public class QueryExecutor<T, R extends SqlResultBase<T>, L extends SqlResult<T>> {

  private final Function<T, R> factory;
  private final Collector<Row, ?, T> collector;

  public QueryExecutor(Function<T, R> factory,
                       Collector<Row, ?, T> collector) {
    this.factory = factory;
    this.collector = collector;
  }

  private QueryResultBuilder<T, R, L> createHandler(PromiseInternal<L> promise) {
    return new QueryResultBuilder<>(factory, promise);
  }

  public void executeSimpleQuery(CommandScheduler scheduler,
                          String sql,
                          boolean autoCommit,
                          boolean singleton,
                          PromiseInternal<L> promise) {
    ContextInternal context = promise.context();
    QueryResultBuilder handler = createHandler(promise);
    scheduler.schedule(context, new SimpleQueryCommand<>(sql, singleton, autoCommit, collector, handler)).onComplete(handler);
  }

  QueryResultBuilder<T, R, L> executeExtendedQuery(CommandScheduler scheduler,
                                                   PreparedStatement preparedStatement,
                                                   PrepareOptions options,
                                                   boolean autoCommit,
                                                   Tuple arguments,
                                                   int fetch,
                                                   String cursorId,
                                                   boolean suspended,
                                                   PromiseInternal<L> promise) {
    ContextInternal context = promise.context();
    QueryResultBuilder handler = createHandler(promise);
    String msg = preparedStatement.prepare((TupleInternal) arguments);
    if (msg != null) {
      handler.fail(msg);
      return null;
    }
    ExtendedQueryCommand<T> cmd = ExtendedQueryCommand.createQuery(
      preparedStatement.sql(),
      options,
      preparedStatement,
      arguments,
      fetch,
      cursorId,
      suspended,
      autoCommit,
      collector,
      handler);
    scheduler.schedule(context, cmd).onComplete(handler);
    return handler;
  }

  public void executeExtendedQuery(CommandScheduler scheduler, String sql, PrepareOptions options, boolean autoCommit, Tuple arguments, PromiseInternal<L> promise) {
    ContextInternal context = (ContextInternal) promise.context();
    QueryResultBuilder handler = this.createHandler(promise);
    ExtendedQueryCommand cmd = createExtendedQueryCommand(sql, options, autoCommit, arguments, handler);
    scheduler.schedule(context, cmd).onComplete(handler);
  }

  private ExtendedQueryCommand<T> createExtendedQueryCommand(String sql,
                                                             PrepareOptions options,
                                                             boolean autoCommit,
                                                             Tuple tuple,
                                                             QueryResultBuilder<T, R, L> handler) {
    return ExtendedQueryCommand.createQuery(
      sql,
      options,
      null,
      tuple,
      autoCommit,
      collector,
      handler);
  }

  void executeBatchQuery(CommandScheduler scheduler,
                         PrepareOptions options,
                         PreparedStatement preparedStatement,
                         boolean autoCommit,
                         List<Tuple> batch,
                         PromiseInternal<L> promise) {
    ContextInternal context = promise.context();
    QueryResultBuilder handler = createHandler(promise);
    for  (Tuple args : batch) {
      String msg = preparedStatement.prepare((TupleInternal)args);
      if (msg != null) {
        handler.fail(msg);
        return;
      }
    }
    ExtendedQueryCommand<T> cmd = ExtendedQueryCommand.createBatch(preparedStatement.sql(), options, preparedStatement, batch, autoCommit, collector, handler);
    scheduler.schedule(context, cmd).onComplete(handler);
  }

  public void executeBatchQuery(CommandScheduler scheduler, String sql, PrepareOptions options, boolean autoCommit, List<Tuple> batch, PromiseInternal<L> promise) {
    ContextInternal context = promise.context();
    QueryResultBuilder handler = createHandler(promise);
    ExtendedQueryCommand<T> cmd = createBatchQueryCommand(sql, options, autoCommit, batch, handler);
    scheduler.schedule(context, cmd).onComplete(handler);
  }

  private ExtendedQueryCommand<T> createBatchQueryCommand(String sql,
                                                          PrepareOptions options,
                                                          boolean autoCommit,
                                                          List<Tuple> argsList,
                                                          QueryResultBuilder<T, R, L> handler) {
    return ExtendedQueryCommand.createBatch(sql, options, null, argsList, autoCommit, collector, handler);
  }
}

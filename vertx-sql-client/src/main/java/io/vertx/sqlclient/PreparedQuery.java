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

package io.vertx.sqlclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.Future;
import io.vertx.sqlclient.impl.ArrayTuple;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * A prepared statement, the statement is pre-compiled and
 * it's more efficient to execute the statement for multiple times.
 * In addition, this kind of statement provides protection against SQL injection attacks.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PreparedQuery<T> {

  /**
   * Calls {@link #execute(Tuple, Handler)} with an empty tuple argument.
   */
  default void execute(Handler<AsyncResult<T>> handler) {
    execute(ArrayTuple.EMPTY, handler);
  }

  /**
   * Like {@link #execute(Handler)} but returns a {@code Future} of the asynchronous result
   */
  default Future<T> execute() {
    return execute(ArrayTuple.EMPTY);
  }

  /**
   * Create a cursor with the provided {@code arguments}.
   *
   * @param args the list of arguments
   */
  void execute(Tuple args, Handler<AsyncResult<T>> handler);

  /**
   * Like {@link #execute(Tuple, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<T> execute(Tuple args);

  /**
   * Use the specified {@code collector} for collecting the query result to {@code <R>}.
   */
  @GenIgnore
  <R> PreparedQuery<SqlResult<R>> collecting(Collector<Row, ?, R> collector);

  /**
   * Use the specified {@code mapper} for mapping {@link Row} to {@code <U>}.
   */
  <U> PreparedQuery<RowSet<U>> mapping(Function<Row, U> mapper);

  /**
   * @return create a query cursor with a {@code fetch} size and empty arguments
   */
  default Cursor cursor() {
    return cursor(ArrayTuple.EMPTY);
  }

  /**
   * Create a cursor with the provided {@code arguments}.
   *
   * @param args the list of arguments
   * @return the query
   */
  Cursor cursor(Tuple args);

  /**
   * Execute the prepared query with a cursor and createStream the result. The createStream opens a cursor
   * with a {@code fetch} size to fetch the results.
   * <p/>
   * Note: this requires to be in a transaction, since cursors require it.
   *
   * @param fetch the cursor fetch size
   * @param args the prepared query arguments
   * @return the createStream
   */
  RowStream<Row> createStream(int fetch, Tuple args);

  /**
   * Execute a batch.
   *
   * @param argsList the list of tuple for the batch
   */
  void batch(List<Tuple> argsList, Handler<AsyncResult<T>> handler);

  /**
   * Like {@link #batch(List, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<T> batch(List<Tuple> argsList);

  /**
   * Close the prepared query and release its resources.
   */
  Future<Void> close();

  /**
   * Like {@link #close()} but notifies the {@code completionHandler} when it's closed.
   */
  void close(Handler<AsyncResult<Void>> completionHandler);

}

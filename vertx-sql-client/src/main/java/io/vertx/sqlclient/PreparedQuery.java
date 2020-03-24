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
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * A query.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PreparedQuery<T> extends Query<T> {

  /**
   * Execute the query.
   *
   * @param handler the handler receiving the response
   */
  void execute(Tuple tuple, Handler<AsyncResult<T>> handler);

  /**
   * Like {@link #execute(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<T> execute(Tuple tuple);

  /**
   * Execute the query.
   *
   * @param batch the batch of tuples
   * @param handler the handler receiving the response
   */
  void executeBatch(List<Tuple> batch, Handler<AsyncResult<T>> handler);

  /**
   * Like {@link #executeBatch(List, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<T> executeBatch(List<Tuple> batch);

  /**
   * Use the specified {@code collector} for collecting the query result to {@code <R>}.
   */
  @GenIgnore
  <R> PreparedQuery<SqlResult<R>> collecting(Collector<Row, ?, R> collector);

  /**
   * Use the specified {@code mapper} for mapping {@link Row} to {@code <U>}.
   */
  <U> PreparedQuery<RowSet<U>> mapping(Function<Row, U> mapper);

}

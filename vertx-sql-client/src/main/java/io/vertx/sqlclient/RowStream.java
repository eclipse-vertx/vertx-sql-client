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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

/**
 * A row oriented stream.
 */
@VertxGen
public interface RowStream<T> extends ReadStream<T> {

  @Fluent
  @Override
  RowStream<T> exceptionHandler(Handler<Throwable> handler);

  @Fluent
  @Override
  RowStream<T> handler(Handler<T> handler);

  @Fluent
  @Override
  RowStream<T> pause();

  @Fluent
  @Override
  RowStream<T> resume();

  @Fluent
  @Override
  RowStream<T> endHandler(Handler<Void> endHandler);

  @Fluent
  @Override
  RowStream<T> fetch(long l);

  /**
   * Close the stream and release the resources.
   */
  Future<Void> close();

  /**
   * Close the stream and release the resources.
   *
   * @param completionHandler the completion handler for this operation
   */
  void close(Handler<AsyncResult<Void>> completionHandler);

}

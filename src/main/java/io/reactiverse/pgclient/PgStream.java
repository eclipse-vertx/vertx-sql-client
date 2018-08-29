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

package io.reactiverse.pgclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

/**
 * A row oriented stream.
 */
@VertxGen
public interface PgStream<T> extends ReadStream<T> {

  @Override
  PgStream<T> exceptionHandler(Handler<Throwable> handler);

  @Override
  PgStream<T> handler(Handler<T> handler);

  @Override
  PgStream<T> pause();

  @Override
  PgStream<T> resume();

  @Override
  PgStream<T> endHandler(Handler<Void> endHandler);

  /**
   * Close the stream and release the resources.
   */
  void close();

  /**
   * Close the stream and release the resources.
   *
   * @param completionHandler the completion handler for this operation
   */
  void close(Handler<AsyncResult<Void>> completionHandler);

}

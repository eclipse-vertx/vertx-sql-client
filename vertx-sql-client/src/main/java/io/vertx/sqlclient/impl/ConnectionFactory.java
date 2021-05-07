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

import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;

public interface ConnectionFactory extends Closeable {

  static EventLoopContext asEventLoopContext(ContextInternal ctx) {
    if (ctx instanceof EventLoopContext) {
      return (EventLoopContext) ctx;
    } else {
      return ctx.owner().createEventLoopContext(ctx.nettyEventLoop(), ctx.workerPool(), ctx.classLoader());
    }
  }

  /**
   * Connect to the database and returns a connection.
   *
   * @return a connection future
   */
  void connect(Promise<Connection> promise);

  default void close(Promise<Void> promise) {
    promise.complete();
  }
}

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
package io.vertx.sqlclient.spi.protocol;

import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.internal.ContextInternal;

@FunctionalInterface
public interface CommandScheduler {

  default <R> Future<R> schedule(ContextInternal context, CommandBase<R> cmd) {
    Promise<R> promise = context.promise();
    schedule(cmd, promise);
    return promise.future();
  }

  <R> void schedule(CommandBase<R> cmd, Completable<R> handler);

}

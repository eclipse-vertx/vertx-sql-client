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

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class ExtendedQueryResultHandler<T> implements QueryResultHandler<T> {

  private final Handler<AsyncResult<PgResult<T>>> handler;
  private Throwable failure;
  private boolean suspended;
  private PgResult<T> result;

  public ExtendedQueryResultHandler(Handler<AsyncResult<PgResult<T>>> handler) {
    this.handler = handler;
  }

  public boolean isSuspended() {
    return suspended;
  }

  @Override
  public void handleSuspend() {
    this.suspended = true;
  }

  @Override
  public void handleFailure(Throwable cause) {
    failure = cause;
    handler.handle(Future.failedFuture(cause));
  }

  @Override
  public void handleResult(PgResult<T> result) {
    this.result = result;
  }

  @Override
  public void handleEnd() {
    if (failure == null) {
      handler.handle(Future.succeededFuture(result));
    }
  }
}

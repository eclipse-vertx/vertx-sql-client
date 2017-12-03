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

import java.util.*;

public class SimpleQueryResultHandler<T> implements QueryResultHandler<T> {

  private final Handler<AsyncResult<PgResult<T>>> handler;
  private Throwable failure;
  private final Queue<PgResult<T>> results = new ArrayDeque<>(1);

  public SimpleQueryResultHandler(Handler<AsyncResult<PgResult<T>>> handler) {
    this.handler = handler;
  }

  @Override
  public void handleResult(PgResult<T> result) {
    results.add(result);
  }

  public boolean hasNext() {
    return results.size() > 0;
  }

  public PgResult<T> next() {
    if (results.size() > 0) {
      return results.poll();
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void handleSuspend() {
  }

  @Override
  public void handleFailure(Throwable cause) {
    failure = cause;
    handler.handle(Future.failedFuture(cause));
  }

  @Override
  public void handleEnd() {
    if (failure == null) {
      handler.handle(Future.succeededFuture(results.poll()));
    }
  }
}

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
import io.vertx.core.Handler;

public class ExtendedQueryResultHandler<T> implements QueryResultHandler<T> {

  private final Handler<AsyncResult<PgResult<T>>> handler;
  private PgResult<T> result;
  private boolean suspended;

  ExtendedQueryResultHandler(Handler<AsyncResult<PgResult<T>>> handler) {
    this.handler = handler;
  }

  boolean isSuspended() {
    return suspended;
  }

  @Override
  public void handleResult(PgResult<T> result) {
    this.result = result;
  }

  @Override
  public void handle(AsyncResult<Boolean> res) {
    suspended = res.succeeded() && res.result();
    handler.handle(res.map(result));
  }
}

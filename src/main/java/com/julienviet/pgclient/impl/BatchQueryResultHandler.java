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
import com.julienviet.pgclient.Row;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

class BatchQueryResultHandler implements QueryResultHandler<Row> {

  private final Handler<AsyncResult<PgResult<Row>>> handler;
  private PgResult<Row> head;
  private PgResultImpl tail;

  public BatchQueryResultHandler(int size, Handler<AsyncResult<PgResult<Row>>> handler) {
    this.handler = handler;
  }

  @Override
  public void handleResult(PgResult<Row> result) {
    if (head == null) {
      head = result;
      tail = (PgResultImpl) result;
    } else {
      tail.next = result;
      tail = (PgResultImpl) result;
    }
  }

  @Override
  public void handle(AsyncResult<Boolean> res) {
    handler.handle(res.map(head));
  }
}

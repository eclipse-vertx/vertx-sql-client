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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.impl.codec.decoder.RowDescription;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.function.Function;

/**
 * A query result handler for building a {@link PgResult}.
 */
public class PgResultBuilder<T, R extends PgResultBase<T, R>, L extends PgResult<T>> implements QueryResultHandler<T>, Handler<AsyncResult<Boolean>> {

  private final Handler<AsyncResult<L>> handler;
  private final Function<T, R> factory;
  private R first;
  private boolean suspended;

  PgResultBuilder(Function<T, R> factory, Handler<AsyncResult<L>> handler) {
    this.factory = factory;
    this.handler = handler;
  }

  @Override
  public void handleResult(int updatedCount, int size, RowDescription desc, T result) {
    R r = factory.apply(result);
    r.updated = updatedCount;
    r.size = size;
    r.columnNames = desc != null ? desc.columnNames() : null;
    handleResult(r);
  }

  private void handleResult(R result) {
    if (first == null) {
      first = result;
    } else {
      R h = first;
      while (h.next != null) {
        h = h.next;
      }
      h.next = result;
    }
  }

  @Override
  public void handle(AsyncResult<Boolean> res) {
    suspended = res.succeeded() && res.result();
    handler.handle((AsyncResult<L>) res.map(first));
  }

  public boolean isSuspended() {
    return suspended;
  }
}

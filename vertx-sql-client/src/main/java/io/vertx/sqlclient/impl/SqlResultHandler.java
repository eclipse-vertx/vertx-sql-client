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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.PropertyKind;

import java.util.HashMap;
import java.util.function.Function;

/**
 * A query result handler for building a {@link SqlResult}.
 */
class SqlResultHandler<T, R extends SqlResultBase<T, R>, L extends SqlResult<T>> implements QueryResultHandler<T>, Handler<AsyncResult<Boolean>> {

  private final Handler<AsyncResult<L>> handler;
  private final Function<T, R> factory;
  private R first;
  private R current;
  private Throwable failure;
  private boolean suspended;

  SqlResultHandler(Function<T, R> factory, Handler<AsyncResult<L>> handler) {
    this.factory = factory;
    this.handler = handler;
  }

  @Override
  public void handleResult(int updatedCount, int size, RowDesc desc, T result, Throwable failure) {
    if (failure != null) {
      this.failure = failure;
    } else {
      R r = factory.apply(result);
      r.updated = updatedCount;
      r.size = size;
      r.columnNames = desc != null ? desc.columnNames() : null;
      handleResult(r);
    }
  }

  private void handleResult(R result) {
    R c = current;
    if (c == null) {
      first = result;
      current = result;
    } else {
      c.next = result;
      current = result;
    }
  }

  @Override
  public <V> void addProperty(PropertyKind<V> property, V value) {
    R r = this.current;
    if (r != null) {
      if (r.properties == null) {
        // lazy init
        r.properties = new HashMap<>();
      }
      r.properties.put(property, value);
    }
  }

  @Override
  public void handle(AsyncResult<Boolean> ar) {
    if (ar.succeeded()) {
      complete(ar.result());
    } else if (ar.failed()) {
      fail(ar.cause());
    }
  }

  void complete(Boolean result) {
    suspended = result;
    if (failure != null) {
      handler.handle(Future.failedFuture(failure));
    } else {
      handler.handle(Future.succeededFuture((L) first));
    }
  }

  void fail(Throwable cause) {
    handler.handle(Future.failedFuture(cause));
  }

  public boolean isSuspended() {
    return suspended;
  }
}

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

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.sqlclient.PropertyKind;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A query result handler for building a {@link SqlResult}.
 */
public class SqlResultBuilder<T, R extends SqlResultBase<T, R>, L extends SqlResult<T>> implements QueryResultHandler<T>, Promise<Boolean> {

  private final Promise<L> handler;
  private final Function<T, R> factory;
  private R first;
  private boolean suspended;

  SqlResultBuilder(Function<T, R> factory, Promise<L> handler) {
    this.factory = factory;
    this.handler = handler;
  }

  @Override
  public void handleResult(int updatedCount, int size, RowDesc desc, T result, Throwable failure) {
    R r = factory.apply(result);
    r.failure = failure;
    r.updated = updatedCount;
    r.size = size;
    r.columnNames = desc != null ? desc.columnNames() : null;
    handleResult(r, failure);
  }

  private void handleResult(R result, Throwable failure) {
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
  public <V> void addProperty(PropertyKind<V> property, V value) {
    if (first != null) {
      R r = first;
      while (r.next != null) {
        r = r.next;
      }
      if (r.properties == null) {
        // lazy init
        r.properties = new HashMap<>();
      }
      r.properties.put(property, value);
    }
  }

  @Override
  public boolean tryComplete(Boolean result) {
    suspended = result;
    if (first == null) {
      return handler.tryComplete();
    } else if (first.failure != null) {
      return handler.tryFail(first.failure);
    } else {
      return handler.tryComplete((L) first);
    }
  }

  @Override
  public boolean tryFail(Throwable cause) {
    return handler.tryFail(cause);
  }

  @Override
  public Future<Boolean> future() {
    throw new UnsupportedOperationException();
  }

  public boolean isSuspended() {
    return suspended;
  }
}

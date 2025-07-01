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

import io.vertx.core.Completable;
import io.vertx.core.Promise;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.internal.QueryResultHandler;
import io.vertx.sqlclient.internal.RowDescriptorBase;

import java.util.function.Function;

/**
 * A query result for building a {@link SqlResult}.
 */
public class QueryResultBuilder<T, R extends SqlResultBase<T>, L extends SqlResult<T>> implements QueryResultHandler<T>, Completable<Boolean> {

  private final Promise<L> handler;
  private final Function<T, R> factory;
  public R first;
  private R current;
  private Throwable failure;
  private boolean suspended;

  QueryResultBuilder(Function<T, R> factory, PromiseInternal<L> handler) {
    this.factory = factory;
    this.handler = handler;
  }

  @Override
  public void handleResult(int updatedCount, int size, RowDescriptorBase desc, T result, Throwable failure) {
    if (failure != null) {
      this.failure = failure;
    } else {
      R r = factory.apply(result);
      r.updated = updatedCount;
      r.size = size;
      r.rowDescriptor = desc;
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
        r.properties = new PropertyKindMap();
      }
      r.properties.put(property, value);
    }
  }

  @Override
  public void complete(Boolean aBoolean, Throwable throwable) {
    if (throwable == null) {
      suspended = aBoolean;
      if (failure != null) {
        handler.tryFail(failure);
      } else {
        handler.tryComplete((L) first);
      }
    } else {
      handler.tryFail(throwable);
    }
  }

  public boolean isSuspended() {
    return suspended;
  }
}

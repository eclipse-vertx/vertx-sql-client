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
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

import java.util.Collections;
import java.util.HashMap;
import java.util.function.Function;

/**
 * A query result for building a {@link SqlResult}.
 */
class QueryResultBuilder<T, R extends SqlResultBase<T>, L extends SqlResult<T>> implements QueryResultHandler<T>, Promise<Boolean> {

  private final Promise<L> handler;
  private final Function<T, R> factory;
  private final ContextInternal context;
  private final QueryTracer tracer;
  private final Object tracingPayload;
  private final ClientMetrics metrics;
  private final Object metric;
  private R first;
  private R current;
  private Throwable failure;
  private boolean suspended;

  QueryResultBuilder(Function<T, R> factory, QueryTracer tracer, Object tracingPayload, ClientMetrics metrics, Object metric, PromiseInternal<L> handler) {
    this.factory = factory;
    this.context = handler.context();
    this.tracer = tracer;
    this.tracingPayload = tracingPayload;
    this.metrics = metrics;
    this.metric = metric;
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
      r.columnDescriptors = desc != null ? desc.columnDescriptor() : null;
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
        r.properties = Collections.singletonMap(property, value);
      } else {
        if (r.properties.size() == 1) {
          r.properties = new HashMap<>(r.properties);
        }
        r.properties.put(property, value);
      }
    }
  }

  @Override
  public boolean tryComplete(Boolean result) {
    suspended = result;
    if (failure != null) {
      return tryFail(failure);
    } else {
      boolean completed = handler.tryComplete((L) first);
      if (completed) {
        if (metrics != null) {
          metrics.responseBegin(metric, null);
          metrics.responseEnd(metric);
        }
        if (tracer != null) {
          tracer.receiveResponse(context, tracingPayload, first, null);
        }
      }
      return completed;
    }
  }

  @Override
  public boolean tryFail(Throwable cause) {
    boolean completed = handler.tryFail(cause);
    if (completed) {
      if (tracer != null) {
        tracer.receiveResponse(context, tracingPayload, null, cause);
      }
      if (metrics != null) {
        metrics.requestReset(metric);
      }
    }
    return completed;
  }

  @Override
  public Future<Boolean> future() {
    return handler.future().map(l -> isSuspended());
  }

  public boolean isSuspended() {
    return suspended;
  }
}

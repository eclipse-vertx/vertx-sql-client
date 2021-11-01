/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.RowDesc;

import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;

public class RowReader<R, A> implements Flow.Subscriber<Row> {

  private final Flow.Publisher<Row> publisher;
  private final ContextInternal context;
  private final RowDesc description;
  private final QueryResultHandler<R> handler;
  private volatile Flow.Subscription subscription;
  private final Promise<Void> subscriptionPromise;
  private Promise<Void> readPromise;
  private volatile boolean completed;
  private volatile Throwable failed;
  private final Collector<Row, A, R> collector;
  private A accumulator;
  private int count;
  private final AtomicInteger toRead = new AtomicInteger();

  private final AtomicBoolean wip = new AtomicBoolean();

  public RowReader(Flow.Publisher<Row> publisher, Collector<Row, A, R> collector, RowDesc description, Promise<Void> promise,
    QueryResultHandler<R> handler,
    ContextInternal context) {
    this.publisher = publisher;
    this.description = description;
    this.subscriptionPromise = promise;
    this.handler = handler;
    this.context = context;
    this.collector = collector;
  }

  public static <R> Future<RowReader<R, ?>> create(Flow.Publisher<Row> publisher,
                                                Collector<Row, ?, R> collector,
                                                ContextInternal context,
                                                QueryResultHandler<R> handler,
                                                RowDesc description) {
    Promise<Void> promise = context.promise();
    RowReader<R, ?> reader = new RowReader<>(publisher, collector, description, promise, handler, context);
    reader.subscribe();
    return promise.future().map(reader);
  }

  public Future<Void> read(int fetchSize) {
    if (subscription == null) {
      return context.failedFuture(new IllegalStateException("Not subscribed"));
    }
    if (completed) {
      return context.succeededFuture();
    }
    if (failed != null) {
      return context.failedFuture(failed);
    }
    if (wip.compareAndSet(false, true)) {
      toRead.set(fetchSize);
      accumulator = collector.supplier().get();
      count = 0;
      readPromise = context.promise();
      subscription.request(fetchSize);
      return readPromise.future();
    } else {
      return context.failedFuture(new IllegalStateException("Read already in progress"));
    }
  }

  private void subscribe() {
    this.publisher.subscribe(this);
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    this.subscription = subscription;
    context.runOnContext(x -> this.subscriptionPromise.complete(null));
  }

  @Override
  public void onNext(Row item) {
    collector.accumulator().accept(accumulator, item);
    count++;
    if (toRead.decrementAndGet() == 0 && wip.compareAndSet(true, false)) {
      R result = collector.finisher().apply(accumulator);
      try {
        handler.handleResult(count, count, description, result, null);
      } catch (Exception e) {
        e.printStackTrace();
      }
      readPromise.complete();
    }
  }

  @Override
  public void onError(Throwable throwable) {
    if (wip.compareAndSet(true, false)) {
      failed = throwable;
      handler.handleResult(0, 0, description, null, throwable);
    }
  }

  @Override
  public void onComplete() {
    if (wip.compareAndSet(true, false)) {
      completed = true;
      context.runOnContext(x -> readPromise.complete(null));
    }
  }
}

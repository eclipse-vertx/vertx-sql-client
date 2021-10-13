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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.RowSetImpl;
import io.vertx.sqlclient.impl.SqlResultBase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;

public class RowReader implements Flow.Subscriber<Row> {

  private final Flow.Publisher<Row> publisher;
  private final ContextInternal context;
  private final RowDesc description;
  private final QueryResultHandler<RowSet<Row>> handler;
  private volatile Flow.Subscription subscription;
  private final Promise<Void> subscriptionPromise;
  private Promise<Void> readPromise;
  private volatile boolean completed;
  private volatile Throwable failed;
  private volatile OracleRowSet collector;
  private final AtomicInteger toRead = new AtomicInteger();

  private final AtomicBoolean wip = new AtomicBoolean();

  public RowReader(Flow.Publisher<Row> publisher, RowDesc description, Promise<Void> promise,
    QueryResultHandler<RowSet<Row>> handler,
    ContextInternal context) {
    this.publisher = publisher;
    this.description = description;
    this.subscriptionPromise = promise;
    this.handler = handler;
    this.context = context;
  }

  public static Future<RowReader> create(Flow.Publisher<Row> publisher, ContextInternal context,
    QueryResultHandler<RowSet<Row>> handler, RowDesc description) {
    Promise<Void> promise = context.promise();
    RowReader reader = new RowReader(publisher, description, promise, handler, context);
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
      collector = new OracleRowSet(description);
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
    collector.add(item);
    if (toRead.decrementAndGet() == 0 && wip.compareAndSet(true, false)) {
      try {
        handler.handleResult(collector.rowCount(), collector.size(), description, collector, null);
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

  static class OracleRowSet extends SqlResultBase<RowSet<Row>> implements RowSet<Row> {

    public static Function<RowSet<Row>, OracleRowSet> FACTORY = rs -> (OracleRowSet) rs;
    public static Collector<Row, OracleRowSet, RowSet<Row>> COLLECTOR = Collector.of(
      () -> new OracleRowSet(null),
      (set, row) -> {
        set.rows.add(row);
      },
      (set1, set2) -> null, // Shall not be invoked as this is sequential
      (set) -> set
    );

    private final List<Row> rows = new ArrayList<>();
    private final RowDesc desc;

    private OracleRowSet(RowDesc desc) {
      this.desc = desc;
    }

    @Override
    public RowIterator<Row> iterator() {
      Iterator<Row> iterator = rows.iterator();
      return new RowIterator<>() {
        @Override
        public boolean hasNext() {
          return iterator.hasNext();
        }

        @Override
        public Row next() {
          return iterator.next();
        }
      };
    }

    @Override
    public int rowCount() {
      return rows.size();
    }

    @Override
    public List<String> columnsNames() {
      return desc.columnNames();
    }

    @Override
    public List<ColumnDescriptor> columnDescriptors() {
      return desc.columnDescriptor();
    }

    @Override
    public int size() {
      return rows.size();
    }

    @Override
    public <V> V property(PropertyKind<V> propertyKind) {
      return null; // TODO
    }

    @Override
    public RowSet<Row> value() {
      return this;
    }

    @Override
    public RowSet<Row> next() {
      return null;
    }

    public void add(Row item) {
      rows.add(item);
    }
  }
}

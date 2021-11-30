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
import io.vertx.core.VertxException;
import io.vertx.core.impl.ContextInternal;
import io.vertx.oracleclient.impl.commands.OraclePreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.RowDesc;
import oracle.jdbc.OracleResultSet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;

import static io.vertx.oracleclient.impl.Helper.convertSqlValue;

public class RowReader<R, A> implements Flow.Subscriber<Row>, Function<oracle.jdbc.OracleRow, Row> {

  private final List<String> types;
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

  public RowReader(OracleResultSet ors, Collector<Row, A, R> collector, Promise<Void> subscriptionPromise, QueryResultHandler<R> handler, ContextInternal context) throws SQLException {
    int cols = ors.getMetaData().getColumnCount();
    types = new ArrayList<>(cols);
    for (int i = 1; i <= cols; i++) {
      types.add(ors.getMetaData().getColumnClassName(i));
    }
    this.publisher = ors.publisherOracle(this);
    this.description = OracleColumnDesc.rowDesc(ors.getMetaData());
    this.subscriptionPromise = subscriptionPromise;
    this.handler = handler;
    this.context = context;
    this.collector = collector;
  }

  public static <R> Future<RowReader<R, ?>> create(OracleResultSet ors,
                                                   Collector<Row, ?, R> collector,
                                                   ContextInternal context,
                                                   QueryResultHandler<R> handler) throws SQLException {
    Promise<Void> promise = context.promise();
    RowReader<R, ?> reader = new RowReader<>(ors, collector, promise, handler, context);
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

  @Override
  public Row apply(oracle.jdbc.OracleRow oracleRow) {
    try {
      return transform(types, description, oracleRow);
    } catch (SQLException e) {
      throw new VertxException(e);
    }
  }

  private static Row transform(List<String> ors, RowDesc desc, oracle.jdbc.OracleRow or) throws SQLException {
    Row row = new OracleRow(desc);
    for (int i = 1; i <= desc.columnNames().size(); i++) {
      Object res = convertSqlValue(or.getObject(i, getType(ors.get(i - 1))));
      row.addValue(res);
    }
    return row;
  }

  private static Class<?> getType(String cn) {
    try {
      return OraclePreparedQuery.class.getClassLoader().loadClass(cn);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}

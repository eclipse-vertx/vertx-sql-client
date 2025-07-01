/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
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
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.oracleclient.OracleException;
import io.vertx.oracleclient.impl.commands.OracleResponse;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.internal.RowDescriptorBase;
import oracle.jdbc.OracleResultSet;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;

import static io.vertx.oracleclient.impl.Helper.*;

public class RowReader<C, R> implements Flow.Subscriber<Row>, Function<oracle.jdbc.OracleRow, Row> {

  private static final Logger LOG = LoggerFactory.getLogger(RowReader.class);

  private final ContextInternal context;
  private final List<Class<?>> classes;
  private final RowDescriptorBase description;
  private final Statement resultSetStatement;

  // The following fields must be read/updated on the RowReader context

  private final Collector<Row, C, R> collector;

  // The following fields become non-null depending on the state  of the RowReader
  // The states are: subscribing, subscribed, fetching, fetched, closing, closed

  private Flow.Subscription subscription;
  private Promise<OracleResponse<R>> readPromise;
  private ArrayDeque<Row> queue;
  private int fetchSize;
  private Promise<Void> closePromise;

  public RowReader(ContextInternal context, Collector<Row, C, R> collector, OracleResultSet ors) throws SQLException {
    this.context = context;
    this.collector = collector;
    resultSetStatement = ors.getStatement();
    ResultSetMetaData metaData = ors.getMetaData();
    int cols = metaData.getColumnCount();
    classes = new ArrayList<>(cols);
    for (int i = 1; i <= cols; i++) {
      classes.add(getType(metaData.getColumnClassName(i)));
    }
    Flow.Publisher<Row> publisher = ors.publisherOracle(this);
    description = OracleRowDescriptor.create(metaData);
    publisher.subscribe(this);
  }

  @Override
  public void onSubscribe(Flow.Subscription sub) {
    context.runOnContext(v -> {
      if (closePromise != null) {
        sub.cancel();
        return;
      }
      subscription = sub;
    });
  }

  public Future<OracleResponse<R>> read(int fetchSize) {
    Promise<OracleResponse<R>> promise = context.owner().promise();
    context.runOnContext(v -> {
      if (closePromise != null) {
        promise.fail("RowReader is closed");
        return;
      }
      if (subscription == null) {
        promise.fail("Subscription is not ready yet");
        return;
      }
      if (readPromise != null) {
        promise.fail("Read is already in progress");
        return;
      }
      this.fetchSize = fetchSize;
      readPromise = context.promise();
      if (queue == null) {
        queue = new ArrayDeque<>(fetchSize + 1);
        executeBlocking(context, () -> subscription.request(fetchSize + 1));
      } else {
        executeBlocking(context, () -> subscription.request(fetchSize));
      }
      readPromise.future().onComplete(promise);
    });
    return promise.future();
  }

  @Override
  public void onNext(Row item) {
    context.runOnContext(v -> {
      if (closePromise != null) {
        return;
      }
      queue.add(item);
      if (queue.size() > fetchSize) {
        OracleResponse<R> response = createResponse();
        readPromise.complete(response);
        readPromise = null;
      }
    });
  }

  @Override
  public void onError(Throwable throwable) {
    context.runOnContext(v -> {
      if (closePromise != null) {
        LOG.trace("Dropping subscription failure", throwable);
        return;
      }
      closePromise = context.promise();
      executeBlocking(context, () -> closeQuietly(resultSetStatement)).otherwiseEmpty().onComplete(closePromise);
      readPromise.fail(throwable);
    });
  }

  @Override
  public void onComplete() {
    context.runOnContext(v -> {
      if (closePromise != null) {
        return;
      }
      closePromise = context.promise();
      executeBlocking(context, () -> closeQuietly(resultSetStatement)).otherwiseEmpty().onComplete(closePromise);
      OracleResponse<R> response = createResponse();
      queue = null;
      readPromise.complete(response);
    });
  }

  private OracleResponse<R> createResponse() {
    OracleResponse<R> response = new OracleResponse<>(-1);
    BiConsumer<C, Row> accumulator = collector.accumulator();
    C container = collector.supplier().get();
    int size = 0;
    Row row;
    while (size < fetchSize && (row = queue.poll()) != null) {
      size++;
      accumulator.accept(container, row);
    }
    response.push(collector.finisher().apply(container), description, size);
    return response;
  }

  @Override
  public Row apply(oracle.jdbc.OracleRow oracleRow) {
    try {
      return transform(classes, description, oracleRow);
    } catch (SQLException e) {
      throw new OracleException(e);
    }
  }

  private static Row transform(List<Class<?>> classes, RowDescriptorBase desc, oracle.jdbc.OracleRow or) throws SQLException {
    Row row = new OracleRow(desc);
    for (int i = 1; i <= desc.columnNames().size(); i++) {
      Object res = convertSqlValue(or.getObject(i, classes.get(i - 1)));
      row.addValue(res);
    }
    return row;
  }

  private static Class<?> getType(String cn) {
    try {
      return Class.forName(cn, true, RowReader.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public Future<Void> close() {
    Promise<Void> promise = context.owner().promise();
    context.runOnContext(v -> {
      if (closePromise != null) {
        closePromise.future().onComplete(promise);
        return;
      }
      closePromise = context.promise();
      closePromise.future().onComplete(promise);
      if (subscription != null) {
        subscription.cancel();
      }
      if (readPromise != null) {
        readPromise.fail("Subscription has been canceled");
      }
      executeBlocking(context, () -> closeQuietly(resultSetStatement)).otherwiseEmpty().onComplete(closePromise);
    });
    return promise.future();
  }

  public Future<Boolean> hasMore() {
    Promise<Boolean> promise = context.owner().promise();
    context.runOnContext(v -> {
      promise.complete(queue != null && !queue.isEmpty());
    });
    return promise.future();
  }
}

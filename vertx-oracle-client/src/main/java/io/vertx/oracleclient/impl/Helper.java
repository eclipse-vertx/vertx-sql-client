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
import oracle.jdbc.OraclePreparedStatement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.function.Supplier;

import static io.vertx.oracleclient.impl.FailureUtil.sanitize;

public class Helper {

  public static <T> Future<T> completeOrFail(ThrowingSupplier<T> supplier) {
    try {
      return Future.succeededFuture(supplier.getOrThrow());
    } catch (SQLException throwables) {
      return Future.failedFuture(throwables);
    }
  }

  public static void closeQuietly(Statement ps) {
    if (ps != null) {
      try {
        ps.close();
      } catch (SQLException throwables) {
        // ignore me.
      }
    }
  }

  public static <T> Future<T> contextualize(CompletionStage<T> stage, ContextInternal context) {
    Promise<T> future = context.promise();
    stage.whenComplete((r, f) -> {
      if (f != null) {
        future.fail(f);
      } else {
        future.complete(r);
      }
    });
    return future.future();
  }

  /**
   * Returns a {@code PreparedStatement}
   * {@linkplain Wrapper#unwrap(Class) unwrapped} as an
   * {@code OraclePreparedStatement}, or throws an {@code R2dbcException} if it
   * does not wrap or implement the Oracle JDBC interface.
   *
   * @param preparedStatement A JDBC prepared statement
   * @return An Oracle JDBC prepared statement
   * @throws VertxException If an Oracle JDBC prepared statement is not wrapped.
   */
  public static OraclePreparedStatement unwrapOraclePreparedStatement(
    PreparedStatement preparedStatement) {
    return getOrHandleSQLException(() ->
      preparedStatement.unwrap(OraclePreparedStatement.class));
  }

  /**
   * Returns the specified {@code supplier}'s output, or throws a
   * {@link VertxException} if the function throws a {@link SQLException}. This
   * method serves to improve code readability. For instance:
   * <pre>
   *   try {
   *     return resultSet.getMetaData();
   *   }
   *   catch (SQLException sqlException) {
   *     throw OracleR2dbcExceptions.toR2dbcException(sqlException);
   *   }
   * </pre>
   * Can be expressed more concisely as:
   * <pre>
   *   return getOrHandleSQLException(resultSet::getMetaData);
   * </pre>
   *
   * @param supplier Returns a value or throws a {@code SQLException}. Not
   *                 null.
   * @param <T>      The output type of the supplier
   * @return The output of the specified {@code supplier}.
   * @throws VertxException If the supplier throws a {@code SQLException}.
   */
  public static <T> T getOrHandleSQLException(ThrowingSupplier<T> supplier)
    throws VertxException {
    try {
      return supplier.getOrThrow();
    } catch (SQLException sqlException) {
      throw new VertxException(sqlException);
    }
  }

  public static void runOrHandleSQLException(ThrowingRunnable runnable)
    throws VertxException {
    try {
      runnable.runOrThrow();
    } catch (SQLException sqlException) {
      throw new VertxException(sqlException);
    }
  }

  public static <T> Future<T> first(Flow.Publisher<T> publisher, ContextInternal context) {
    Promise<T> promise = context.promise();
    publisher.subscribe(new Flow.Subscriber<>() {
      volatile Flow.Subscription subscription;

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
      }

      @Override
      public void onNext(T item) {
        context.runOnContext(x -> promise.tryComplete(item));
        subscription.cancel();
      }

      @Override
      public void onError(Throwable throwable) {
        promise.fail(sanitize(throwable));
      }

      @Override
      public void onComplete() {
        // Use tryComplete as the completion signal can be sent even if we cancelled.
        // Also for Publisher<Void> we would get in this case.
        promise.tryComplete(null);
      }
    });
    return promise.future();
  }

  public static <T> Future<List<T>> collect(Flow.Publisher<T> publisher, ContextInternal context) {
    Promise<List<T>> promise = context.promise();
    publisher.subscribe(new Flow.Subscriber<>() {
      final List<T> list = new ArrayList<>();

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(T item) {
        list.add(item);
      }

      @Override
      public void onError(Throwable throwable) {
        promise.fail(sanitize(throwable));
      }

      @Override
      public void onComplete() {
        promise.complete(list);
      }
    });
    return promise.future();
  }

  /**
   * <p>
   * Function type that returns a value or throws a {@link SQLException}. This
   * functional interface can reference JDBC methods that throw
   * {@code SQLExceptions}. The standard {@link Supplier} interface cannot
   * reference methods that throw checked exceptions.
   * </p>
   *
   * @param <T> the type of values supplied by this supplier.
   */
  @FunctionalInterface
  public interface ThrowingSupplier<T> extends Supplier<T> {
    /**
     * Returns a value, or throws a {@code SQLException} if an error is
     * encountered.
     *
     * @return the supplied value
     * @throws SQLException If a value is not returned due to an error.
     */
    T getOrThrow() throws SQLException;

    /**
     * Returns a value, or throws an {@code R2dbcException} if an error is
     * encountered.
     *
     * @throws VertxException If a value is not returned due to an error.
     * @implNote The default implementation invokes
     * {@link #getOrHandleSQLException(ThrowingSupplier)} (ThrowingRunnable)}
     * with this {@code ThrowingSupplier}.
     */
    @Override
    default T get() throws VertxException {
      return getOrHandleSQLException(this);
    }
  }

  /**
   * <p>
   * Function type that returns no value or throws a {@link SQLException}.
   * This functional interface can reference JDBC methods that throw
   * {@code SQLExceptions}. The standard {@link Runnable} interface cannot
   * reference methods that throw checked exceptions.
   * </p>
   */
  @FunctionalInterface
  public interface ThrowingRunnable extends Runnable {
    /**
     * Runs to completion and returns normally, or throws a {@code SQLException}
     * if an error is encountered.
     *
     * @throws SQLException If the run does not complete due to an error.
     */
    void runOrThrow() throws SQLException;

    /**
     * Runs to completion and returns normally, or throws an {@code
     * R2dbcException} if an error is encountered.
     *
     * @throws VertxException If the run does not complete due to an error.
     * @implNote The default implementation invokes
     * {@link #runOrHandleSQLException(ThrowingRunnable)} with this {@code
     * ThrowingRunnable}.
     */
    @Override
    default void run() throws VertxException {
      runOrHandleSQLException(this);
    }
  }

  /**
   * Accessor of column values within a single row from a table of data that
   * a {@link ResultSet} represents. Instances of {@code JdbcRow} are
   * supplied as input to row mapping functions, and each instance is valid
   * only within the scope of a row mapping function's call. Usage outside of
   * a row mapping function's scope results in an {@code IllegalStateException}.
   */
  interface JdbcRow {

    /**
     * Returns the value of this row for the specified {@code index} as
     * the specified {@code type}. The value is returned as if by invoking
     * {@link ResultSet#getObject(int, Class)} on a result set with a cursor
     * positioned on the table row that this object represents.
     *
     * @param index 0-based column index. (The first column's index is 0)
     * @param type  The type of object to return. Not null.
     * @param <T>   The returned type
     * @return The column value as the specified type.
     * @throws VertxException           If the {@code index} is invalid
     * @throws IllegalArgumentException If conversion to the specified {@code
     *                                  type} is not supported.
     * @throws IllegalStateException    If this method is invoked outside of a
     *                                  row mapping function.
     */
    <T> T getObject(int index, Class<T> type);

    /**
     * Returns a copy of this row. The copy returned by this method is not
     * backed by the resources of the JDBC connection that created this row.
     * The copy returned by this method allows the column values of this row
     * to be accessed after closing the JDBC connection that created this row.
     *
     * @return A cached copy of this row.
     */
    JdbcRow copy();
  }
}

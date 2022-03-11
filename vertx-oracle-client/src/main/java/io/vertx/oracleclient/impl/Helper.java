/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
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
import io.vertx.oracleclient.OracleException;
import io.vertx.sqlclient.Tuple;
import oracle.sql.TIMESTAMPTZ;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Supplier;

import static io.vertx.oracleclient.impl.FailureUtil.sanitize;

public class Helper {

  public static void closeQuietly(AutoCloseable autoCloseable) {
    if (autoCloseable != null) {
      try {
        autoCloseable.close();
      } catch (Exception ignore) {
      }
    }
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
      throw new OracleException(sqlException);
    }
  }

  public static void runOrHandleSQLException(ThrowingRunnable runnable)
    throws VertxException {
    try {
      runnable.runOrThrow();
    } catch (SQLException sqlException) {
      throw new OracleException(sqlException);
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

  public static Object convertSqlValue(Object value) throws SQLException {
    if (value == null) {
      return null;
    }

    if (value instanceof Boolean || value instanceof String || value instanceof byte[] || value instanceof Number) {
      return value;
    }

    // JDBC temporal values

    if (value instanceof Time) {
      return ((Time) value).toLocalTime();
    }

    if (value instanceof Date) {
      return ((Date) value).toLocalDate();
    }

    if (value instanceof Timestamp) {
      return ((Timestamp) value).toLocalDateTime();
    }

    if (value instanceof TIMESTAMPTZ) {
      return ((TIMESTAMPTZ) value).toZonedDateTime().toOffsetDateTime();
    }

    // large objects
    if (value instanceof Clob) {
      Clob c = (Clob) value;
      try {
        // result might be truncated due to downcasting to int
        return c.getSubString(1, (int) c.length());
      } finally {
        try {
          c.free();
        } catch (AbstractMethodError | SQLFeatureNotSupportedException e) {
          // ignore since it is an optional feature since 1.6 and non existing before 1.6
        }
      }
    }

    if (value instanceof Blob) {
      Blob b = (Blob) value;
      try {
        // result might be truncated due to downcasting to int
        return b.getBytes(1, (int) b.length());
      } finally {
        try {
          b.free();
        } catch (AbstractMethodError | SQLFeatureNotSupportedException e) {
          // ignore since it is an optional feature since 1.6 and non existing before 1.6
        }
      }
    }

    // arrays
    if (value instanceof Array) {
      Array a = (Array) value;
      try {
        Object arr = a.getArray();
        if (arr != null) {
          int len = java.lang.reflect.Array.getLength(arr);
          Object[] castedArray = new Object[len];
          for (int i = 0; i < len; i++) {
            castedArray[i] = convertSqlValue(java.lang.reflect.Array.get(arr, i));
          }
          return castedArray;
        }
      } finally {
        a.free();
      }
    }

    // RowId
    if (value instanceof RowId) {
      return ((RowId) value).getBytes();
    }

    // Struct
    if (value instanceof Struct) {
      return Tuple.of(((Struct) value).getAttributes());
    }

    // fallback to String
    return value.toString();
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

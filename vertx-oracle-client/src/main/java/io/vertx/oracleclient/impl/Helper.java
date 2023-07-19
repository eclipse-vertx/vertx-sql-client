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

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.oracleclient.OracleException;
import io.vertx.sqlclient.Tuple;
import oracle.sql.TIMESTAMPTZ;

import java.sql.*;
import java.util.function.Function;
import java.util.function.Supplier;

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

  public static Object convertSqlValue(Object value) throws SQLException {
    if (value == null) {
      return null;
    }

    if (value instanceof Boolean || value instanceof String || value instanceof Number) {
      return value;
    }

    if (value instanceof byte[]) {
      return Buffer.buffer((byte[]) value);
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
        return Buffer.buffer(b.getBytes(1, (int) b.length()));
      } finally {
        try {
          b.free();
        } catch (AbstractMethodError | SQLFeatureNotSupportedException ignore) {
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

  public static boolean isFatal(SQLException e) {
    int errorCode = e.getErrorCode();
    return errorCode == 28
      || errorCode == 600
      || errorCode == 1012
      || errorCode == 1014
      || errorCode == 1033
      || errorCode == 1034
      || errorCode == 1035
      || errorCode == 1089
      || errorCode == 1090
      || errorCode == 1092
      || errorCode == 1094
      || errorCode == 2396
      || errorCode == 3106
      || errorCode == 3111
      || errorCode == 3113
      || errorCode == 3114
      || (errorCode >= 12100 && errorCode <= 12299)
      || errorCode == 17002
      || errorCode == 17008
      || errorCode == 17410
      || errorCode == 17447
      || "08000".equals(e.getSQLState());
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

  @FunctionalInterface
  public interface SQLBlockingCodeHandler<T> extends java.util.concurrent.Callable<T> {

    T doHandle() throws SQLException;

    @Override
    default T call() {
      try {
        return doHandle();
      } catch (SQLException e) {
        throw new OracleException(e);
      }
    }
  }

  @FunctionalInterface
  public interface SQLBlockingTaskHandler extends java.util.concurrent.Callable<Void> {

    void doHandle() throws SQLException;

    @Override
    default Void call() throws Exception {
      try {
        doHandle();
        return null;
      } catch (SQLException e) {
        throw new OracleException(e);
      }
    }
  }

  @FunctionalInterface
  public interface SQLFutureMapper<T, U> extends Function<T, Future<U>> {

    Future<U> doApply(T t) throws SQLException;

    @Override
    default Future<U> apply(T t) {
      try {
        return doApply(t);
      } catch (SQLException e) {
        return Future.failedFuture(new OracleException(e));
      }
    }
  }

  public static <T> Future<T> executeBlocking(Context context, SQLBlockingCodeHandler<T> blockingCodeHandler) {
    return context.executeBlocking(blockingCodeHandler, false);
  }

  public static Future<Void> executeBlocking(Context context, SQLBlockingTaskHandler blockingTaskHandler) {
    return context.executeBlocking(blockingTaskHandler, false);
  }
}

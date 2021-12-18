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

package io.vertx.mssqlclient.data;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.mssqlclient.MSSQLTestBase;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.vertx.sqlclient.ColumnChecker.getByIndex;
import static io.vertx.sqlclient.ColumnChecker.getByName;

public abstract class MSSQLDataTypeTestBase extends MSSQLTestBase {
  Vertx vertx;
  MSSQLConnectOptions options;

  static {
    ColumnChecker.load(() -> {
      List<ColumnChecker.SerializableBiFunction<Tuple, Integer, ?>> tupleMethods = new ArrayList<>();
      tupleMethods.add(Tuple::getValue);

      tupleMethods.add(Tuple::getShort);
      tupleMethods.add(Tuple::getInteger);
      tupleMethods.add(Tuple::getLong);
      tupleMethods.add(Tuple::getFloat);
      tupleMethods.add(Tuple::getDouble);
      tupleMethods.add(Tuple::getBigDecimal);
      tupleMethods.add(Tuple::getString);
      tupleMethods.add(Tuple::getBoolean);
      tupleMethods.add(Tuple::getLocalDate);
      tupleMethods.add(Tuple::getLocalTime);
      tupleMethods.add(Tuple::getLocalDateTime);
      tupleMethods.add(Tuple::getUUID);

      tupleMethods.add(getByIndex(BigDecimal.class));
      return tupleMethods;
    }, () -> {
      List<ColumnChecker.SerializableBiFunction<Row, String, ?>> rowMethods = new ArrayList<>();
      rowMethods.add(Row::getValue);

      rowMethods.add(Row::getShort);
      rowMethods.add(Row::getInteger);
      rowMethods.add(Row::getLong);
      rowMethods.add(Row::getFloat);
      rowMethods.add(Row::getDouble);
      rowMethods.add(Row::getBigDecimal);
      rowMethods.add(Row::getString);
      rowMethods.add(Row::getBoolean);
      rowMethods.add(Row::getLocalDate);
      rowMethods.add(Row::getLocalTime);
      rowMethods.add(Row::getLocalDateTime);
      rowMethods.add(Row::getUUID);

      rowMethods.add(getByName(BigDecimal.class));

      return rowMethods;
    });
  }

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MSSQLConnectOptions(MSSQLTestBase.options);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  protected <T> void testQueryDecodeGenericWithoutTable(TestContext ctx,
                                                        String columnName,
                                                        String type,
                                                        String value,
                                                        Consumer<Row> checker) {
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT CAST(" + value + " AS " + type + ") AS " + columnName)
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          checker.accept(row);
          conn.close();
        }));
    }));
  }

  protected <T> void testQueryDecodeGenericWithoutTable(TestContext ctx,
                                                        String columnName,
                                                        String type,
                                                        String value,
                                                        T expected) {
    testQueryDecodeGenericWithoutTable(ctx, columnName, type, value, row -> {
      ctx.assertEquals(expected, row.getValue(0));
      ctx.assertEquals(expected, row.getValue(columnName));
    });
  }

  protected <T> void testPreparedQueryDecodeGenericWithoutTable(TestContext ctx,
                                                                String columnName,
                                                                String type,
                                                                String value,
                                                                Consumer<Row> checker) {
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT CAST(" + value + " AS " + type + ") AS " + columnName)
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          checker.accept(row);
          conn.close();
        }));
    }));
  }

  protected <T> void testPreparedQueryDecodeGenericWithoutTable(TestContext ctx,
                                                                String columnName,
                                                                String type,
                                                                String value,
                                                                T expected) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, columnName, type, value, row -> {
      ctx.assertEquals(expected, row.getValue(0));
      ctx.assertEquals(expected, row.getValue(columnName));
    });
  }

  protected <T> void testQueryDecodeGeneric(TestContext ctx,
                                            String tableName,
                                            String columnName,
                                            String rowIdentifier,
                                            Consumer<Row> checker) {
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query(String.format("SELECT %s FROM %s WHERE id = %s", columnName, tableName, rowIdentifier))
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          checker.accept(row);
          conn.close();
        }));
    }));
  }


  protected <T> void testPreparedQueryDecodeGeneric(TestContext ctx,
                                                    String tableName,
                                                    String columnName,
                                                    String rowIdentifier,
                                                    Consumer<Row> checker) {
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(String.format("SELECT %s FROM %s WHERE id = %s", columnName, tableName, rowIdentifier))
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          checker.accept(row);
          conn.close();
        }));
    }));
  }

  protected <T> void testPreparedQueryEncodeGeneric(TestContext ctx,
                                                    String tableName,
                                                    String columnName,
                                                    T param,
                                                    Consumer<Row> checker) {
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(String.format("UPDATE %s SET %s = @p1 WHERE id = 2", tableName, columnName))
        .execute(Tuple.of(param), ctx.asyncAssertSuccess(updateRes -> {
          conn.preparedQuery(String.format("SELECT %s FROM %s WHERE id = 2", columnName, tableName))
            .execute(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              Row row = result.iterator().next();
              checker.accept(row);
              conn.close();
            }));
          }));
    }));
  }
}

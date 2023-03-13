/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.data;

import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.MySQLTestBase;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;

public abstract class MySQLDataTypeTestBase extends MySQLTestBase {
  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  protected <T> void testTextDecodeGenericWithTable(TestContext ctx,
                                                    String columnName,
                                                    T expected) {
    testTextDecodeGenericWithTable(ctx, columnName, (row, cn) -> {
      ctx.assertEquals(expected, row.getValue(0));
      ctx.assertEquals(expected, row.getValue(cn));
    });
  }

  protected <T> void testTextDecodeGenericWithTable(TestContext ctx,
                                                    String columnName,
                                                    BiConsumer<Row, String> expected) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT `" + columnName + "` FROM datatype WHERE id = 1")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        expected.accept(row, columnName);
        conn.close();
      }));
    }));
  }

  protected <T> void testBinaryDecodeGenericWithTable(TestContext ctx,
                                                      String columnName,
                                                      T expected) {
    testBinaryDecodeGenericWithTable(ctx, columnName, (row, cn) -> {
      ctx.assertEquals(expected, row.getValue(0));
      ctx.assertEquals(expected, row.getValue(cn));
    });
  }

  protected <T> void testBinaryDecodeGenericWithTable(TestContext ctx,
                                                      String columnName,
                                                      BiConsumer<Row, String> expected) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT `" + columnName + "` FROM datatype WHERE id = 1")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        expected.accept(row, columnName);
        conn.close();
      }));
    }));
  }

  protected void testBinaryEncodeGeneric(TestContext ctx,
                                             String columnName,
                                             Object param,
                                             BiConsumer<Row, String> valueAccessor) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("UPDATE datatype SET `" + columnName + "` = ?" + " WHERE id = 2")
        .execute(Tuple.tuple().addValue(param))
        .onComplete(ctx.asyncAssertSuccess(updateResult -> {
        conn
          .preparedQuery("SELECT `" + columnName + "` FROM datatype WHERE id = 2")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          valueAccessor.accept(row, columnName);
          conn.close();
        }));
      }));
    }));
  }

  protected <T> void testBinaryEncodeGeneric(TestContext ctx,
                                             String columnName,
                                             T expected) {
    testBinaryEncodeGeneric(ctx, columnName, expected, (row, cName) -> {
      ctx.assertEquals(expected, row.getValue(0));
      ctx.assertEquals(expected, row.getValue(columnName));
    });
  }

  protected void testBinaryDecode(TestContext ctx, String sql, Tuple params, Consumer<RowSet<Row>> checker) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(sql)
        .execute(params)
        .onComplete(ctx.asyncAssertSuccess(result -> {
        checker.accept(result);
        conn.close();
      }));
    }));
  }

  protected void testBinaryDecode(TestContext ctx, String sql, Consumer<RowSet<Row>> checker) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(sql)
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        checker.accept(result);
        conn.close();
      }));
    }));
  }

  protected void testTextDecode(TestContext ctx, String sql, Consumer<RowSet<Row>> checker) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn
        .query(sql)
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        checker.accept(result);
        conn.close();
      }));
    }));
  }
}

/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLEnumDataTypeTest extends MSSQLDataTypeTestBase {
  @Test
  public void testQueryDecodeStringToJavaEnum(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_enum", "varchar", "'large'", row -> {
      ColumnChecker.checkColumn(0, "test_enum")
        .returns(Tuple::getValue, Row::getValue, "large")
        .returns(Tuple::getString, Row::getString, "large")
        .returns(String.class, "large")
        .returns(Size.class, Size.large)
        .forRow(row);
    });
  }

  @Test
  public void testPreparedQueryDecodeStringToJavaEnum(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_enum", "varchar", "'large'", row -> {
      ColumnChecker.checkColumn(0, "test_enum")
        .returns(Tuple::getValue, Row::getValue, "large")
        .returns(Tuple::getString, Row::getString, "large")
        .returns(String.class, "large")
        .returns(Size.class, Size.large)
        .forRow(row);
    });
  }

  @Test
  public void testPreparedQueryEncodeJavaEnumToString(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_varchar", Size.medium, row -> {
      ColumnChecker.checkColumn(0, "test_varchar")
        .returns(Tuple::getValue, Row::getValue, "medium")
        .returns(Tuple::getString, Row::getString, "medium")
        .returns(String.class, "medium")
        .returns(Size.class, Size.medium)
        .forRow(row);
    });
  }

  private enum Size {
    x_small, small, medium, large, x_large;
  }
}

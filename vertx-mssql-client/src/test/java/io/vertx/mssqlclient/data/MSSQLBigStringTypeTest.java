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

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLBigStringTypeTest extends MSSQLDataTypeTestBase {

  @Test
  public void testPreparedQueryEncode(TestContext ctx) {
    StringBuilder sb = new StringBuilder(10000);
    while (sb.length() < 10000) {
      sb.append("ae $ € iou y éè %û* <> '");
    }
    String bigString = sb.toString();

    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_varchar_max", bigString, row -> {
      ColumnChecker.checkColumn(0, "test_varchar_max")
        .returns(Tuple::getValue, Row::getValue, bigString)
        .returns(Tuple::getString, Row::getString, bigString)
        .returns(String.class, bigString)
        .forRow(row);
    });
  }
}

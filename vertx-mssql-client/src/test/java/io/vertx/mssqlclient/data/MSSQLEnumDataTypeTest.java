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
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLEnumDataTypeTest extends MSSQLDataTypeTestBase {
  @Test
  public void testQueryDecodeStringToJavaEnum(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_enum", "varchar", "'large'", row -> {
      ctx.assertEquals(Size.large, row.get(Size.class, 0));
      ctx.assertEquals(Size.large, row.get(Size.class, "test_enum"));
      ctx.assertEquals("large", row.get(String.class, 0));
      ctx.assertEquals("large", row.get(String.class, "test_enum"));
      ctx.assertEquals("large", row.getString(0));
      ctx.assertEquals("large", row.getString("test_enum"));
    });
  }

  @Test
  public void testPreparedQueryDecodeStringToJavaEnum(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_enum", "varchar", "'large'", row -> {
      ctx.assertEquals(Size.large, row.get(Size.class, 0));
      ctx.assertEquals(Size.large, row.get(Size.class, "test_enum"));
      ctx.assertEquals("large", row.get(String.class, 0));
      ctx.assertEquals("large", row.get(String.class, "test_enum"));
      ctx.assertEquals("large", row.getString(0));
      ctx.assertEquals("large", row.getString("test_enum"));
    });
  }

  @Test
  public void testPreparedQueryEncodeJavaEnumToString(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_varchar", Size.medium, row -> {
      ctx.assertEquals(Size.medium, row.get(Size.class, 0));
      ctx.assertEquals(Size.medium, row.get(Size.class, "test_varchar"));
      ctx.assertEquals("medium", row.get(String.class, 0));
      ctx.assertEquals("medium", row.get(String.class, "test_varchar"));
      ctx.assertEquals("medium", row.getString(0));
      ctx.assertEquals("medium", row.getString("test_varchar"));
    });
  }

  private enum Size {
    x_small, small, medium, large, x_large;
  }
}

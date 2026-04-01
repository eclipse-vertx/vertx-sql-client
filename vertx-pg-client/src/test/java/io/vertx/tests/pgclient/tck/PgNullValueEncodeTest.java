/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.tests.pgclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.tests.pgclient.junit.ContainerPgRule;
import io.vertx.tests.sqlclient.tck.NullValueEncodeTestBase;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgNullValueEncodeTest extends NullValueEncodeTestBase {

  @ClassRule
  public static final ContainerPgRule rule = ContainerPgRule.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Test
  @Override
  public void testEncodeNullBigDecimal(TestContext ctx) {
    Assume.assumeTrue("PostgreSQL 9 cannot determine data type for NULL BigDecimal", ContainerPgRule.isAtLeastPg10());
    super.testEncodeNullBigDecimal(ctx);
  }

  @Test
  @Override
  public void testEncodeNullLocalTime(TestContext ctx) {
    Assume.assumeTrue("PostgreSQL 9 cannot determine data type for NULL LocalTime", ContainerPgRule.isAtLeastPg10());
    super.testEncodeNullLocalTime(ctx);
  }

  @Test
  @Override
  public void testEncodeNullOffsetTime(TestContext ctx) {
    Assume.assumeTrue("PostgreSQL 9 cannot determine data type for NULL OffsetTime", ContainerPgRule.isAtLeastPg10());
    super.testEncodeNullOffsetTime(ctx);
  }

  @Test
  @Override
  public void testEncodeNullTemporal(TestContext ctx) {
    Assume.assumeTrue("PostgreSQL 9 cannot determine data type for NULL Temporal", ContainerPgRule.isAtLeastPg10());
    super.testEncodeNullTemporal(ctx);
  }

  @Test
  @Override
  public void testEncodeNullArrayOfBigDecimal(TestContext ctx) {
    Assume.assumeTrue("PostgreSQL 9 cannot determine data type for NULL BigDecimal[]", ContainerPgRule.isAtLeastPg10());
    super.testEncodeNullArrayOfBigDecimal(ctx);
  }

  @Test
  @Override
  public void testEncodeNullArrayOfLocalTime(TestContext ctx) {
    Assume.assumeTrue("PostgreSQL 9 cannot determine data type for NULL LocalTime[]", ContainerPgRule.isAtLeastPg10());
    super.testEncodeNullArrayOfLocalTime(ctx);
  }

  @Test
  @Override
  public void testEncodeNullArrayOfOffsetTime(TestContext ctx) {
    Assume.assumeTrue("PostgreSQL 9 cannot determine data type for NULL OffsetTime[]", ContainerPgRule.isAtLeastPg10());
    super.testEncodeNullArrayOfOffsetTime(ctx);
  }

  @Test
  @Override
  public void testEncodeNullArrayOfTemporal(TestContext ctx) {
    Assume.assumeTrue("PostgreSQL 9 cannot determine data type for NULL Temporal[]", ContainerPgRule.isAtLeastPg10());
    super.testEncodeNullArrayOfTemporal(ctx);
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("$").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }
}

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

package io.vertx.mssqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mssqlclient.junit.MSSQLRule;
import io.vertx.sqlclient.tck.NullValueEncodeTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLNullValueEncodeTest extends NullValueEncodeTestBase {
  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("@p").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfOffsetDateTime(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfBoolean(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfLocalDateTime(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfBuffer(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfDouble(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullJsonArray(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfOffsetTime(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfString(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfBigDecimal(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfLong(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfUUID(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfFloat(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfShort(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfJsonObject(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullTemporal(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfInteger(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfLocalDate(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfLocalTime(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfJsonArray(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullOffsetTime(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullArrayOfTemporal(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullUUID(TestContext ctx) {
  }

  @Test
  @Ignore
  @Override
  public void testEncodeNullJsonObject(TestContext ctx) {
  }
}

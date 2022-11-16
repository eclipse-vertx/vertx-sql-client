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

package io.vertx.mysqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.junit.ProxySQLRule;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ProxySQLPreparedQueryTest extends MySQLPreparedQueryTest {

  @ClassRule
  public static ProxySQLRule proxySql = new ProxySQLRule(rule);

  @Override
  protected void initConnector() {
    options = proxySql.options(rule.options());
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testQueryCursor(TestContext ctx) {
    super.testQueryCursor(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testQueryCloseCursor(TestContext ctx) {
    super.testQueryCloseCursor(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testQueryStreamCloseCursor(TestContext ctx) {
    super.testQueryStreamCloseCursor(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testStreamQuery(TestContext ctx) {
    super.testStreamQuery(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testStreamQueryPauseInBatch(TestContext ctx) {
    super.testStreamQueryPauseInBatch(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testStreamQueryPauseInBatchFromAnotherThread(TestContext ctx) {
    super.testStreamQueryPauseInBatchFromAnotherThread(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testStreamQueryPauseResume(TestContext ctx) {
    super.testStreamQueryPauseResume(ctx);
  }
}

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
package io.vertx.tests.db2client.tck;

import io.vertx.db2client.DB2Builder;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.tests.db2client.junit.DB2Resource;
import io.vertx.tests.sqlclient.tck.TransactionTestBase;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(VertxUnitRunner.class)
public class DB2TransactionTest extends TransactionTestBase {

  private static final Logger logger = LoggerFactory.getLogger(DB2TransactionTest.class);

  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Rule
  public TestName testName = new TestName();

  @Before
  public void printTestName(TestContext ctx) throws Exception {
    logger.info(">>> BEGIN {}.{}", getClass().getSimpleName(), testName.getMethodName());
  }

  @Override
  protected Pool createPool() {
    return DB2Builder.pool()
      .with(new PoolOptions().setMaxSize(1))
      .connectingTo(new DB2ConnectOptions(rule.options()))
      .using(vertx)
      .build();
  }

  @Override
  protected Pool nonTxPool() {
    return DB2Builder.pool()
      .with(new PoolOptions().setMaxSize(1))
      .connectingTo(new DB2ConnectOptions(rule.options()))
      .using(vertx)
      .build();
  }

  @Override
  protected void cleanTestTable(TestContext ctx) {
    // use DELETE FROM because DB2 does not support TRUNCATE TABLE
    getPool()
      .query("DELETE FROM mutable")
      .execute()
      .onComplete(ctx.asyncAssertSuccess());
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }
}

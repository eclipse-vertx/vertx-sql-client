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
package io.vertx.tests.mssqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mssqlclient.MSSQLBuilder;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.tests.mssqlclient.junit.MSSQLRule;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.tests.sqlclient.tck.TransactionTestBase;
import org.junit.AssumptionViolatedException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLTransactionTest extends TransactionTestBase {

  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;

  @Override
  protected Pool createPool() {
    return MSSQLBuilder.pool(builder -> builder.with(new PoolOptions().setMaxSize(1)).connectingTo(new MSSQLConnectOptions(rule.options())).using(vertx));
  }

  @Override
  protected Pool nonTxPool() {
    return MSSQLBuilder.pool(builder -> builder.with(new PoolOptions().setMaxSize(1)).connectingTo(new MSSQLConnectOptions(rule.options())).using(vertx));
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
  public void testDelayedCommit(TestContext ctx) {
    throw new AssumptionViolatedException("MSSQL holds write locks on inserted row with isolation level = 2");
  }
}

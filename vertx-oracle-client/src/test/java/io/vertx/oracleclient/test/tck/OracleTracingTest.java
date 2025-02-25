/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient.test.tck;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleBuilder;
import io.vertx.oracleclient.test.junit.OracleRule;
import io.vertx.sqlclient.Pool;
import io.vertx.tests.sqlclient.tck.TracingTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class OracleTracingTest extends TracingTestBase {

  @ClassRule
  public static OracleRule rule = OracleRule.SHARED_INSTANCE;

  @Override
  protected Pool createPool(Vertx vertx) {
    return OracleBuilder.pool(builder -> builder
      .connectingTo(rule.options())
      .using(vertx));
  }

  @Override
  protected String statement(String... parts) {
    return String.join(" ?", parts);
  }

  @Test
  @Ignore("Oracle does not support batched SELECT")
  @Override
  public void testTraceBatchQuery(TestContext ctx) {
  }

  @Test
  @Ignore("Oracle does not support batched SELECT")
  @Override
  public void testTracePooledBatchQuery(TestContext ctx) {
  }

  @Override
  protected boolean isValidDbSystem(String dbSystem) {
    return "oracle".equals(dbSystem);
  }
}

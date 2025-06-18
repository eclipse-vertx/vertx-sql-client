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

package tests.oracleclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.oracleclient.OracleBuilder;
import io.vertx.sqlclient.ClientBuilder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.tests.sqlclient.tck.MetricsTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import tests.oracleclient.junit.OracleRule;

public class OracleMetricsTest extends MetricsTestBase {

  @ClassRule
  public static OracleRule rule = OracleRule.SHARED_INSTANCE;

  @Override
  protected SqlConnectOptions connectOptions() {
    return rule.options();
  }

  @Override
  protected ClientBuilder<Pool> poolBuilder() {
    return OracleBuilder.pool();
  }

  @Override
  protected String statement(String... parts) {
    return String.join(" ?", parts);
  }

  @Test
  @Ignore("Oracle does not support batched SELECT")
  @Override
  public void testPreparedBatchQuery(TestContext ctx) {
    super.testPreparedBatchQuery(ctx);
  }

  @Test
  @Ignore("Oracle does not support batched SELECT")
  @Override
  public void testPrepareAndBatchQuery(TestContext ctx) {
    super.testPrepareAndBatchQuery(ctx);
  }

  @Test
  @Ignore("Implementation of the test does not work with Oracle")
  @Override
  public void testConnectionLost(TestContext ctx) throws Exception {
    super.testConnectionLost(ctx);
  }
}

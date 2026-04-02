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

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.tests.db2client.junit.DB2Resource;
import io.vertx.tests.sqlclient.tck.CollectorTestBase;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(VertxUnitRunner.class)
public class DB2CollectorTest extends CollectorTestBase {

  private static final Logger logger = LoggerFactory.getLogger(DB2CollectorTest.class);
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Rule
  public TestName testName = new TestName();

  @Before
  public void printTestName(TestContext ctx) throws Exception {
    logger.info(">>> BEGIN {}.{}", getClass().getSimpleName(), testName.getMethodName());
  }

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }
}

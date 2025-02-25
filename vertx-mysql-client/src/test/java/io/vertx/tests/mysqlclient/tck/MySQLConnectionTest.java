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

package io.vertx.tests.mysqlclient.tck;

import io.vertx.tests.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.tests.sqlclient.tck.ConnectionTestBase;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLConnectionTest extends ConnectionTestBase {
  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    options = rule.options();
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  @Override
  public void tearDown(TestContext ctx) {
    connector.close();
    super.tearDown(ctx);
  }

  @Override
  protected void validateDatabaseMetaData(TestContext ctx, DatabaseMetadata md) {
    if (rule.isUsingMariaDB()) {
      ctx.assertEquals("MariaDB", md.productName());
      ctx.assertTrue(md.majorVersion() >= 10, "Expected DB major version >= 10 but was " + md.majorVersion());
    } else if (rule.isUsingMySQL5_6()) {
      ctx.assertEquals("MySQL", md.productName());
      ctx.assertEquals(5, md.majorVersion());
      ctx.assertEquals(6, md.minorVersion());
    }
    else if (rule.isUsingMySQL8()) {
      ctx.assertEquals("MySQL", md.productName());
      ctx.assertEquals(8, md.majorVersion());
      ctx.assertEquals(0, md.minorVersion());
    }
    else {
      ctx.assertFalse(md.fullVersion().isEmpty());
    }
  }
}

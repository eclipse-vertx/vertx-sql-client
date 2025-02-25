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

package tests.oracleclient;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleBuilder;
import tests.oracleclient.junit.OracleRule;
import io.vertx.sqlclient.Pool;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class OracleColumnDescriptorTest extends OracleTestBase {

  @ClassRule
  public static OracleRule oracle = OracleRule.SHARED_INSTANCE;

  Pool pool;

  @Before
  public void setUp() throws Exception {
    pool = OracleBuilder.pool(builder -> builder
      .connectingTo(oracle.options())
      .using(vertx));
  }

  @Test
  public void testMetadata(TestContext ctx) {
    pool
      .withConnection(conn -> conn
        .query("SELECT id, val FROM mutable")
        .execute())
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertNotNull(rows.columnDescriptors());
        ctx.assertNotNull(rows.columnsNames());
        ctx.assertEquals("ID", rows.columnsNames().get(0));
        ctx.assertEquals("VAL", rows.columnsNames().get(1));
        ctx.assertEquals(2, rows.columnDescriptors().size());
        ctx.assertEquals("NUMBER", rows.columnDescriptors().get(0).typeName());
        ctx.assertEquals("VARCHAR2", rows.columnDescriptors().get(1).typeName());
      }));
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
    pool.close().onComplete(ctx.asyncAssertSuccess());
  }
}

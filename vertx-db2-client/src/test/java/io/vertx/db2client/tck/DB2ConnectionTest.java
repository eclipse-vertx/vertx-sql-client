/*
 * Copyright (C) 2019,2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.db2client.tck;

import static io.vertx.db2client.junit.TestUtil.assertContains;

import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.tck.ConnectionTestBase;
import io.vertx.db2client.DB2Exception;
import io.vertx.db2client.impl.drda.SQLState;
import io.vertx.db2client.impl.drda.SqlCode;
import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class DB2ConnectionTest extends ConnectionTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Rule
  public TestName testName = new TestName();

  @Before
  public void printTestName(TestContext ctx) throws Exception {
    System.out.println(">>> BEGIN " + getClass().getSimpleName() + "." + testName.getMethodName());
  }

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

  /**
   * Override this this test so we can test for specific parts of the exception.
   * There can be two potential error paths:
   * 1. If it is the first request to an invalid DB name, we will get the RDB_NOT_FOUND code
   * and a more detailed error message.
   * 2. If it is beyond the first failed request for an invalid DB name, the DB server will
   * just hang up the connection, and the client has to infer CONNECTION_REFUSED but cannot
   * accurately provide more details than that
   */
  @Test
  @Override
  public void testConnectInvalidDatabase(TestContext ctx) {
    options.setDatabase("bogusdb");
    connect(ctx.asyncAssertFailure(err -> {
      ctx.assertTrue(err instanceof DB2Exception);
      DB2Exception ex = (DB2Exception) err;
      assertContains(ctx, ex.getMessage(), "bogusdb", "The connection was closed by the database server");
      assertContains(ctx, ex.getSqlState(),
          SQLState.NET_DATABASE_NOT_FOUND,
          SQLState.AUTH_DATABASE_CONNECTION_REFUSED);
      ctx.assertTrue(ex.getErrorCode() == SqlCode.RDB_NOT_FOUND ||
                 ex.getErrorCode() == SqlCode.CONNECTION_REFUSED,
                 "Unexpected error code: " + ex.getErrorCode());
    }));
  }

  @Override
  protected void validateDatabaseMetaData(TestContext ctx, DatabaseMetadata md) {
    ctx.assertEquals(11, md.majorVersion());
    ctx.assertEquals(5, md.minorVersion());
    ctx.assertTrue(md.productName().contains("DB2"));
  }
}

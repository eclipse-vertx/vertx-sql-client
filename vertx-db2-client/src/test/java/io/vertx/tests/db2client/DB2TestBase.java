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
package io.vertx.tests.db2client;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.tests.db2client.junit.DB2Resource;
import io.vertx.tests.db2client.tck.ClientConfig;
import io.vertx.tests.sqlclient.tck.Connector;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public abstract class DB2TestBase {

  private static final Logger logger = LoggerFactory.getLogger(DB2TestBase.class);

  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  protected Vertx vertx;
  protected Connector<SqlConnection> connector;
  protected DB2ConnectOptions options;

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp(TestContext ctx) throws Exception {
    logger.info(">>> BEGIN {}", testName.getMethodName());
    vertx = Vertx.vertx();
    initConnector();
    for (String table : tablesToClean())
      cleanTestTable(ctx, table);
  }

  @After
  public void tearDown(TestContext ctx) {
    connector.close();
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  protected void initConnector() {
    options = rule.options();
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  protected void connect(Handler<AsyncResult<SqlConnection>> handler) {
    connector.connect(handler);
  }

  protected void cleanTestTable(TestContext ctx, String table) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("DELETE FROM " + table)
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        conn.close();
      }));
    }));
  }

  protected List<String> tablesToClean() {
    return Collections.emptyList();
  }

}

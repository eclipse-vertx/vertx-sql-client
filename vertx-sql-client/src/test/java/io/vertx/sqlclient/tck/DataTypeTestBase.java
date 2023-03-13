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

package io.vertx.sqlclient.tck;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import org.junit.After;
import org.junit.Before;

import java.sql.JDBCType;

public abstract class DataTypeTestBase {

  protected Vertx vertx;
  protected Connector<SqlConnection> connector;

  protected abstract JDBCType getNumericJDBCType();

  protected abstract Class<? extends Number> getNumericClass();

  protected abstract Number getNumericValue(Number value);

  protected abstract Number getNumericValue(String value);

  protected abstract void initConnector();

  protected void connect(Handler<AsyncResult<SqlConnection>> handler) {
    connector.connect(handler);
  }

  @Before
  public void setUp(TestContext ctx) throws Exception {
    vertx = Vertx.vertx();
    initConnector();
  }

  @After
  public void tearDown(TestContext ctx) {
    connector.close();
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  protected void verifyTypeName(TestContext ctx, ColumnDescriptor columnDescriptor) {
    ctx.assertNotNull(columnDescriptor);
  }
}

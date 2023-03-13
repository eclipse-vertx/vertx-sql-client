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

package io.vertx.mysqlclient;

import io.vertx.ext.unit.TestContext;
import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.SqlClient;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public abstract class MySQLTestBase {

  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

  protected static MySQLConnectOptions options;

  @BeforeClass
  public static void before() {
    options = rule.options();
  }

  static void deleteFromMutableTable(TestContext ctx, SqlClient client, Runnable completionHandler) {
    client
      .query("TRUNCATE TABLE mutable")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(result -> completionHandler.run()));
  }
}

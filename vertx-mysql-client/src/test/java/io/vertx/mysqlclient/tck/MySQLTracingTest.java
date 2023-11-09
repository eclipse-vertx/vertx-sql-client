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

package io.vertx.mysqlclient.tck;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.tck.TracingTestBase;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLTracingTest extends TracingTestBase {

  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

  @Override
  protected Pool createPool(Vertx vertx) {
    return MySQLBuilder.pool(builder -> builder.connectingTo(rule.options()).using(vertx));
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Override
  protected boolean isValidDbSystem(String dbSystem) {
    return "mysql".equals(dbSystem) || "mariadb".equals(dbSystem);
  }
}

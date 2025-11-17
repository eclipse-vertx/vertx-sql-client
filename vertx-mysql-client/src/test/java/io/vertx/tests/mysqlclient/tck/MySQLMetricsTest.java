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

package io.vertx.tests.mysqlclient.tck;

import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.sqlclient.ClientBuilder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.tests.mysqlclient.junit.MySQLRule;
import io.vertx.tests.sqlclient.tck.MetricsTestBase;
import org.junit.ClassRule;

public class MySQLMetricsTest extends MetricsTestBase {

  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

  @Override
  protected SqlConnectOptions connectOptions() {
    return rule.options();
  }

  @Override
  protected ClientBuilder<Pool> poolBuilder() {
    return MySQLBuilder.pool();
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }
}

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

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.tests.mysqlclient.junit.ProxySQLRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ProxySQLPreparedBatchTest extends MySQLPreparedBatchTest {

  @ClassRule
  public static ProxySQLRule proxySql = new ProxySQLRule(rule);

  @Override
  protected void initConnector() {
    MySQLConnectOptions options = proxySql.options(rule.options());
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }
}

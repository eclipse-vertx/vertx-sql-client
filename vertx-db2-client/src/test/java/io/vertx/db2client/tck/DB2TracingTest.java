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

package io.vertx.db2client.tck;

import io.vertx.core.Vertx;
import io.vertx.db2client.DB2Pool;
import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.tck.TracingTestBase;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class DB2TracingTest extends TracingTestBase {

  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Override
  protected Pool createPool(Vertx vertx) {
    return DB2Pool.pool(vertx, rule.options(), new PoolOptions());
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }
}

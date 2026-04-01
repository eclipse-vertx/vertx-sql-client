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

package io.vertx.tests.pgclient;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.vertx.tests.pgclient.junit.ContainerPgRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

@RunWith(VertxUnitRunner.class)
public abstract class PgTestBase {

  protected static final String ERRCODE_QUERY_CANCELED = "57014";

  @ClassRule
  public static final ContainerPgRule rule = ContainerPgRule.SHARED_INSTANCE;

  protected PgConnectOptions options;
  protected PoolOptions poolOptions;

  public void setup() throws Exception {
    options = rule.options();
    poolOptions = rule.poolOptions();
  }

  static void deleteFromTestTable(TestContext ctx, SqlClient client, Runnable completionHandler) {
    client
      .query("DELETE FROM Test")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(result -> completionHandler.run()));
  }

  static void insertIntoTestTable(TestContext ctx, SqlClient client, int amount, Runnable completionHandler) {
    AtomicInteger count = new AtomicInteger();
    for (int i = 0;i < 10;i++) {
      client
        .query("INSERT INTO Test (id, val) VALUES (" + i + ", 'Whatever-" + i + "')")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(r1 -> {
        ctx.assertEquals(1, r1.rowCount());
        if (count.incrementAndGet() == amount) {
          completionHandler.run();
        }
      }));
    }
  }

  /**
   * @return whether throwable is a PgException with the SQLSTATE code
   */
  static boolean hasSqlstateCode(Throwable throwable, String code) {
    return throwable instanceof PgException &&
      code.equals(((PgException) throwable).getSqlState());
  }
}

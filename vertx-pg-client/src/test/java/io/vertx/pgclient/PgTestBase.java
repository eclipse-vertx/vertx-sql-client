/*
 * Copyright (C) 2017 Julien Viet
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
 *
 */

package io.vertx.pgclient;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.ClassRule;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.PgRule;
import io.vertx.sqlclient.SqlClient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

@RunWith(VertxUnitRunner.class)
public abstract class PgTestBase {

  @ClassRule
  public static PgRule rule = PgRule.SHARED_INSTANCE;

  protected PgConnectOptions options;

  public void setup() throws Exception {
    options = rule.options();
  }

  static void deleteFromTestTable(TestContext ctx, SqlClient client, Runnable completionHandler) {
    client.query(
      "DELETE FROM Test",
      ctx.asyncAssertSuccess(result -> completionHandler.run()));
  }

  static void insertIntoTestTable(TestContext ctx, SqlClient client, int amount, Runnable completionHandler) {
    AtomicInteger count = new AtomicInteger();
    for (int i = 0;i < 10;i++) {
      client.query("INSERT INTO Test (id, val) VALUES (" + i + ", 'Whatever-" + i + "')", ctx.asyncAssertSuccess(r1 -> {
        ctx.assertEquals(1, r1.rowCount());
        if (count.incrementAndGet() == amount) {
          completionHandler.run();
        }
      }));
    }
  }


}

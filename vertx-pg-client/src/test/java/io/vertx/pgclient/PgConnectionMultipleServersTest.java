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

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.PoolOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(VertxUnitRunner.class)
public class PgConnectionMultipleServersTest {

  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule().setServerCount(3);

  protected Vertx vertx;
  protected PgConnectOptions options;
  protected PoolOptions poolOptions;
  private PgPool pool;

  @Before
  public void setup() throws Exception {
    options = rule.options();
    poolOptions = rule.poolOptions();
    vertx = Vertx.vertx();
    pool = PgPool.pool(vertx, options, poolOptions);
  }

  @After
  public void tearDown(TestContext ctx) {
    Async async = ctx.async();
    pool.close(ar -> {
      async.countDown();
    });
    async.awaitSuccess(20_000);
    vertx.close(ctx.asyncAssertSuccess());
  }


  @Test
  public void testDifferentConnectionsConnectToDifferentHosts(TestContext ctx) {
    Async async = ctx.async(4);
    String sql = "SELECT id, randomnumber FROM WORLD WHERE id = 1";
    pool.query(sql).execute(res -> {
        ctx.put("first_conn", res.result().iterator().next().getValue("randomnumber"));
        async.countDown();
      });
      pool.query(sql).execute(res -> {
        ctx.put("second_conn", res.result().iterator().next().getValue("randomnumber"));
        async.countDown();
      });
      pool.query(sql).execute(res -> {
        ctx.put("third_conn", res.result().iterator().next().getValue("randomnumber"));
        async.countDown();
      });
      pool.query(sql).execute(res -> {
        ctx.put("fourth_conn", res.result().iterator().next().getValue("randomnumber"));
        async.countDown();
      });
    async.handler(ar -> {
      ctx.assertNotEquals(ctx.get("first_conn"), ctx.get("second_conn"));
      ctx.assertNotEquals(ctx.get("third_conn"), ctx.get("second_conn"));
      ctx.assertNotEquals(ctx.get("third_conn"), ctx.get("first_conn"));
      ctx.assertEquals(ctx.get("first_conn"), ctx.get("fourth_conn"));
    });
  }
}

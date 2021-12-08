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

package io.vertx.pgclient;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.PoolOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(VertxUnitRunner.class)
public class SharedPoolTest extends PgTestBase {

  private static final String COUNT_CONNECTIONS_QUERY = "SELECT count(*) FROM pg_stat_activity WHERE application_name LIKE '%vertx%'";

  Vertx vertx;

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testUseSamePool(TestContext ctx) {
    int maxSize = 8;
    int instances = maxSize * 4;
    vertx.deployVerticle(() -> new AbstractVerticle() {
      @Override
      public void start() {
        PgPool pool = PgPool.pool(vertx, options, new PoolOptions().setMaxSize(maxSize).setShared(true));
        pool
          .query("SELECT pg_sleep(0.5);SELECT count(*) FROM pg_stat_activity WHERE application_name LIKE '%vertx%'")
          .execute(ctx.asyncAssertSuccess(rows -> {
          ctx.assertTrue(rows.next().iterator().next().getInteger(0) <= maxSize);
        }));
      }
    }, new DeploymentOptions().setInstances(instances), ctx.asyncAssertSuccess());
  }

  @Test
  public void testCloseAutomatically(TestContext ctx) {
    int maxSize = 8;
    int instances = maxSize * 4;
    Async latch = ctx.async(instances);
    AtomicReference<String> deployment = new AtomicReference<>();
    vertx.deployVerticle(() -> new AbstractVerticle() {
      @Override
      public void start() {
        PgPool pool = PgPool.pool(vertx, options, new PoolOptions().setMaxSize(maxSize).setShared(true));
        pool
          .query("SELECT 1")
          .execute(ctx.asyncAssertSuccess(res -> latch.countDown()));
      }
    }, new DeploymentOptions().setInstances(instances), ctx.asyncAssertSuccess(deployment::set));
    latch.awaitSuccess(20_000);
    vertx.undeploy(deployment.get())
      .compose(v -> PgConnection.connect(vertx, options))
      .compose(conn -> conn.query(COUNT_CONNECTIONS_QUERY)
        .execute().compose(res -> {
          int num = res.iterator().next().getInteger(0);
          return conn.close().map(num);
        })
      ).onComplete(ctx.asyncAssertSuccess(num -> {
        ctx.assertEquals(1, num);
      }));
  }

  @Test
  public void testPartialClose(TestContext ctx) {
    int maxSize = 8;
    int instances = maxSize * 4;
    vertx.deployVerticle(new AbstractVerticle() {
      @Override
      public void start() {
        PgPool pool = PgPool.pool(vertx, options, new PoolOptions().setMaxSize(maxSize).setShared(true));
        vertx.deployVerticle(() -> new AbstractVerticle() {
          @Override
          public void start(Promise<Void> startPromise) {
            PgPool pool = PgPool.pool(vertx, options, new PoolOptions().setMaxSize(maxSize).setShared(true));
            pool.query("SELECT 1").execute()
              .<Void>mapEmpty()
              .onComplete(startPromise);
          }
        }, new DeploymentOptions().setInstances(instances), ctx.asyncAssertSuccess(id -> {
          pool
            .query(COUNT_CONNECTIONS_QUERY)
            .execute(ctx.asyncAssertSuccess(res1 -> {
              int num1 = res1.iterator().next().getInteger(0);
              ctx.assertTrue(num1 <= maxSize);
              vertx.undeploy(id)
                .compose(v -> pool.query(COUNT_CONNECTIONS_QUERY).execute())
                .onComplete(ctx.asyncAssertSuccess(res2 -> {
                  int num2 = res1.iterator().next().getInteger(0);
                  ctx.assertEquals(num1, num2);
                }));
            }));
        }));
      }
    });
  }
}

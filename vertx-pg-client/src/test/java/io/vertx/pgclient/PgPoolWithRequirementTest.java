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

import java.util.HashSet;
import java.util.Set;

import static io.vertx.sqlclient.ServerRequirement.*;

@RunWith(VertxUnitRunner.class)
public class PgPoolWithRequirementTest {

  @ClassRule
  public static ContainerPgRule pg14Rule = new ContainerPgRule().setPostgresVersion("14");
  @ClassRule
  public static ContainerPgRule pg13Rule = new ContainerPgRule().setPostgresVersion("13");

  protected PgConnectOptions pg14Options;
  protected PgConnectOptions pg13Options;

  @Before
  public void setup() throws Exception {
    pg14Options = pg14Rule.options();
    pg13Options = pg13Rule.options();
  }

  private Set<PgPool> pools = new HashSet<>();

  @After
  public void tearDown(TestContext ctx) {
    int size = pools.size();
    if (size > 0) {
      Async async = ctx.async(size);
      Set<PgPool> pools = this.pools;
      this.pools = new HashSet<>();
      pools.forEach(pool -> {
        pool.close(ar -> {
          async.countDown();
        });
      });
      async.awaitSuccess(20_000);
    }
  }

  protected PgPool createPool(PgConnectOptions connectOptions, PoolOptions poolOptions) {
    PgPool pool = PgPool.pool(Vertx.vertx(), connectOptions, poolOptions);
    pools.add(pool);
    return pool;
  }

  @Test
  public void testPrimaryRequirementForSinglePrimaryFactory14(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(pg14Options), new PoolOptions().setServerRequirement(PRIMARY));
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v -> {
      async.complete();
    }));
    async.await(4000);
  }

  @Test
  public void testPrimaryRequirementForSinglePrimaryFactory13(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(pg13Options), new PoolOptions().setServerRequirement(PRIMARY));
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v -> {
      async.complete();
    }));
    async.await(4000);
  }

  @Test
  public void testExplicitAnyRequirementForSinglePrimaryFactory14(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(pg14Options), new PoolOptions().setServerRequirement(ANY));
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v -> {
      async.complete();
    }));
    async.await(4000);
  }

  @Test
  public void testExplicitAnyRequirementForSinglePrimaryFactory13(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(pg13Options), new PoolOptions().setServerRequirement(ANY));
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v -> {
      async.complete();
    }));
    async.await(4000);
  }

  @Test
  public void testReplicaRequirementForSinglePrimaryFactory14(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(pg14Options), new PoolOptions().setServerRequirement(REPLICA));
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertFailure(v -> {
      ctx.assertEquals("No suitable server of type REPLICA was found", v.getMessage());
      async.complete();
    }));
    async.await(4000);
  }

  @Test
  public void testReplicaRequirementForSinglePrimaryFactory13(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(pg13Options), new PoolOptions().setServerRequirement(REPLICA));
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertFailure(v -> {
      ctx.assertEquals("No suitable server of type REPLICA was found", v.getMessage());
      async.complete();
    }));
    async.await(4000);
  }

  @Test
  public void testExplicitPreferReplicaRequirementForSinglePrimaryFactory14(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(pg14Options), new PoolOptions().setServerRequirement(PREFER_REPLICA));
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v -> {
      async.complete();
    }));
    async.await(4000);
  }

  @Test
  public void testExplicitPreferReplicaRequirementForSinglePrimaryFactory13(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(pg13Options), new PoolOptions().setServerRequirement(PREFER_REPLICA));
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v -> {
      async.complete();
    }));
    async.await(4000);
  }
}

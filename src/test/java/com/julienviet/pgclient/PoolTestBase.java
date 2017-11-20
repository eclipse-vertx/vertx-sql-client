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

package com.julienviet.pgclient;

import io.vertx.core.Vertx;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class PoolTestBase extends PgTestBase {

  Vertx vertx;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  protected abstract PgPool createPool(PgClient client, int size);

  @Test
  public void testPool(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgClient client = PgClient.create(vertx, options);
    PgPool pool = createPool(client, 4);
    for (int i = 0;i < num;i++) {
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        conn.query("SELECT id, randomnumber from WORLD").execute(ar -> {
          if (ar.succeeded()) {
            ResultSet result = ar.result();
            ctx.assertEquals(10000, result.getNumRows());
          } else {
            ctx.assertEquals("closed", ar.cause().getMessage());
          }
          conn.close();
          async.countDown();
        });
      }));
    }
  }

  @Test
  public void testQuery(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgClient client = PgClient.create(vertx, options);
    PgPool pool = createPool(client, 4);
    for (int i = 0;i < num;i++) {
      pool.query("SELECT id, randomnumber from WORLD").execute(ar -> {
        if (ar.succeeded()) {
          ResultSet result = ar.result();
          ctx.assertEquals(10000, result.getNumRows());
        } else {
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testQueryWithParams(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgClient client = PgClient.create(vertx, options);
    PgPool pool = createPool(client, 4);
    for (int i = 0;i < num;i++) {
      pool.preparedQuery("SELECT id, randomnumber from WORLD where id=$1", i + 1, ar -> {
        if (ar.succeeded()) {
          ResultSet result = ar.result();
          ctx.assertEquals(1, result.getNumRows());
        } else {
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testUpdate(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgClient client = PgClient.create(vertx, options);
    PgPool pool = createPool(client, 4);
    for (int i = 0;i < num;i++) {
      pool.update("UPDATE Fortune SET message = 'Whatever' WHERE id = 9", ar -> {
        if (ar.succeeded()) {
          UpdateResult result = ar.result();
          ctx.assertEquals(1, result.getUpdated());
        } else {
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testUpdateWithParams(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgClient client = PgClient.create(vertx, options);
    PgPool pool = createPool(client, 4);
    for (int i = 0;i < num;i++) {
      pool.preparedUpdate("UPDATE Fortune SET message = 'Whatever' WHERE id = $1", 9, ar -> {
        if (ar.succeeded()) {
          UpdateResult result = ar.result();
          ctx.assertEquals(1, result.getUpdated());
        } else {
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testReconnect(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      PgClient client = PgClient.create(vertx, new PgClientOptions(options).setPort(8080).setHost("localhost"));
      PgPool pool = createPool(client, 1);
      pool.getConnection(ctx.asyncAssertSuccess(conn1 -> {
        proxyConn.get().close();
        conn1.closeHandler(v2 -> {
          conn1.query("never-executer").execute(ctx.asyncAssertFailure(err -> {
            pool.getConnection(ctx.asyncAssertSuccess(conn2 -> {
              conn2.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v3 -> {
                async.complete();
              }));
            }));
          }));
        });
      }));
    }));
  }
}

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

package io.reactiverse.pgclient;

import io.vertx.core.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

@RunWith(VertxUnitRunner.class)
public abstract class PgClientTestBase<C extends PgClient> extends PgTestBase {

  Vertx vertx;
  Consumer<Handler<AsyncResult<C>>> connector;
  PgConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new PgConnectOptions(PgTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testConnect(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidDatabase(TestContext ctx) {
    Async async = ctx.async();
    options.setDatabase("blah_db");
    connector.accept(ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("database \"blah_db\" does not exist", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidPassword(TestContext ctx) {
    Async async = ctx.async();
    options.setPassword("incorrect");
    connector.accept(ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("password authentication failed for user \"postgres\"", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidUsername(TestContext ctx) {
    Async async = ctx.async();
    options.setUser("vertx");
    connector.accept(ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("password authentication failed for user \"vertx\"", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(10000, result.size());
        Iterator<Row> it = result.iterator();
        for (int i = 0; i < 10000; i++) {
          Row row = it.next();
          ctx.assertEquals(2, row.size());
          ctx.assertTrue(row.getValue(0) instanceof Integer);
          ctx.assertEquals(row.getValue("id"), row.getValue(0));
          ctx.assertTrue(row.getValue(1) instanceof Integer);
          ctx.assertEquals(row.getValue("randomnumber"), row.getValue(1));
        }
        async.complete();
      }));
    }));
  }

  @Test
  public void testMultipleQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, message from FORTUNE LIMIT 1;SELECT message, id from FORTUNE LIMIT 1", ctx.asyncAssertSuccess(result1 -> {
        ctx.assertEquals(1, result1.size());
        ctx.assertEquals(Arrays.asList("id", "message"), result1.columnsNames());
        Tuple row1 = result1.iterator().next();
        ctx.assertTrue(row1.getValue(0) instanceof Integer);
        ctx.assertTrue(row1.getValue(1) instanceof String);
        PgRowSet result2 = result1.next();
        ctx.assertNotNull(result2);
        ctx.assertEquals(1, result2.size());
        ctx.assertEquals(Arrays.asList("message", "id"), result2.columnsNames());
        Tuple row2 = result2.iterator().next();
        ctx.assertTrue(row2.getValue(0) instanceof String);
        ctx.assertTrue(row2.getValue(1) instanceof Integer);
        ctx.assertNull(result2.next());
        async.complete();
      }));
    }));
  }

  @Test
  public void testQueryError(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(err -> {
        async.complete();
      }));
    }));
  }

  @Test
  public void testUpdate(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.updatedCount());
        async.complete();
      }));
    }));
  }

  @Test
  public void testInsert(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("INSERT INTO Fortune (id, message) VALUES (13, 'Whatever')", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.updatedCount());
        async.complete();
      }));
    }));
  }

  @Test
  public void testInsertReturning(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO Fortune (id, message) VALUES ($1, $2) RETURNING id", Tuple.of(14, "SomeMessage"), ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(14, result.iterator().next().getInteger("id"));
        async.complete();
      }));
    }));
  }

  @Test
  public void testInsertReturningError(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO Fortune (id, message) VALUES ($1, $2) RETURNING id", Tuple.of(15, "SomeMessage"), ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(15, result.iterator().next().getInteger("id"));
        conn.preparedQuery("INSERT INTO Fortune (id, message) VALUES ($1, $2) RETURNING id", Tuple.of(15, "SomeMessage"), ctx.asyncAssertFailure(err -> {
          ctx.assertEquals("23505", ((PgException) err).getCode());
          async.complete();
        }));
      }));
      }));
  }

  @Test
  public void testDelete(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("DELETE FROM Fortune where id = 6", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.updatedCount());
        async.complete();
      }));
    }));
  }

  static int randomWorld() {
    return 1 + ThreadLocalRandom.current().nextInt(10000);
  }

  @Test
  public void testBatchSelect(TestContext ctx) throws Exception {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      List<Tuple> batch = new ArrayList<>();
      batch.add(Tuple.tuple());
      batch.add(Tuple.tuple());
      conn.preparedBatch("SELECT count(id) FROM World", batch, ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(result.size(), result.next().size());
        async.complete();
      }));
    }));
  }

  @Test
  public void testDisconnectAbruptlyDuringStartup(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.clientSocket().handler(buff -> {
        conn.clientSocket().close();
      });
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      options.setPort(8080).setHost("localhost");
      connector.accept(ctx.asyncAssertFailure(err -> async.complete()));
    }));
  }

  @Test
  public void testTx(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(result1 -> {
        ctx.assertEquals(0, result1.size());
        ctx.assertNotNull(result1.iterator());
        conn.query("COMMIT", ctx.asyncAssertSuccess(result2 -> {
          ctx.assertEquals(0, result2.size());
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testPreparedUpdate(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("UPDATE Fortune SET message = 'PgClient Rocks!' WHERE id = 2", ctx.asyncAssertSuccess(res1 -> {
        ctx.assertEquals(1, res1.updatedCount());
        conn.preparedQuery("SELECT message FROM Fortune WHERE id = 2", ctx.asyncAssertSuccess(res2 -> {
          ctx.assertEquals("PgClient Rocks!", res2.iterator().next().getValue(0));
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testPreparedUpdateWithParams(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("UPDATE Fortune SET message = $1 WHERE id = $2", Tuple.of("PgClient Rocks Again!!", 2), ctx.asyncAssertSuccess(res1 -> {
        ctx.assertEquals(1, res1.updatedCount());
        conn.preparedQuery("SELECT message FROM Fortune WHERE id = $1", Tuple.of(2), ctx.asyncAssertSuccess(res2 -> {
          ctx.assertEquals("PgClient Rocks Again!!", res2.iterator().next().getValue(0));
          async.complete();
        }));
      }));
    }));
  }
}

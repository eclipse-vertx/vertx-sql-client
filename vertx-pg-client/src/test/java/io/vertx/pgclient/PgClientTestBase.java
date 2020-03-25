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

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.*;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public abstract class PgClientTestBase<C extends SqlClient> extends PgTestBase {

  Vertx vertx;
  Consumer<Handler<AsyncResult<C>>> connector;

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testConnectNonSSLServer(TestContext ctx) {
    Async async = ctx.async();
    options.setSslMode(SslMode.REQUIRE).setTrustAll(true);
    connector.accept(ctx.asyncAssertFailure(err -> {
      ctx.assertEquals("Postgres Server does not handle SSL connection", err.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testMultipleQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, message from FORTUNE LIMIT 1;SELECT message, id from FORTUNE LIMIT 1").execute(ctx.asyncAssertSuccess(result1 -> {
        ctx.assertEquals(1, result1.size());
        ctx.assertEquals(Arrays.asList("id", "message"), result1.columnsNames());
        Tuple row1 = result1.iterator().next();
        ctx.assertTrue(row1.getValue(0) instanceof Integer);
        ctx.assertTrue(row1.getValue(1) instanceof String);
        RowSet<Row> result2 = result1.next();
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
  public void testInsertReturning(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(client -> {
      deleteFromTestTable(ctx, client, () -> {
        client.preparedQuery("INSERT INTO Test (id, val) VALUES ($1, $2) RETURNING id").execute(Tuple.of(14, "SomeMessage"), ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(14, result.iterator().next().getInteger("id"));
          async.complete();
        }));
      });
    }));
  }

  @Test
  public void testInsertReturningError(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(client -> {
      deleteFromTestTable(ctx, client, () -> {
        client.preparedQuery("INSERT INTO Test (id, val) VALUES ($1, $2) RETURNING id").execute(Tuple.of(15, "SomeMessage"), ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(15, result.iterator().next().getInteger("id"));
          client.preparedQuery("INSERT INTO Test (id, val) VALUES ($1, $2) RETURNING id").execute(Tuple.of(15, "SomeMessage"), ctx.asyncAssertFailure(err -> {
            ctx.assertEquals("23505", ((PgException) err).getCode());
            async.complete();
          }));
        }));
      });
    }));
  }

  static int randomWorld() {
    return 1 + ThreadLocalRandom.current().nextInt(10000);
  }

  @Test
  public void testBatchSelect(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      List<Tuple> batch = new ArrayList<>();
      batch.add(Tuple.tuple());
      batch.add(Tuple.tuple());
      conn.preparedQuery("SELECT count(id) FROM World").executeBatch(batch, ctx.asyncAssertSuccess(result -> {
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
      NetSocket clientSo = conn.clientSocket();
      clientSo.handler(buff -> {
        clientSo.close();
      });
      clientSo.resume();
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
      conn.query("BEGIN").execute(ctx.asyncAssertSuccess(result1 -> {
        ctx.assertEquals(0, result1.size());
        ctx.assertNotNull(result1.iterator());
        conn.query("COMMIT").execute(ctx.asyncAssertSuccess(result2 -> {
          ctx.assertEquals(0, result2.size());
          async.complete();
        }));
      }));
    }));
  }
}

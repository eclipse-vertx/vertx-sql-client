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

package io.vertx.tests.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgException;
import io.vertx.pgclient.SslMode;
import io.vertx.tests.sqlclient.ProxyServer;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.SqlClientInternal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public abstract class PgClientTestBase<C extends SqlClient> extends PgTestBase {

  protected Vertx vertx;
  protected Consumer<Handler<AsyncResult<C>>> connector;

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testConnectNonSSLServer(TestContext ctx) {
    Async async = ctx.async();
    options.setSslMode(SslMode.REQUIRE).setSslOptions(new ClientSSLOptions().setTrustAll(true));
    connector.accept(ctx.asyncAssertFailure(err -> {
      ctx.assertEquals("Postgres Server does not handle SSL connection", err.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testMultipleQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT id, message from FORTUNE LIMIT 1;SELECT message, id from FORTUNE LIMIT 1")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result1 -> {
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
        client
          .preparedQuery("INSERT INTO Test (id, val) VALUES ($1, $2) RETURNING id")
          .execute(Tuple.of(14, "SomeMessage"))
          .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(14, result.iterator().next().getInteger("id"));
          async.complete();
        }));
      });
    }));
  }

  @Test
  public void testInsertReturningBatch(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(client -> {
      deleteFromTestTable(ctx, client, () -> {
        List<Tuple> batch = Arrays.asList(
          Tuple.of(14, "SomeMessage1"),
          Tuple.of(15, "SomeMessage2"));
        client
          .preparedQuery("INSERT INTO Test (id, val) VALUES ($1, $2) RETURNING id")
          .executeBatch(batch)
          .onComplete(ctx.asyncAssertSuccess(r1 -> {
            ctx.assertEquals(14, r1.iterator().next().getInteger("id"));
            RowSet<Row> r2 = r1.next();
            ctx.assertEquals(15, r2.iterator().next().getInteger("id"));
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
        client.preparedQuery("INSERT INTO Test (id, val) VALUES ($1, $2) RETURNING id")
          .execute(Tuple.of(15, "SomeMessage"))
          .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(15, result.iterator().next().getInteger("id"));
          client
            .preparedQuery("INSERT INTO Test (id, val) VALUES ($1, $2) RETURNING id")
            .execute(Tuple.of(15, "SomeMessage"))
            .onComplete(ctx.asyncAssertFailure(err -> {
              ctx.assertEquals("23505", ((PgException) err).getSqlState());
              async.complete();
            }));
        }));
      });
    }));
  }

  @Test
  public void testDeleteReturningBatch(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(client -> {
      deleteFromTestTable(ctx, client, () -> {
        List<Tuple> batch = Arrays.asList(
          Tuple.of(14, "SomeMessage1"),
          Tuple.of(15, "SomeMessage2"));
        client
          .preparedQuery("INSERT INTO Test (id, val) VALUES ($1, $2)")
          .executeBatch(batch)
          .compose(res -> client.query("DELETE FROM Test RETURNING id")
            .collecting(Collectors.toMap(row -> row.getInteger(0), row -> "whatever"))
            .execute()).onComplete(ctx.asyncAssertSuccess(res -> {
            ctx.assertEquals(2, res.size());
            ctx.assertEquals(new HashSet<>(Arrays.asList(14, 15)), res.value().keySet());
            async.complete();
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
      conn
        .preparedQuery("SELECT count(id) FROM World")
        .executeBatch(batch)
        .onComplete(ctx.asyncAssertSuccess(result -> {
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
      conn
        .query("BEGIN")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result1 -> {
        ctx.assertEquals(0, result1.size());
        ctx.assertNotNull(result1.iterator());
        conn
          .query("COMMIT")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(result2 -> {
          ctx.assertEquals(0, result2.size());
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testGrouping(TestContext ctx) {
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      ((SqlClientInternal)conn).group(client -> {
        client
          .query("SHOW TIME ZONE")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(res -> {
          ctx.assertEquals(1, res.size());
          Row row = res.iterator().next();
          ctx.assertEquals("PST8PDT", row.getString(0));
        }));
        conn
          .query("SET TIME ZONE 'PST8PDT'")
          .execute()
          .onComplete(ctx.asyncAssertSuccess());
      });
    }));
  }
}

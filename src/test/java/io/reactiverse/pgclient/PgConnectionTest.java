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

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionTest extends PgConnectionTestBase {

  public PgConnectionTest() {
    connector = (handler) -> PgClient.connect(vertx, options, handler);
  }

  @Test
  public void testBatchUpdate(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      deleteFromTestTable(ctx, conn, () -> {
        insertIntoTestTable(ctx, conn, 10, () -> {
          conn.prepare("UPDATE Test SET val=$1 WHERE id=$2", ctx.asyncAssertSuccess(ps -> {
            List<Tuple> batch = new ArrayList<>();
            batch.add(Tuple.of("val0", 0));
            batch.add(Tuple.of("val1", 1));
            ps.batch(batch, ctx.asyncAssertSuccess(result -> {
              for (int i = 0;i < 2;i++) {
                ctx.assertEquals(1, result.rowCount());
                result = result.next();
              }
              ctx.assertNull(result);
              ps.close(ctx.asyncAssertSuccess(v -> {
                async.complete();
              }));
            }));
          }));
        });
      });
    }));
  }

  @Test
  public void testClose(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.closeHandler(v -> {
        async.complete();
      });
      conn.close();
    }));
  }

  @Test
  public void testCloseWithErrorInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(err -> {
        ctx.assertEquals(2, async.count());
        async.countDown();
      }));
      conn.closeHandler(v -> {
        ctx.assertEquals(1, async.count());
        async.countDown();
      });
      conn.close();
    }));
  }

  @Test
  public void testCloseWithQueryInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(2, async.count());
        ctx.assertEquals(10000, result.size());
        async.countDown();
      }));
      conn.closeHandler(v -> {
        ctx.assertEquals(1, async.count());
        async.countDown();
      });
      conn.close();
    }));
  }

  @Test
  public void testQueueQueries(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num + 1);
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      for (int i = 0;i < num;i++) {
        conn.query("SELECT id, randomnumber from WORLD", ar -> {
          if (ar.succeeded()) {
            PgResult result = ar.result();
            ctx.assertEquals(10000, result.size());
          } else {
            ctx.assertEquals("closed", ar.cause().getMessage());
          }
          async.countDown();
        });
      }
      conn.closeHandler(v -> {
        ctx.assertEquals(1, async.count());
        async.countDown();
      });
      conn.close();
    }));
  }

  @Test
  public void testCancelRequest(TestContext ctx) {
    Async async = ctx.async(2);
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT pg_sleep(10)", ctx.asyncAssertFailure(error -> {
        ctx.assertEquals("canceling statement due to user request", error.getMessage());
        async.countDown();
      }));
      conn.cancelRequest(ctx.asyncAssertSuccess());

      conn.closeHandler(v -> {
        ctx.assertEquals(1, async.count());
        async.countDown();
      });
      conn.close();
    }));
  }
}

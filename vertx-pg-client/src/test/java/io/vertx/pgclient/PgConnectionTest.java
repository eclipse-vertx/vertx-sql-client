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

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.ClosedConnectionException;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionTest extends PgConnectionTestBase {

  public PgConnectionTest() {
    connector = (handler) -> PgConnection.connect(vertx, options, ar -> {
      handler.handle(ar.map(p -> p));
    });
  }

  @Test
  public void testSettingSchema(TestContext ctx) {
    options.addProperty("search_path", "myschema");
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("SHOW search_path;").execute(ctx.asyncAssertSuccess(pgRowSet -> {
        ctx.assertEquals("myschema", pgRowSet.iterator().next().getString("search_path"));
      }));
    }));
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
            ps.query().executeBatch(batch, ctx.asyncAssertSuccess(result -> {
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
  public void testQueueQueries(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num + 1);
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      for (int i = 0;i < num;i++) {
        conn
          .query("SELECT id, randomnumber from WORLD")
          .execute(ar -> {
          if (ar.succeeded()) {
            SqlResult result = ar.result();
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
      conn
        .query("SELECT pg_sleep(10)")
        .execute(ctx.asyncAssertFailure(error -> {
        ctx.assertTrue(hasSqlstateCode(error, ERRCODE_QUERY_CANCELED), error.getMessage());
        async.countDown();
      }));
      ((PgConnection)conn).cancelRequest(ctx.asyncAssertSuccess());

      conn.closeHandler(v -> {
        ctx.assertEquals(1, async.count());
        async.countDown();
      });
      conn.close();
    }));
  }

  @Test
  public void testInflightCommandsFailWhenConnectionClosed(TestContext ctx) {
    connector.accept(ctx.asyncAssertSuccess(conn1 -> {
      conn1.query("SELECT pg_sleep(20)").execute(ctx.asyncAssertFailure(t -> {
        ctx.assertTrue(t instanceof ClosedConnectionException);
      }));
      connector.accept(ctx.asyncAssertSuccess(conn2 -> {
        conn2.query("SELECT * FROM pg_stat_activity WHERE state = 'active' AND query = 'SELECT pg_sleep(20)'").execute(ctx.asyncAssertSuccess(statRes -> {
          for (Row row : statRes) {
            Integer id = row.getInteger("pid");
            // kill the connection
            conn2.query(String.format("SELECT pg_terminate_backend(%d);", id)).execute(ctx.asyncAssertSuccess(v -> conn2.close()));
            break;
          }
        }));
      }));
    }));
  }
}

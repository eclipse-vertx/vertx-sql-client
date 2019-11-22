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
package io.vertx.sqlclient.tck;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PreparedQueryTestBase {

  protected Vertx vertx;
  protected Connector<SqlConnection> connector;

  protected SqlConnectOptions options;

  private static void insertIntoTestTable(TestContext ctx, SqlClient client, int amount, Runnable completionHandler) {
    AtomicInteger count = new AtomicInteger();
    for (int i = 0; i < 10; i++) {
      client.query("INSERT INTO mutable (id, val) VALUES (" + i + ", 'Whatever-" + i + "')", ctx.asyncAssertSuccess(r1 -> {
        ctx.assertEquals(1, r1.rowCount());
        if (count.incrementAndGet() == amount) {
          completionHandler.run();
        }
      }));
    }
  }

  protected void connect(Handler<AsyncResult<SqlConnection>> handler) {
    connector.connect(handler);
  }

  protected abstract String statement(String... parts);

  protected abstract void initConnector();

  @Before
  public void setUp(TestContext ctx) throws Exception {
    vertx = Vertx.vertx();
    initConnector();
    cleanTestTable(ctx);
  }

  @After
  public void tearDown(TestContext ctx) {
    connector.close();
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testPrepare(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare(statement("SELECT id, message from immutable where id=", ""), ctx.asyncAssertSuccess(result -> {
        conn.close();
      }));
    }));
  }

  @Test
  public void testPrepareError(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(error -> {
      }));
    }));
  }

  @Test
  public void testPreparedQuery(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery(statement("SELECT * FROM immutable WHERE id=", ""), Tuple.of(1), ctx.asyncAssertSuccess(rowSet -> {
        ctx.assertEquals(1, rowSet.size());
        Tuple row = rowSet.iterator().next();
        ctx.assertEquals(1, row.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row.getString(1));
        conn.close();
      }));
    }));
  }

  @Test
  public void testPreparedQueryWithWrappedParams(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery(statement("SELECT * FROM immutable WHERE id=", ""), Tuple.wrap(Arrays.asList(1)), ctx.asyncAssertSuccess(rowSet -> {
        ctx.assertEquals(1, rowSet.size());
        Tuple row = rowSet.iterator().next();
        ctx.assertEquals(1, row.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row.getString(1));
        conn.close();
      }));
    }));
  }

  @Test
  public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare(statement("SELECT * FROM immutable WHERE id=", ""), ctx.asyncAssertSuccess(ps -> {
        ps.execute(Tuple.of("1"), ctx.asyncAssertFailure(error -> {
          ctx.assertEquals("Parameter at position[0] with class = [java.lang.String] and value = [1] can not be coerced to the expected class = [java.lang.Number] for encoding.", error.getMessage());
        }));
      }));
    }));
  }

  @Test
  public void testPreparedQueryParamCoercionQuantityError(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare(statement("SELECT * FROM immutable WHERE id=", ""), ctx.asyncAssertSuccess(ps -> {
        ps.execute(Tuple.of(1, 2), ctx.asyncAssertFailure(error -> {
          ctx.assertEquals("The number of parameters to execute should be consistent with the expected number of parameters = [1] but the actual number is [2].", error.getMessage());
        }));
      }));
    }));
  }

  @Test
  public void testPreparedUpdate(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO mutable (id, val) VALUES (2, 'Whatever')", ctx.asyncAssertSuccess(r1 -> {
        ctx.assertEquals(1, r1.rowCount());
        conn.preparedQuery("UPDATE mutable SET val = 'Rocks!' WHERE id = 2", ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.rowCount());
          conn.preparedQuery("SELECT val FROM mutable WHERE id = 2", ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals("Rocks!", res2.iterator().next().getValue(0));
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testPreparedUpdateWithParams(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO mutable (id, val) VALUES (2, 'Whatever')", ctx.asyncAssertSuccess(r1 -> {
        ctx.assertEquals(1, r1.rowCount());
        conn.preparedQuery(statement("UPDATE mutable SET val = ", " WHERE id = ", ""), Tuple.of("Rocks Again!!", 2), ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.rowCount());
          conn.preparedQuery(statement("SELECT val FROM mutable WHERE id = ", ""), Tuple.of(2), ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals("Rocks Again!!", res2.iterator().next().getValue(0));
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testPreparedUpdateWithNullParams(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery(
        statement("INSERT INTO mutable (val, id) VALUES (", ",", ")"), Tuple.of(null, 1),
        ctx.asyncAssertFailure(error -> {
        })
      );
    }));
  }

  // Need to test partial query close or abortion ?
  @Test
  public void testQueryCursor(TestContext ctx) {
    Async async = ctx.async();
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(begin -> {
        conn.prepare(statement("SELECT * FROM immutable WHERE id="," OR id=", " OR id=", " OR id=", " OR id=", " OR id=",""), ctx.asyncAssertSuccess(ps -> {
          Cursor query = ps.cursor(Tuple.of(1, 8, 4, 11, 2, 9));
          query.read(4, ctx.asyncAssertSuccess(result -> {
            ctx.assertNotNull(result.columnsNames());
            ctx.assertEquals(4, result.size());
            ctx.assertTrue(query.hasMore());
            query.read(4, ctx.asyncAssertSuccess(result2 -> {
              ctx.assertNotNull(result.columnsNames());
              ctx.assertEquals(4, result.size());
              ctx.assertFalse(query.hasMore());
              async.complete();
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testQueryCloseCursor(TestContext ctx) {
    Async async = ctx.async();
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(begin -> {
        conn.prepare(statement("SELECT * FROM immutable WHERE id="," OR id=", " OR id=", " OR id=", " OR id=", " OR id=",""), ctx.asyncAssertSuccess(ps -> {
          Cursor query = ps.cursor(Tuple.of(1, 8, 4, 11, 2, 9));
          query.read(4, ctx.asyncAssertSuccess(results -> {
            ctx.assertEquals(4, results.size());
            query.close(ctx.asyncAssertSuccess(v1 -> {
              ps.close(ctx.asyncAssertSuccess(v2 -> {
                async.complete();
              }));
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testQueryStreamCloseCursor(TestContext ctx) {
    Async async = ctx.async();
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(begin -> {
        conn.prepare(statement("SELECT * FROM immutable WHERE id="," OR id=", " OR id=", " OR id=", " OR id=", " OR id=",""), ctx.asyncAssertSuccess(ps -> {
          Cursor stream = ps.cursor(Tuple.of(1, 8, 4, 11, 2, 9));
          stream.read(4, ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(4, result.size());
            stream.close(ctx.asyncAssertSuccess(v1 -> {
              ps.close(ctx.asyncAssertSuccess(v2 -> {
                async.complete();
              }));
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testStreamQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM immutable", ctx.asyncAssertSuccess(ps -> {
          RowStream<Row> stream = ps.createStream(4, Tuple.tuple());
          List<Tuple> rows = new ArrayList<>();
          AtomicInteger ended = new AtomicInteger();
          stream.endHandler(v -> {
            ctx.assertEquals(0, ended.getAndIncrement());
            ctx.assertEquals(12, rows.size());
            async.complete();
          });
          stream.handler(tuple -> {
            ctx.assertEquals(0, ended.get());
            rows.add(tuple);
          });
        }));
      }));
    }));
  }

  @Test
  public void testStreamQueryPauseInBatch(TestContext ctx) {
    testStreamQueryPauseInBatch(ctx, Runnable::run);
  }

  @Test
  public void testStreamQueryPauseInBatchFromAnotherThread(TestContext ctx) {
    testStreamQueryPauseInBatch(ctx, t -> new Thread(t).start());
  }

  private void testStreamQueryPauseInBatch(TestContext ctx, Executor executor) {
    Async async = ctx.async();
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM immutable", ctx.asyncAssertSuccess(ps -> {
          RowStream<Row> stream = ps.createStream(4, Tuple.tuple());
          List<Tuple> rows = Collections.synchronizedList(new ArrayList<>());
          AtomicInteger ended = new AtomicInteger();
          executor.execute(() -> {
            stream.endHandler(v -> {
              ctx.assertEquals(0, ended.getAndIncrement());
              ctx.assertEquals(12, rows.size());
              async.complete();
            });
            stream.handler(tuple -> {
              rows.add(tuple);
              if (rows.size() == 2) {
                stream.pause();
                executor.execute(() -> {
                  vertx.setTimer(100, v -> {
                    executor.execute(stream::resume);
                  });
                });
              }
            });
          });
        }));
      }));
    }));
  }

  private void cleanTestTable(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("TRUNCATE TABLE mutable;", ctx.asyncAssertSuccess(result -> {
        conn.close();
      }));
    }));
  }
}

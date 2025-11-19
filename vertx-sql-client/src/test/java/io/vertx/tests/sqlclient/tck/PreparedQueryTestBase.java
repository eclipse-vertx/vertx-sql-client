/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.tests.sqlclient.tck;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.RowStreamInternal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class PreparedQueryTestBase {

  protected Vertx vertx;
  protected Connector<SqlConnection> connector;

  protected SqlConnectOptions options;

  protected Consumer<Throwable> msgVerifier;

  protected void connect(Handler<AsyncResult<SqlConnection>> handler) {
    connector.connect(handler);
  }

  protected abstract String statement(String... parts);

  protected abstract void initConnector();

  protected boolean cursorRequiresTx() {
      return true;
  }

  @Before
  public void setUp(TestContext ctx) throws Exception {
    vertx = Vertx.vertx();
    initConnector();
    cleanTestTable(ctx);
  }

  @After
  public void tearDown(TestContext ctx) {
    msgVerifier = null;
    connector.close();
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testPrepare(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .prepare(statement("SELECT id, message from immutable where id=", ""))
        .compose(PreparedStatement::close)
        .onComplete(ctx.asyncAssertSuccess(result -> {
          conn.close();
        }));
    }));
  }

  @Test
  public void testPrepareError(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT whatever from DOES_NOT_EXIST").onComplete(ctx.asyncAssertFailure(error -> {
        if (msgVerifier != null) {
          msgVerifier.accept(error);
        }
      }));
    }));
  }

  @Test
  public void testPreparedQuery(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(statement("SELECT * FROM immutable WHERE id=", ""))
        .execute(Tuple.of(1))
        .onComplete(ctx.asyncAssertSuccess(rowSet -> {
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
      conn
        .preparedQuery(statement("SELECT * FROM immutable WHERE id=", ""))
        .execute(Tuple.wrap(Arrays.asList(1)))
        .onComplete(ctx.asyncAssertSuccess(rowSet -> {
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
      conn
        .prepare(statement("SELECT * FROM immutable WHERE id=", ""))
        .onComplete(ctx.asyncAssertSuccess(ps -> {
        ps
          .query()
          .execute(Tuple.of("1"))
          .onComplete(ctx.asyncAssertFailure(error -> {
          if (msgVerifier != null) {
            msgVerifier.accept(error);
          } else {
            ctx.assertEquals("Parameter at position[0] with class = [java.lang.String] and value = [1] can not be coerced to the expected class = [java.lang.Number] for encoding.", error.getMessage());
          }
        }));
      }));
    }));
  }

  @Test
  public void testPreparedQueryParamCoercionQuantityError(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .prepare(statement("SELECT * FROM immutable WHERE id=", ""))
        .onComplete(ctx.asyncAssertSuccess(ps -> {
        ps
          .query()
          .execute(Tuple.of(1, 2))
          .onComplete(ctx.asyncAssertFailure(error -> {
          if (msgVerifier != null) {
            msgVerifier.accept(error);
          } else {
            ctx.assertEquals("The number of parameters to execute should be consistent with the expected number of parameters = [1] but the actual number is [2].", error.getMessage());
          }
        }));
      }));
    }));
  }

  @Test
  public void testPreparedUpdate(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("INSERT INTO mutable (id, val) VALUES (2, 'Whatever')")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(r1 -> {
        ctx.assertEquals(1, r1.rowCount());
        conn
          .preparedQuery("UPDATE mutable SET val = 'Rocks!' WHERE id = 2")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.rowCount());
          conn
            .preparedQuery("SELECT val FROM mutable WHERE id = 2")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(res2 -> {
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
      conn
        .preparedQuery("INSERT INTO mutable (id, val) VALUES (2, 'Whatever')")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(r1 -> {
        ctx.assertEquals(1, r1.rowCount());
        conn
          .preparedQuery(statement("UPDATE mutable SET val = ", " WHERE id = ", ""))
          .execute(Tuple.of("Rocks Again!!", 2))
          .onComplete(ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.rowCount());
          conn
            .preparedQuery(statement("SELECT val FROM mutable WHERE id = ", ""))
            .execute(Tuple.of(2))
            .onComplete(ctx.asyncAssertSuccess(res2 -> {
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
        statement("INSERT INTO mutable (val, id) VALUES (", ",", ")"))
        .execute(Tuple.of(null, 1))
        .onComplete(ctx.asyncAssertFailure(error -> {
          if (msgVerifier != null) {
            msgVerifier.accept(error);
          }
        })
      );
    }));
  }

  private void testCursor(TestContext ctx, Handler<SqlConnection> test) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      if (cursorRequiresTx()) {
        conn
          .query("BEGIN")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(begin -> {
          test.handle(conn);
        }));
      } else {
        test.handle(conn);
      }
    }));
  }

  // Need to test partial query close or abortion ?
  @Test
  public void testQueryCursor(TestContext ctx) {
    Async async = ctx.async();
    testCursor(ctx, conn -> {
      conn
        .prepare(statement("SELECT * FROM immutable WHERE id=", " OR id=", " OR id=", " OR id=", " OR id=", " OR id=", ""))
        .onComplete(ctx.asyncAssertSuccess(ps -> {
          Cursor cursor = ps.cursor(Tuple.of(1, 8, 4, 11, 2, 9));
          ctx.assertNull(cursor.rowDescriptor());
          cursor
            .read(4)
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ctx.assertNotNull(result.columnsNames());
              ctx.assertNotNull(cursor.rowDescriptor());
              ctx.assertEquals(result.columnsNames(), cursor.rowDescriptor().columnNames());
              ctx.assertEquals(4, result.size());
              ctx.assertTrue(cursor.hasMore());
              cursor
                .read(4)
                .onComplete(ctx.asyncAssertSuccess(result2 -> {
                  ctx.assertNotNull(result2.columnsNames());
                  ctx.assertNotNull(cursor.rowDescriptor());
                  ctx.assertEquals(result2.columnsNames(), cursor.rowDescriptor().columnNames());
                  ctx.assertEquals(2, result2.size());
                  ctx.assertFalse(cursor.hasMore());
                  async.complete();
                }));
            }));
        }));
    });
  }

  @Test
  public void testQueryCursorNoResults(TestContext ctx) {
    Async async = ctx.async();
    testCursor(ctx, conn -> {
      conn
        .prepare("SELECT * FROM immutable WHERE 1=3")
        .onComplete(ctx.asyncAssertSuccess(ps -> {
          Cursor cursor = ps.cursor(Tuple.tuple());
          ctx.assertNull(cursor.rowDescriptor());
          cursor
            .read(99)
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ctx.assertNotNull(result.columnsNames());
              ctx.assertNotNull(cursor.rowDescriptor());
              ctx.assertEquals(result.columnsNames(), cursor.rowDescriptor().columnNames());
              ctx.assertEquals(0, result.size());
              ctx.assertFalse(cursor.hasMore());
              async.complete();
            }));
        }));
    });
  }

  @Test
  public void testQueryCloseCursor(TestContext ctx) {
    Async async = ctx.async();
    testCursor(ctx, conn -> {
      conn
        .prepare(statement("SELECT * FROM immutable WHERE id="," OR id=", " OR id=", " OR id=", " OR id=", " OR id=",""))
        .onComplete(ctx.asyncAssertSuccess(ps -> {
        Cursor cursor = ps.cursor(Tuple.of(1, 8, 4, 11, 2, 9));
        cursor
          .read(4)
          .onComplete(ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(4, results.size());
          cursor.close().onComplete(ctx.asyncAssertSuccess(v1 -> {
            ctx.assertTrue(cursor.isClosed());
            ps.close().onComplete(ctx.asyncAssertSuccess(v2 -> {
              async.complete();
            }));
          }));
        }));
      }));
    });
  }

  @Test
  public void testQueryStreamCloseCursor(TestContext ctx) {
    Async async = ctx.async();
    testCursor(ctx, conn -> {
      conn
        .prepare(statement("SELECT * FROM immutable WHERE id=", " OR id=", " OR id=", " OR id=", " OR id=", " OR id=", ""))
        .onComplete(ctx.asyncAssertSuccess(ps -> {
        RowStream<Row> stream = ps.createStream(4, Tuple.of(1, 8, 4, 11, 2, 9));
        List<Row> rows = new ArrayList<>();
        stream.handler(row -> {
          rows.add(row);
          if (rows.size() == 4) {
            Cursor cursor = ((RowStreamInternal) stream).cursor();
            ctx.assertFalse(cursor.isClosed());
            stream.close().onComplete(ctx.asyncAssertSuccess(v1 -> {
              ctx.assertTrue(cursor.isClosed());
              ps.close().onComplete(ctx.asyncAssertSuccess(v2 -> {
                async.complete();
              }));
            }));
          }
        });
      }));
    });
  }

  @Test
  public void testStreamQuery(TestContext ctx) {
    Async async = ctx.async();
    testCursor(ctx, conn -> {
      conn.prepare("SELECT * FROM immutable").onComplete(ctx.asyncAssertSuccess(ps -> {
        RowStream<Row> stream = ps.createStream(4, Tuple.tuple());
        ctx.assertNull(stream.rowDescriptor());
        List<Tuple> rows = new ArrayList<>();
        AtomicInteger ended = new AtomicInteger();
        stream.handler(tuple -> {
          ctx.assertEquals(0, ended.get());
          ctx.assertNotNull(stream.rowDescriptor());
          rows.add(tuple);
        });
        Cursor cursor = ((RowStreamInternal) stream).cursor();
        stream.endHandler(v -> {
          ctx.assertTrue(cursor.isClosed());
          ctx.assertEquals(0, ended.getAndIncrement());
          ctx.assertEquals(12, rows.size());
          async.complete();
        });
      }));
    });
  }

  @Test
  public void testStreamQueryNoResults(TestContext ctx) {
    Async async = ctx.async();
    testCursor(ctx, conn -> {
      conn.prepare("SELECT * FROM immutable WHERE 1=3").onComplete(ctx.asyncAssertSuccess(ps -> {
        RowStream<Row> stream = ps.createStream(99, Tuple.tuple());
        ctx.assertNull(stream.rowDescriptor());
        stream.endHandler(v -> {
          ctx.assertNotNull(stream.rowDescriptor());
          async.complete();
        });
        stream.handler(tuple -> {
          ctx.fail("Should not get here");
        });
      }));
    });
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
    testCursor(ctx, conn -> {
      conn.prepare("SELECT * FROM immutable").onComplete(ctx.asyncAssertSuccess(ps -> {
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
    });
  }

  @Test
  public void testStreamQueryPauseResume(TestContext ctx) {
    Async async = ctx.async();
    testCursor(ctx, conn -> {
      conn.prepare("SELECT * FROM immutable").onComplete(ctx.asyncAssertSuccess(ps -> {
        RowStream<Row> stream = ps.createStream(4, Tuple.tuple());
        List<Tuple> rows = new ArrayList<>();
        AtomicInteger ended = new AtomicInteger();
        stream.handler(tuple -> {
          ctx.assertEquals(0, ended.get());
          rows.add(tuple);
        });
        stream.pause();
        stream.resume();
        stream.endHandler(v -> {
          async.complete();
        });
      }));
    });
  }

  protected void cleanTestTable(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("TRUNCATE TABLE mutable")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        conn.close();
      }));
    }));
  }
}

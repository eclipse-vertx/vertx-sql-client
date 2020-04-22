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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.impl.util.Util;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class PreparedStatementTestBase extends PgTestBase {

  Vertx vertx;

  protected abstract PgConnectOptions options();

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
  public void testQuery1Param(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> {
        ps.query().execute(Tuple.of(1), ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(1, results.size());
          Tuple row = results.iterator().next();
          ctx.assertEquals(1, row.getInteger(0));
          ctx.assertEquals("fortune: No such file or directory", row.getString(1));
          ps.close(ctx.asyncAssertSuccess(ar -> {
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testQuery(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", ctx.asyncAssertSuccess(ps -> {
        ps.query().execute(Tuple.of(1, 8, 4, 11, 2, 9), ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(6, results.size());
          ps.close(ctx.asyncAssertSuccess(result -> {
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testCollectorQuery(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", ctx.asyncAssertSuccess(ps -> {
        ps.query().collecting(Collectors.toList()).execute(Tuple.of(1, 8, 4, 11, 2, 9), ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(6, results.size());
          List<Row> list = results.value();
          ctx.assertEquals(list.size(), 6);
          ctx.assertEquals(6L, list.stream().distinct().count());
          ps.close(ctx.asyncAssertSuccess(result -> {
            async.complete();
          }));
        }));
      }));
    }));
  }

/*
  @Test
  public void testQueryStream(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), (ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", ctx.asyncAssertSuccess(ps -> {
        PgQuery createStream = ps.query(1, 8, 4, 11, 2, 9);
        LinkedList<JsonArray> results = new LinkedList<>();
        createStream.exceptionHandler(ctx::fail);
        createStream.endHandler(v -> {
          ctx.assertEquals(6, results.size());
          ps.close(ctx.asyncAssertSuccess(result -> {
            async.complete();
          }));
        });
        createStream.handler(rs -> results.addAll(rs.getResults()));
      }));
    }));
  }
*/
  @Test
  public void testQueryParseError(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("invalid", ctx.asyncAssertFailure(err -> {
        PgException pgErr = (PgException) err;
        ctx.assertEquals(ErrorCodes.syntax_error, pgErr.getCode());
        async.complete();
      }));
    }));
  }

  private static final String validationErrorSql = "SELECT * FROM Fortune WHERE id=$1";
  private static final Tuple validationErrorTuple = Tuple.of("invalid-id");

  private void testValidationError(TestContext ctx, BiConsumer<PgConnection, Handler<Throwable>> test) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      test.accept(conn, failure -> {
        ctx.assertEquals("Parameter at position[0] with class = [java.lang.String] and value = [invalid-id] can not be coerced to the expected class = [java.lang.Number] for encoding.", failure.getMessage());
        async.complete();
      });
      conn.preparedQuery("SELECT * FROM Fortune WHERE id=$1").execute(Tuple.of("invalid-id"), ctx.asyncAssertFailure(failure -> {
      }));
    }));
  }

  @Test
  public void testPrepareExecuteValidationError(TestContext ctx) {
    testValidationError(ctx, (conn, cont) -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> {
        ps.query().execute(Tuple.of("invalid-id"), ctx.asyncAssertFailure(cont));
      }));
    });
  }

  @Test
  public void testPrepareCursorValidationError(TestContext ctx) {
    testValidationError(ctx, (conn, cont) -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> {
        try {
          ps.cursor(Tuple.of("invalid-id"));
        } catch (Exception e) {
          cont.handle(e);
        }
      }));
    });
  }

  @Test
  public void testPrepareBatchValidationError(TestContext ctx) {
    testValidationError(ctx, (conn, cont) -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> {
        ps.query().executeBatch(Collections.singletonList(Tuple.of("invalid-id")), ctx.asyncAssertFailure(cont));
      }));
    });
  }

  @Test
  public void testPreparedQueryValidationError(TestContext ctx) {
    testValidationError(ctx, (conn, cont) -> {
      conn.preparedQuery("SELECT * FROM Fortune WHERE id=$1").execute(Tuple.of("invalid-id"), ctx.asyncAssertFailure(cont));
    });
  }

  @Test
  public void testPreparedBatchValidationError(TestContext ctx) {
    testValidationError(ctx, (conn, cont) -> {
      conn.preparedQuery("SELECT * FROM Fortune WHERE id=$1").executeBatch(Collections.singletonList(Tuple.of("invalid-id")), ctx.asyncAssertFailure(cont));
    });
  }

  @Test
  public void testNullValueIsAlwaysValid(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT 1 WHERE $1::INT4 IS NULL").execute(Tuple.tuple().addInteger(null), ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          async.complete();
        }));
    }));
  }

  @Test
  public void testStreamQueryError(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune", ctx.asyncAssertSuccess(ps -> {
        RowStream<Row> stream = ps.createStream(4, Tuple.tuple());
        stream.endHandler(v -> ctx.fail());
        AtomicInteger rowCount = new AtomicInteger();
        stream.exceptionHandler(err -> {
          ctx.assertEquals(4, rowCount.getAndIncrement());
          async.complete();
        });
        stream.handler(tuple -> rowCount.incrementAndGet());
      }));
    }));
  }

  @Test
  public void testCursorNoTx(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune", ctx.asyncAssertSuccess(ps -> {
        Cursor cursor = ps.cursor(Tuple.tuple());
        cursor.read(1, ctx.asyncAssertSuccess(rowSet -> {
          cursor.read(1, ctx.asyncAssertFailure(err -> {
            PgException pgErr = (PgException) err;
            // This fails expectedly because the portal is closed
            ctx.assertEquals("34000", pgErr.getCode()); // invalid_cursor_name
          }));
        }));
      }));
    }));
  }


  /*
  @Test
  public void testStreamQueryCancel(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), (ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN").execute(ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM Fortune", ctx.asyncAssertSuccess(ps -> {
          PgStream<Tuple> createStream = ps.createStream(Tuple.tuple());
          AtomicInteger count = new AtomicInteger();
          createStream.handler(tuple -> {
            ctx.assertEquals(0, count.getAndIncrement());
            createStream.handler(null);
          });
        }));
      }));
    }));
  }
  */

  @Test
  public void testCloseStatement(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> {
        conn.query("SELECT * FROM pg_prepared_statements").execute(ctx.asyncAssertSuccess(res1 -> {
          boolean isStatementPrepared = false;
          for (Row row : res1) {
            String statement = row.getString("statement");
            if (statement.equals("SELECT * FROM Fortune WHERE id=$1")) {
              isStatementPrepared = true;
            }
          }
          if (!isStatementPrepared) {
            ctx.fail("Statement is not prepared");
          }
          ps.close(ctx.asyncAssertSuccess(v -> {
            conn.query("SELECT * FROM pg_prepared_statements").execute(ctx.asyncAssertSuccess(res2 -> {
              for (Row row : res2) {
                String statement = row.getString("statement");
                if (statement.equals("SELECT * FROM Fortune WHERE id=$1")) {
                  ctx.fail("Statement is not closed");
                }
              }
              conn.close();
            }));
          }));
        }));
      }));
    }));
  }
}

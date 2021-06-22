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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.data.*;
import io.vertx.sqlclient.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        ps.query()
          .execute(Tuple.of(1, 8, 4, 11, 2, 9), ctx.asyncAssertSuccess(results -> {
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

  @Test
  public void testMappedQuery(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1 :: INT4", ctx.asyncAssertSuccess(ps -> {
        ps.query()
          .mapping(row -> "" + row.getInteger(0))
          .execute(Tuple.of(1), ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(1, results.size());
          RowSet<String> rows = results.value();
          ctx.assertEquals(rows.size(), 1);
          RowIterator<String> it = rows.iterator();
          ctx.assertEquals("1", it.next());
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

  public static final Tuple INVALID_TUPLE = Tuple.of("invalid-id");

  private void testValidationError(TestContext ctx, BiConsumer<PgConnection, Handler<Throwable>> test) {
    int times = 3;
    Async async = ctx.async(times);
    Consumer<Throwable> check = failure -> ctx.assertEquals("Parameter at position[0] with class = [java.lang.String] and value = [invalid-id] can not be coerced to the expected class = [java.lang.Number] for encoding.", failure.getMessage());
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      // This will test with pipelining
      for (int i = 0;i < times;i++) {
        test.accept(conn, failure1 -> {
          check.accept(failure1);
          test.accept(conn, failure2 -> {
            check.accept(failure2);
            test.accept(conn, failure3 -> {
              check.accept(failure3);
              async.countDown();
            });
          });
        });
      }
    }));
  }

  @Test
  public void testPrepareExecuteValidationError(TestContext ctx) {
    testValidationError(ctx, (conn, cont) -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> {
        ps.query().execute(INVALID_TUPLE, ctx.asyncAssertFailure(cont));
      }));
    });
  }

  @Test
  public void testPreparedQueryValidationError(TestContext ctx) {
    testValidationError(ctx, (conn, cont) -> {
      conn
        .preparedQuery("SELECT * FROM Fortune WHERE id=$1")
        .execute(INVALID_TUPLE, ctx.asyncAssertFailure(cont));
    });
  }

  @Test
  public void testPreparedQueryValidationError_(TestContext ctx) {
    testValidationError(ctx, (conn, cont) -> {
      conn
        .preparedQuery("SELECT * FROM Fortune WHERE id=$1")
        .execute(INVALID_TUPLE, ctx.asyncAssertFailure(cont));
    });
  }

  @Test
  public void testPrepareCursorValidationError(TestContext ctx) {
    testValidationError(ctx, (conn, cont) -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> {
        Cursor cursor = ps.cursor(INVALID_TUPLE);
        cursor.read(10, ctx.asyncAssertFailure(cont));
      }));
    });
  }

  @Test
  public void testPrepareBatchValidationError(TestContext ctx) {
    testValidationError(ctx, (conn, cont) -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> {
        ps.query().executeBatch(Collections.singletonList(INVALID_TUPLE), ctx.asyncAssertFailure(cont));
      }));
    });
  }

  @Test
  public void testPreparedBatchValidationError(TestContext ctx) {
    testValidationError(ctx, (conn, cont) -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> ps
        .query()
        .executeBatch(Collections.singletonList(INVALID_TUPLE), ctx.asyncAssertFailure(cont))));
    });
  }

  @Test
  public void testNullValueIsAlwaysValid(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn
        .prepare("SELECT 1 WHERE $1::INT4 IS NULL", ctx.asyncAssertSuccess(ps ->
          ps.query()
            .execute(Tuple.tuple().addInteger(null), ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              async.complete();
            }))));
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

  @Test
  public void testInferDataTypeString(TestContext ctx) {
    testInferDataType(ctx, String.class, "WORLD", "WORLD");
  }

  @Test
  public void testInferDataTypeBoolean(TestContext ctx) {
    testInferDataType(ctx, Boolean.class, true, "t");
  }

  @Test
  public void testInferDataTypeShort(TestContext ctx) {
    testInferDataType(ctx, Short.class, (short)2, "2");
  }

  @Test
  public void testInferDataTypeInteger(TestContext ctx) {
    testInferDataType(ctx, Integer.class, Integer.MAX_VALUE, "" + Integer.MAX_VALUE);
  }

  @Test
  public void testInferDataTypeLong(TestContext ctx) {
    testInferDataType(ctx, Long.class, Long.MAX_VALUE, "" + Long.MAX_VALUE);
  }

  @Test
  public void testInferDataTypeFloat(TestContext ctx) {
    testInferDataType(ctx, Float.class, 0F, "0");
  }

  @Test
  public void testInferDataTypeDouble(TestContext ctx) {
    testInferDataType(ctx, Double.class, 0D, "0");
  }

  @Test
  public void testInferDataTypeLocalDate(TestContext ctx) {
    LocalDate value = LocalDate.now();
    testInferDataType(ctx, LocalDate.class, value, value.toString());
  }

  @Test
  public void testInferDataTypeLocalDateTime(TestContext ctx) {
    LocalDateTime value = LocalDateTime.of(LocalDate.now(), LocalTime.NOON);
    String suffix = value.toLocalDate() + " " + value.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME);
    testInferDataType(ctx, LocalDateTime.class, value, suffix, "{\"" + suffix + "\"}");
  }

  @Test
  public void testInferDataTypeOffsetDateTime(TestContext ctx) {
    OffsetDateTime value = OffsetDateTime.of(LocalDateTime.of(LocalDate.now(), LocalTime.NOON), ZoneOffset.UTC);
    String suffix = value.toLocalDate() + " " + value.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME) + "+00";
    testInferDataType(ctx, OffsetDateTime.class, value, suffix, "{\"" + suffix + "\"}");
  }

  @Test
  public void testInferDataTypeOffsetInterval(TestContext ctx) {
    Interval value = Interval.of(1);
    testInferDataType(ctx, Interval.class, value, "1 year", "{\"1 year\"}");
  }

  @Test
  public void testInferDataTypeBuffer(TestContext ctx) {
    testInferDataType(ctx, Buffer.class, Buffer.buffer("WORLD"), "\\x574f524c44", "{\"\\\\x574f524c44\"}");
  }

  @Test
  public void testInferDataTypeUUID(TestContext ctx) {
    UUID value = UUID.randomUUID();
    testInferDataType(ctx, UUID.class, value, "" + value);
  }

  @Test
  public void testInferDataTypeJsonObject(TestContext ctx) {
    JsonObject value = new JsonObject().put("foo", "bar");
    testInferDataType(ctx, JsonObject.class, value, "" + value, "{\"{\\\"foo\\\":\\\"bar\\\"}\"}");
  }

  @Test
  public void testInferDataTypeJsonArray(TestContext ctx) {
    JsonArray value = new JsonArray().add(1).add("foo").add(true);
    testInferDataType(ctx, JsonArray.class, value, "" + value, "{\"[1,\\\"foo\\\",true]\"}");
  }

  @Test
  public void testInferDataTypePoint(TestContext ctx) {
    Point value = new Point();
    testInferDataType(ctx, Point.class, value, "(0,0)", "{\"(0,0)\"}");
  }

  @Test
  public void testInferDataTypeLine(TestContext ctx) {
    Line value = new Line(1.0, 0.0, 0.0);
    testInferDataType(ctx, Line.class, value, "{1,0,0}", "{\"{1,0,0}\"}");
  }

  @Test
  public void testInferDataTypeLineSegment(TestContext ctx) {
    LineSegment value = new LineSegment();
    testInferDataType(ctx, LineSegment.class, value, "[(0,0),(0,0)]", "{\"[(0,0),(0,0)]\"}");
  }

  @Test
  public void testInferDataTypeBox(TestContext ctx) {
    Box value = new Box();
    testInferDataType(ctx, Box.class, value, "(0,0),(0,0)");
  }

  @Test
  public void testInferDataTypePath(TestContext ctx) {
    Path value = new Path().addPoint(new Point());
    testInferDataType(ctx, Path.class, value, "((0,0))", "{\"((0,0))\"}");
  }

  @Test
  public void testInferDataTypePolygon(TestContext ctx) {
    Polygon value = new Polygon().addPoint(new Point()).addPoint(new Point()).addPoint(new Point());
    testInferDataType(ctx, Polygon.class, value, "((0,0),(0,0),(0,0))", "{\"((0,0),(0,0),(0,0))\"}");
  }

  @Test
  public void testInferDataTypeCircle(TestContext ctx) {
    Circle value = new Circle();
    testInferDataType(ctx, Circle.class, value, "<(0,0),0>", "{\"<(0,0),0>\"}");
  }

  private <T> void testInferDataType(TestContext ctx, Class<T> type, T value, String suffix) {
    testInferDataType(ctx, type, value, suffix, "{" + suffix + "}");
  }

  private <T> void testInferDataType(TestContext ctx, Class<T> type, T value, String suffix1, String suffix2) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT CONCAT('HELLO ', $1)")
        .execute(Tuple.of(value), ctx.asyncAssertSuccess(result1 -> {
          Row row1 = result1.iterator().next();
          ctx.assertEquals("HELLO " + suffix1, row1.getString(0));
          Object array = Array.newInstance(type, 1);
          Array.set(array, 0, value);
          conn.preparedQuery("SELECT CONCAT('HELLO ', $1)")
            .execute(Tuple.of(array), ctx.asyncAssertSuccess(result2 -> {
              Row row2 = result2.iterator().next();
              String v = row2.getString(0);
              ctx.assertEquals("HELLO " + suffix2, row2.getString(0));
              conn.close();
            }));
        }));
    }));
  }

  @Test
  public void testInferDataTypeFailure(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT CONCAT('HELLO', $1)")
        .execute(Tuple.of(null), ctx.asyncAssertFailure(result -> {
          conn.close();
        }));
    }));
  }

  @Test
  public void testInferDataTypeLazy(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT CONCAT('HELLO', $1)", ctx.asyncAssertSuccess(ps -> {
        ps.query().execute(Tuple.of("__"), ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          ctx.assertEquals("HELLO__", row.getString(0));
          conn.close();
        }));
      }));
    }));
  }

  @Test
  public void testInferDataTypeLazyFailure(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT CONCAT('HELLO', $1)", ctx.asyncAssertSuccess(ps -> {
        ps.query().execute(Tuple.of(null), ctx.asyncAssertFailure(result -> {
          conn.close();
        }));
      }));
    }));
  }

  @Test
  public void testInferDataTypeLazyPolymorphic(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT to_jsonb($1)", ctx.asyncAssertSuccess(ps -> {
        ps.query().execute(Tuple.of("foo"), ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals("foo", result.iterator().next().getString(0));
          conn.close();
        }));
      }));
    }));
  }
}

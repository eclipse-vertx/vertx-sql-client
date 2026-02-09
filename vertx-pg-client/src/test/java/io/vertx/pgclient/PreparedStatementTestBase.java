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
import io.vertx.pgclient.data.Box;
import io.vertx.pgclient.data.Circle;
import io.vertx.pgclient.data.Interval;
import io.vertx.pgclient.data.Line;
import io.vertx.pgclient.data.LineSegment;
import io.vertx.pgclient.data.Path;
import io.vertx.pgclient.data.Point;
import io.vertx.pgclient.data.Polygon;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
        ctx.assertEquals(ErrorCodes.syntax_error, pgErr.getSqlState());
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
            ctx.assertEquals("34000", pgErr.getSqlState()); // invalid_cursor_name
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
  public void testInferDataTypeString42P18(TestContext ctx) {
    testInferDataType42P18(ctx, String.class, "WORLD", "WORLD");
  }

  @Test
  public void testInferDataTypeBoolean42P18(TestContext ctx) {
    testInferDataType42P18(ctx, Boolean.class, true, "t");
  }

  @Test
  public void testInferDataTypeShort42P18(TestContext ctx) {
    testInferDataType42P18(ctx, Short.class, (short)2, "2");
  }

  @Test
  public void testInferDataTypeInteger42P18(TestContext ctx) {
    testInferDataType42P18(ctx, Integer.class, Integer.MAX_VALUE, "" + Integer.MAX_VALUE);
  }

  @Test
  public void testInferDataTypeLong42P18(TestContext ctx) {
    testInferDataType42P18(ctx, Long.class, Long.MAX_VALUE, "" + Long.MAX_VALUE);
  }

  @Test
  public void testInferDataTypeFloat42P18(TestContext ctx) {
    testInferDataType42P18(ctx, Float.class, 0F, "0");
  }

  @Test
  public void testInferDataTypeDouble42P18(TestContext ctx) {
    testInferDataType42P18(ctx, Double.class, 0D, "0");
  }

  @Test
  public void testInferDataTypeLocalDate42P18(TestContext ctx) {
    LocalDate value = LocalDate.now();
    testInferDataType42P18(ctx, LocalDate.class, value, value.toString());
  }

  @Test
  public void testInferDataTypeLocalDateTime42P18(TestContext ctx) {
    LocalDateTime value = LocalDateTime.of(LocalDate.now(), LocalTime.NOON);
    String suffix = value.toLocalDate() + " " + value.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME);
    testInferDataType42P18(ctx, LocalDateTime.class, value, suffix, "{\"" + suffix + "\"}");
  }

  @Test
  public void testInferDataTypeOffsetDateTime42P18(TestContext ctx) {
    OffsetDateTime value = OffsetDateTime.of(LocalDateTime.of(LocalDate.now(), LocalTime.NOON), ZoneOffset.UTC);
    String suffix = value.toLocalDate() + " " + value.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME) + "+00";
    testInferDataType42P18(ctx, OffsetDateTime.class, value, suffix, "{\"" + suffix + "\"}");
  }

  @Test
  public void testInferDataTypeOffsetInterval42P18(TestContext ctx) {
    Interval value = Interval.of(1);
    testInferDataType42P18(ctx, Interval.class, value, "1 year", "{\"1 year\"}");
  }

  @Test
  public void testInferDataTypeBuffer42P18(TestContext ctx) {
    testInferDataType42P18(ctx, Buffer.class, Buffer.buffer("WORLD"), "\\x574f524c44", "{\"\\\\x574f524c44\"}");
  }

  @Test
  public void testInferDataTypeUUID42P18(TestContext ctx) {
    UUID value = UUID.randomUUID();
    testInferDataType42P18(ctx, UUID.class, value, "" + value);
  }

  @Test
  public void testInferDataTypeJsonObject42P18(TestContext ctx) {
    JsonObject value = new JsonObject().put("foo", "bar");
    testInferDataType42P18(ctx, JsonObject.class, value, "" + value, "{\"{\\\"foo\\\":\\\"bar\\\"}\"}");
  }

  @Test
  public void testInferDataTypeJsonArray42P18(TestContext ctx) {
    JsonArray value = new JsonArray().add(1).add("foo").add(true);
    testInferDataType42P18(ctx, JsonArray.class, value, "" + value, "{\"[1,\\\"foo\\\",true]\"}");
  }

  @Test
  public void testInferDataTypePoint42P18(TestContext ctx) {
    Point value = new Point();
    testInferDataType42P18(ctx, Point.class, value, "(0,0)", "{\"(0,0)\"}");
  }

  @Test
  public void testInferDataTypeLine42P18(TestContext ctx) {
    Line value = new Line(1.0, 0.0, 0.0);
    testInferDataType42P18(ctx, Line.class, value, "{1,0,0}", "{\"{1,0,0}\"}");
  }

  @Test
  public void testInferDataTypeLineSegment42P18(TestContext ctx) {
    LineSegment value = new LineSegment();
    testInferDataType42P18(ctx, LineSegment.class, value, "[(0,0),(0,0)]", "{\"[(0,0),(0,0)]\"}");
  }

  @Test
  public void testInferDataTypeBox42P18(TestContext ctx) {
    Box value = new Box();
    testInferDataType42P18(ctx, Box.class, value, "(0,0),(0,0)");
  }

  @Test
  public void testInferDataTypePath42P18(TestContext ctx) {
    Path value = new Path().addPoint(new Point());
    testInferDataType42P18(ctx, Path.class, value, "((0,0))", "{\"((0,0))\"}");
  }

  @Test
  public void testInferDataTypePolygon42P18(TestContext ctx) {
    Polygon value = new Polygon().addPoint(new Point()).addPoint(new Point()).addPoint(new Point());
    testInferDataType42P18(ctx, Polygon.class, value, "((0,0),(0,0),(0,0))", "{\"((0,0),(0,0),(0,0))\"}");
  }

  @Test
  public void testInferDataTypeCircle42P18(TestContext ctx) {
    Circle value = new Circle();
    testInferDataType42P18(ctx, Circle.class, value, "<(0,0),0>", "{\"<(0,0),0>\"}");
  }

  private <T> void testInferDataType42P18(TestContext ctx, Class<T> type, T value, String suffix) {
    testInferDataType42P18(ctx, type, value, suffix, "{" + suffix + "}");
  }

  private <T> void testInferDataType42P18(TestContext ctx, Class<T> type, T value, String suffix1, String suffix2) {
    PgConnection.connect(vertx, options()).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT CONCAT('HELLO ', $1)").execute(Tuple.of(value))
        .map(rows -> rows.iterator().next().getString(0))
        .eventually(() -> conn.close())
        .onComplete(ctx.asyncAssertSuccess(str -> {
          ctx.assertEquals("HELLO " + suffix1, str);
        }));
    }));
    Object array = Array.newInstance(type, 1);
    Array.set(array, 0, value);
    PgConnection.connect(vertx, options()).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT CONCAT('HELLO ', $1)").execute(Tuple.of(array))
        .map(rows -> rows.iterator().next().getString(0))
        .eventually(() -> conn.close())
        .onComplete(ctx.asyncAssertSuccess(str -> {
          ctx.assertEquals("HELLO " + suffix2, str);
        }));
    }));
    PgConnection.connect(vertx, options()).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.begin()
        .flatMap(tx -> conn.preparedQuery("SELECT CONCAT('HELLO ', $1)").execute(Tuple.of(value))
          .eventually(() -> conn.close())
          .onComplete(ctx.asyncAssertFailure(failure -> {
            if (!hasSqlstateCode(failure, "42P18")) {
              ctx.fail(failure);
            }
          })));
    }));
  }

  @Test
  public void testInferDataTypeLazy42P18(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn
        .prepare("SELECT CONCAT('HELLO', $1)")
        .compose(ps -> ps.query().execute(Tuple.of("__")))
        .map(result -> {
          Row row = result.iterator().next();
          ctx.assertEquals("HELLO__", row.getString(0));
          return "";
        })
        .eventually(v -> conn.close());
    }));
  }

  @Test
  public void testInferDataTypeFailure42P18(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT CONCAT('HELLO', $1)")
        .execute(Tuple.of(null))
        .eventually(v -> conn.close())
        .onComplete(ctx.asyncAssertFailure());
    }));
  }

  @Test
  public void testInferDataTypeLazyFailure42P18(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn
        .prepare("SELECT CONCAT('HELLO', $1)")
        .compose(ps -> ps.query().execute(Tuple.of(null)))
        .eventually(v -> conn.close())
        .onComplete(ctx.asyncAssertFailure());
    }));
  }

  @Test
  public void testInferDataTypeLazy42804(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn
        .prepare("SELECT to_jsonb($1)")
        .compose(ps -> ps.query().execute(Tuple.of("foo")))
        .map(result -> {
          ctx.assertEquals("foo", result.iterator().next().getString(0));
          return "";
        })
        .eventually(v -> conn.close())
        .onComplete(ctx.asyncAssertSuccess());
    }));
  }

  @Test
  public void testInferDataTypeLazy42P08(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn
        .prepare("UPDATE Fortune SET message=$2 WHERE id=$1 AND (Fortune.*) IS DISTINCT FROM ($1, $2)")
        .compose(ps -> ps.query().execute(Tuple.of(9, "Feature: A bug with seniority.")))
        .eventually(v -> conn.close())
        .onComplete(ctx.asyncAssertSuccess());
    }));
  }
}

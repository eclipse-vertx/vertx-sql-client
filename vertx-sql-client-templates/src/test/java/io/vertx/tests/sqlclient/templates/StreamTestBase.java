package io.vertx.tests.sqlclient.templates;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.templates.SqlTemplate;
import io.vertx.sqlclient.templates.SqlTemplateStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(VertxUnitRunner.class)
public abstract class StreamTestBase {

  protected Vertx vertx;
  protected SqlConnection connection;

  protected abstract Future<SqlConnection> connect(Vertx vertx);

  protected abstract String createTableSql();

  protected abstract String selectSingleRowSql();

  protected abstract String selectTwoColumnsSql();

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    Async async = ctx.async();
    connect(vertx).onComplete(ctx.asyncAssertSuccess(conn -> {
      connection = conn;
      async.complete();
    }));
    async.await(10000);
  }

  @After
  public void teardown(TestContext ctx) {
    if (connection != null) {
      connection.close();
    }
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testStream(TestContext ctx) {
    Async async = ctx.async();
    connection.begin().onComplete(ctx.asyncAssertSuccess(tx -> {
      SqlTemplateStream
        .forStream(connection, selectSingleRowSql(), 1)
        .execute(Collections.singletonMap("id", 1))
        .onComplete(ctx.asyncAssertSuccess(stream -> {
          List<Row> rows = new ArrayList<>();
          stream.handler(rows::add);
          stream.endHandler(v -> {
            ctx.assertEquals(1, rows.size());
            ctx.assertEquals(1, rows.get(0).getInteger("id"));
            tx.rollback().onComplete(ctx.asyncAssertSuccess(v2 -> async.complete()));
          });
          stream.exceptionHandler(ctx::fail);
        }));
    }));
    async.await(10000);
  }

  @Test
  public void testStreamWithMapTo(TestContext ctx) {
    Async async = ctx.async();
    connection.begin().onComplete(ctx.asyncAssertSuccess(tx -> {
      SqlTemplateStream
        .forStream(connection, selectTwoColumnsSql(), 1)
        .mapTo(World.class)
        .execute(Map.of("id", 1, "randomnumber", 10))
        .onComplete(ctx.asyncAssertSuccess(stream -> {
          List<World> worlds = new ArrayList<>();
          stream.handler(worlds::add);
          stream.endHandler(v -> {
            ctx.assertEquals(1, worlds.size());
            ctx.assertEquals(1, worlds.get(0).id);
            ctx.assertEquals(10, worlds.get(0).randomnumber);
            tx.rollback().onComplete(ctx.asyncAssertSuccess(v2 -> async.complete()));
          });
          stream.exceptionHandler(ctx::fail);
        }));
    }));
    async.await(10000);
  }

  @Test
  public void testStreamWithRowMapper(TestContext ctx) {
    Async async = ctx.async();
    connection.begin().onComplete(ctx.asyncAssertSuccess(tx -> {
      SqlTemplateStream
        .forStream(connection, selectTwoColumnsSql(), 1)
        .mapTo(row -> new World(row.getInteger("id"), row.getInteger("randomnumber")))
        .execute(Map.of("id", 1, "randomnumber", 10))
        .onComplete(ctx.asyncAssertSuccess(stream -> {
          List<World> worlds = new ArrayList<>();
          stream.handler(worlds::add);
          stream.endHandler(v -> {
            ctx.assertEquals(1, worlds.size());
            ctx.assertEquals(1, worlds.get(0).id);
            ctx.assertEquals(10, worlds.get(0).randomnumber);
            tx.rollback().onComplete(ctx.asyncAssertSuccess(v2 -> async.complete()));
          });
          stream.exceptionHandler(ctx::fail);
        }));
    }));
    async.await(10000);
  }

  @Test
  public void testStreamMultipleRows(TestContext ctx) {
    Async async = ctx.async();
    connection.begin().onComplete(ctx.asyncAssertSuccess(tx -> {
      connection.query("DROP TABLE IF EXISTS test_stream").execute().onComplete(ctx.asyncAssertSuccess(d -> {
        connection.query(createTableSql()).execute().onComplete(ctx.asyncAssertSuccess(c -> {
          connection.query("INSERT INTO test_stream VALUES (1),(2),(3),(4),(5)").execute().onComplete(ctx.asyncAssertSuccess(i -> {
            SqlTemplateStream
              .forStream(connection, "SELECT * FROM test_stream WHERE id > #{minId}", 2)
              .mapTo(row -> row.getInteger("id"))
              .execute(Collections.singletonMap("minId", 0))
              .onComplete(ctx.asyncAssertSuccess(stream -> {
                List<Integer> ids = new ArrayList<>();
                stream.handler(ids::add);
                stream.endHandler(v -> {
                  ctx.assertEquals(5, ids.size());
                  tx.rollback().onComplete(ctx.asyncAssertSuccess(v2 -> async.complete()));
                });
                stream.exceptionHandler(ctx::fail);
              }));
          }));
        }));
      }));
    }));
    async.await(10000);
  }

  @Test
  public void testCursor(TestContext ctx) {
    Async async = ctx.async();
    connection.begin().onComplete(ctx.asyncAssertSuccess(tx -> {
      SqlTemplate
        .forCursor(connection, selectSingleRowSql())
        .execute(Collections.singletonMap("id", 1))
        .onComplete(ctx.asyncAssertSuccess(cursor -> {
          cursor.read(10).onComplete(ctx.asyncAssertSuccess(rows -> {
            ctx.assertEquals(1, rows.size());
            ctx.assertEquals(1, rows.iterator().next().getInteger("id"));
            ctx.assertFalse(cursor.hasMore());
            cursor.close().onComplete(ctx.asyncAssertSuccess(v ->
              tx.rollback().onComplete(ctx.asyncAssertSuccess(v2 -> async.complete()))
            ));
          }));
        }));
    }));
    async.await(10000);
  }

  @Test
  public void testCursorMultipleRows(TestContext ctx) {
    Async async = ctx.async();
    connection.begin().onComplete(ctx.asyncAssertSuccess(tx -> {
      connection.query("DROP TABLE IF EXISTS test_cursor").execute().onComplete(ctx.asyncAssertSuccess(d -> {
        connection.query(createTableSql().replace("test_stream", "test_cursor")).execute().onComplete(ctx.asyncAssertSuccess(c -> {
          connection.query("INSERT INTO test_cursor VALUES (1),(2),(3),(4),(5)").execute().onComplete(ctx.asyncAssertSuccess(i -> {
            SqlTemplate
              .forCursor(connection, "SELECT * FROM test_cursor WHERE id > #{minId}")
              .execute(Collections.singletonMap("minId", 0))
              .onComplete(ctx.asyncAssertSuccess(cursor -> {
                cursor.read(2).onComplete(ctx.asyncAssertSuccess(rows1 -> {
                  ctx.assertEquals(2, rows1.size());
                  ctx.assertTrue(cursor.hasMore());
                  cursor.read(2).onComplete(ctx.asyncAssertSuccess(rows2 -> {
                    ctx.assertEquals(2, rows2.size());
                    ctx.assertTrue(cursor.hasMore());
                    cursor.read(2).onComplete(ctx.asyncAssertSuccess(rows3 -> {
                      ctx.assertEquals(1, rows3.size());
                      ctx.assertFalse(cursor.hasMore());
                      cursor.close().onComplete(ctx.asyncAssertSuccess(v ->
                        tx.rollback().onComplete(ctx.asyncAssertSuccess(v2 -> async.complete()))
                      ));
                    }));
                  }));
                }));
              }));
          }));
        }));
      }));
    }));
    async.await(10000);
  }
}

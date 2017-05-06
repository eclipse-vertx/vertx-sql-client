package io.vertx.pgclient;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.V9_5_0;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public class PipeliningTest {

  private static final String LIST_TABLES = "SELECT table_schema,table_name FROM information_schema.tables ORDER BY table_schema,table_name";
  private static final String CURRENT_DB = "SELECT current_database()";

  private static PostgresProcess process;
  private Vertx vertx;
  private static PostgresClientOptions options = new PostgresClientOptions();

  @BeforeClass
  public static void startPg() throws Exception {
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
    PostgresConfig config = new PostgresConfig(V9_5_0, new AbstractPostgresConfig.Net(),
      new AbstractPostgresConfig.Storage("postgres"), new AbstractPostgresConfig.Timeout(),
      new AbstractPostgresConfig.Credentials("postgres", "postgres"));
    PostgresExecutable exec = runtime.prepare(config);
    process = exec.start();
    // File f1 = new File("src/test/resources/create-postgres-database.sql");
    // process.importFromFile(f1);
    File f2 = new File("src/test/resources/create-postgres.sql");
    process.importFromFile(f2);
    options.setHost(process.getConfig().net().host());
    options.setPort(process.getConfig().net().port());
    options.setUsername("postgres");
    options.setPassword("postgres");
    options.setDatabase("postgres");
  }

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }


  @Test
  public void testConnect(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      async.complete();
    }));
  }

  @Test
  public void testQuery(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(10000, result.size());
        for (int i = 0;i < 10000;i++) {
          ctx.assertEquals(2, result.get(i).size());
          ctx.assertTrue(result.get(i).get(0) instanceof Integer);
          ctx.assertTrue(result.get(i).get(1) instanceof Integer);
        }
        async.complete();
      }));
    }));
  }

  @Test
  public void testQueueQueries(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num + 1);
    PostgresClient client = PostgresClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      for (int i = 0;i < num;i++) {
        conn.execute("SELECT id, randomnumber from WORLD", ar -> {
          if (ar.succeeded()) {
            Result result = ar.result();
            ctx.assertEquals(0, result.getUpdatedRows());
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
  public void testQueryError(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(err -> {
        async.complete();
      }));
    }));
  }

  @Test
  public void testUpdate(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.execute("UPDATE world SET randomnumber = 10 WHERE id = 0", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getUpdatedRows());
        ctx.assertEquals(0, result.size());
        async.complete();
      }));
    }));
  }

  @Test
  public void testClose(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.closeHandler(v -> {
        async.complete();
      });
      conn.close();
    }));
  }

  @Test
  public void testCloseWithQueryInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    PostgresClient client = PostgresClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(2, async.count());
        ctx.assertEquals(0, result.getUpdatedRows());
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
  public void testCloseWithErrorInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    PostgresClient client = PostgresClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(err -> {
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
  public void testPool(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PostgresClient client = PostgresClient.create(vertx, options);
    PostgresConnectionPool pool = client.createPool(4);
    for (int i = 0;i < num;i++) {
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        conn.execute("SELECT id, randomnumber from WORLD", ar -> {
          if (ar.succeeded()) {
            Result result = ar.result();
            ctx.assertEquals(0, result.getUpdatedRows());
            ctx.assertEquals(10000, result.size());
          } else {
            ctx.assertEquals("closed", ar.cause().getMessage());
          }
          conn.close();
          async.countDown();
        });
      }));
    }
  }

  @Test
  public void testTx(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.execute("BEGIN", ctx.asyncAssertSuccess(result1 -> {
        ctx.assertEquals(0, result1.getUpdatedRows());
        ctx.assertEquals(0, result1.size());
        conn.execute("COMMIT", ctx.asyncAssertSuccess(result2 -> {
          async.complete();
        }));
      }));
    }));
  }

/*
  @Test
  public void testServerUpdate(TestContext ctx) {

    ctx.async();

    PostgresClient client = PostgresClient.create(vertx, options);
    PostgresConnectionPool pool = client.createPool(1);
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      new Update(pool).handle(req);
    });
    server.listen(8080, ctx.asyncAssertSuccess());


  }

  class Update {

    boolean failed;
    JsonArray worlds = new JsonArray();
    PostgresConnectionPool pool;

    public Update(PostgresConnectionPool pool) {
      this.pool = pool;
    }

    public void handle(HttpServerRequest req) {
      HttpServerResponse resp = req.response();
      final int queries = getQueries(req);

      pool.getConnection(ar1 -> {
        if (ar1.succeeded()) {
          PostgresConnection conn = ar1.result();

          int[] ids = new int[queries];
          Row[] rows = new Row[queries];
          for (int i = 0; i < queries; i++) {
            int index = i;
            int id = randomWorld();
            ids[i] = id;
            conn.execute("SELECT id, randomnumber from WORLD where id = " + id, ar2 -> {
              if (!failed) {
                if (ar2.failed()) {
                  failed = true;
                  resp.setStatusCode(500).end(ar2.cause().getMessage());
                  conn.close();
                  return;
                }
                rows[index] = ar2.result().get(0);
              }
            });
          }

          conn.execute("BEGIN", ar2 -> {
            if (!failed) {
              if (ar2.failed()) {
                failed = true;
                resp.setStatusCode(500).end(ar2.cause().getMessage());
                conn.close();
              }

              for (int i = 0;i < queries;i++) {
                int index = i;
                int randomNumber = randomWorld();

                conn.execute("UPDATE world SET randomnumber = " + randomNumber + " WHERE id = " + ids[i], ar4 -> {
                  if (!failed) {
                    if (ar4.failed()) {
                      failed = true;
                      resp.setStatusCode(500).end(ar4.cause().getMessage());
                      conn.close();
                      return;
                    }
                    Row row = rows[index];
                    worlds.add(new JsonObject().put("id", "" + row.get(0)).put("randomNumber", "" + randomNumber));
                  }
                });
              }

              conn.execute("COMMIT", ar5 -> {
                if (!failed) {
                  if (ar5.failed()) {
                    failed = true;
                    resp.setStatusCode(500).end(ar5.cause().getMessage());
                    conn.close();
                    return;
                  }
                  conn.close();
                  resp
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(Json.encode(worlds.encode()));
                }
              });
            }
          });
        } else {
          resp.setStatusCode(500).end(ar1.cause().getMessage());
        }
      });
    }

    int getQueries(HttpServerRequest request) {
      String param = request.getParam("queries");

      if (param == null) {
        return 1;
      }
      try {
        int parsedValue = Integer.parseInt(param);
        return Math.min(500, Math.max(1, parsedValue));
      } catch (NumberFormatException e) {
        return 1;
      }
    }

    private int randomWorld() {
      return 1 + ThreadLocalRandom.current().nextInt(10000);
    }
  }
*/

}

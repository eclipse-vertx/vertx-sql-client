package com.julienviet.pgclient;

import io.netty.handler.codec.DecoderException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static com.julienviet.pgclient.codec.formatter.DateTimeFormatter.*;
import static com.julienviet.pgclient.codec.formatter.TimeFormatter.*;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class PgTestBase {

  private static final String LIST_TABLES = "SELECT table_schema,table_name FROM information_schema.tables ORDER BY table_schema,table_name";
  private static final String CURRENT_DB = "SELECT current_database()";

  private static PostgresProcess process;
  static PostgresClientOptions options = new PostgresClientOptions();
  Vertx vertx;
  BiConsumer<PostgresClient, Handler<AsyncResult<PostgresConnection>>> connector;

  public PgTestBase(BiConsumer<PostgresClient, Handler<AsyncResult<PostgresConnection>>> connector) {
    this.connector = connector;
  }

  @BeforeClass
  public static void startPg() throws Exception {
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
    PostgresConfig config = new PostgresConfig(V9_5_0, new AbstractPostgresConfig.Net("localhost", 8081),
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
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidDatabase(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, new PostgresClientOptions(options).setDatabase("blah_db"));
    connector.accept(client, ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("database \"blah_db\" does not exist", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidPassword(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, new PostgresClientOptions(options).setPassword("incorrect"));
    connector.accept(client, ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("password authentication failed for user \"postgres\"", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidUsername(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, new PostgresClientOptions(options).setUsername("vertx"));
    connector.accept(client, ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("password authentication failed for user \"vertx\"", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testQuery(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
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
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
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
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(err -> {
        async.complete();
      }));
    }));
  }

  @Test
  public void testUpdate(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("UPDATE Fortune SET message = 'Whatever' WHERE id = 9", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getUpdatedRows());
        ctx.assertEquals(0, result.size());
        async.complete();
      }));
    }));
  }

  @Test
  public void testInsert(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("INSERT INTO Fortune (id, message) VALUES (13, 'Whatever')", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getUpdatedRows());
        ctx.assertEquals(0, result.size());
        async.complete();
      }));
    }));
  }

  @Test
  public void testDelete(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("DELETE FROM Fortune where id = 6", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getUpdatedRows());
        ctx.assertEquals(0, result.size());
        async.complete();
      }));
    }));
  }

  @Test
  public void testTextNullDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT null", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNull(result.get(0).get(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testTextBoolTrueDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT true", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        ctx.assertEquals(true, result.get(0).get(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testBoolFalseTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT false", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        ctx.assertEquals(false, result.get(0).get(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testInt2TextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 32767::INT2", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        ctx.assertEquals((short)32767, result.get(0).get(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testInt4TextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 2147483647::INT4", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        ctx.assertEquals(2147483647, result.get(0).get(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testInt8TextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 9223372036854775807::INT8", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        ctx.assertEquals(9223372036854775807L, result.get(0).get(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testFloat4TextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 3.4028235E38::FLOAT4", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        ctx.assertEquals(3.4028235E38f, result.get(0).get(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testFloat8TextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 1.7976931348623157E308::FLOAT8", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        ctx.assertEquals(1.7976931348623157E308d, result.get(0).get(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testNumericTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 919.999999999999999999999999999999999999::NUMERIC", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        BigDecimal numeric = (BigDecimal) result.get(0).get(0);
        ctx.assertEquals(new BigDecimal("919.999999999999999999999999999999999999"), numeric);
        async.complete();
      }));
    }));
  }

  @Test
  public void testNameTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 'VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X & VERT.X'::NAME",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(1, result.size());
          ctx.assertNotNull(result.get(0).get(0));
          String name = (String) result.get(0).get(0);
          ctx.assertEquals("VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ", name);
          // must be 63 length
          ctx.assertEquals(63, name.length());
          async.complete();
        }));
    }));
  }

  @Test
  public void testBlankPaddedCharTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 'pgClient'::CHAR(15)", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        String bpchar = (String) result.get(0).get(0);
        ctx.assertEquals("pgClient       ", bpchar);
        ctx.assertEquals(15, bpchar.length());
        async.complete();
      }));
    }));
  }

  @Test
  public void testSingleBlankPaddedCharTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 'V'::CHAR", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        String sbpchar = (String) result.get(0).get(0);
        ctx.assertEquals("V", sbpchar);
        ctx.assertEquals(1, sbpchar.length());
        async.complete();
      }));
    }));
  }

  @Test
  public void testSingleCharTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 'X'::\"char\"", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        char character = (char) result.get(0).get(0);
        ctx.assertEquals('X', character);
        async.complete();
      }));
    }));
  }

  @Test
  public void testVarCharTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 'pgClient'::VARCHAR(15)", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        String varchar = (String) result.get(0).get(0);
        ctx.assertEquals("pgClient", varchar);
        ctx.assertEquals(8, varchar.length());
        async.complete();
      }));
    }));
  }

  @Test
  public void testTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT 'Vert.x PostgreSQL Client'::TEXT", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        String text = (String) result.get(0).get(0);
        ctx.assertEquals("Vert.x PostgreSQL Client", text);
        ctx.assertEquals(24, text.length());
        async.complete();
      }));
    }));
  }

  @Test
  public void testUUIDTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT '50867d3d-0098-4f61-bd31-9309ebf53475'::UUID", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        UUID uuid = (UUID) result.get(0).get(0);
        ctx.assertEquals(UUID.fromString("50867d3d-0098-4f61-bd31-9309ebf53475"), uuid);
        ctx.assertEquals(36, uuid.toString().length());
        async.complete();
      }));
    }));
  }

  @Test
  public void testDateTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT '1981-05-30'::DATE", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        LocalDate localDate = (LocalDate) result.get(0).get(0);
        ctx.assertEquals(LocalDate.parse("1981-05-30"), localDate);
        ctx.assertEquals(10, localDate.toString().length());
        async.complete();
      }));
    }));
  }

  @Test
  public void testTimeTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT '17:55:04.905120'::TIME", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        LocalTime localTime = (LocalTime) result.get(0).get(0);
        ctx.assertEquals(LocalTime.parse("17:55:04.905120"), localTime);
        ctx.assertEquals(15, localTime.toString().length());
        async.complete();
      }));
    }));
  }

  @Test
  public void testTimeTzTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT '17:55:04.90512+03:07'::TIMETZ",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(1, result.size());
          ctx.assertNotNull(result.get(0).get(0));
          OffsetTime offsetTime = (OffsetTime) result.get(0).get(0);
          ctx.assertEquals(OffsetTime.parse("17:55:04.90512+03:07", TIMETZ_FORMAT), offsetTime);
          ctx.assertEquals(21, offsetTime.toString().length());
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimestampTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT '2017-05-14 19:35:58.237666'::TIMESTAMP", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(0, result.getUpdatedRows());
        ctx.assertEquals(1, result.size());
        ctx.assertNotNull(result.get(0).get(0));
        LocalDateTime localDateTime = (LocalDateTime) result.get(0).get(0);
        ctx.assertEquals(LocalDateTime.parse("2017-05-14 19:35:58.237666", TIMESTAMP_FORMAT), localDateTime);
        ctx.assertEquals(26, localDateTime.toString().length());
        async.complete();
      }));
    }));
  }

  @Test
  public void testTimestampTzTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SET TIME ZONE 'UTC'", ctx.asyncAssertSuccess(v -> {
        conn.execute("SELECT '2017-05-14 22:35:58.237666-03'::TIMESTAMPTZ",
          ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(0, result.getUpdatedRows());
            ctx.assertEquals(1, result.size());
            ctx.assertNotNull(result.get(0).get(0));
            OffsetDateTime offsetDateTime = (OffsetDateTime) result.get(0).get(0);
            ctx.assertEquals(OffsetDateTime.parse("2017-05-15 01:35:58.237666+00", TIMESTAMPTZ_FORMAT), offsetDateTime);
            ctx.assertEquals(27, offsetDateTime.toString().length());
            async.complete();
          }));
      }));
    }));
  }

  @Test
  public void testJsonbObjectTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT '{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
          " 3.5, \"object\": {}, \"array\" : []}'::JSONB",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(1, result.size());
          ctx.assertNotNull(result.get(0).get(0));
          JsonObject jsonObject = (JsonObject) result.get(0).get(0);
          ctx.assertEquals(new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
            " 3.5, \"object\": {}, \"array\" : []}"), jsonObject);
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonbArrayTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT '[1,true,null,9.5,\"Hi\"]'::JSONB",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(1, result.size());
          ctx.assertNotNull(result.get(0).get(0));
          JsonArray jsonArray = (JsonArray) result.get(0).get(0);
          ctx.assertEquals(new JsonArray("[1,true,null,9.5,\"Hi\"]"), jsonArray);
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonObjectTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT '{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
          " 3.5, \"object\": {}, \"array\" : []}'::JSON",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(1, result.size());
          ctx.assertNotNull(result.get(0).get(0));
          JsonObject jsonObject = (JsonObject) result.get(0).get(0);
          ctx.assertEquals(new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
            " 3.5, \"object\": {}, \"array\" : []}"), jsonObject);
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonArrayTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.execute("SELECT '[1,true,null,9.5,\"Hi\"]'::JSON",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(1, result.size());
          ctx.assertNotNull(result.get(0).get(0));
          JsonArray jsonArray = (JsonArray) result.get(0).get(0);
          ctx.assertEquals(new JsonArray("[1,true,null,9.5,\"Hi\"]"), jsonArray);
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedSelect(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("SELECT id, message FROM Fortune WHERE id=$1 AND message =$2",
        5,
        "A computer program does what you tell it to do, not what you want it to do.",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(1, result.size());
          ctx.assertNotNull(result.get(0).get(0));
          ctx.assertNotNull(result.get(0).get(1));
          ctx.assertEquals(5, result.get(0).get(0));
          ctx.assertEquals("A computer program does what you tell it to do, not what you want it to do.",
            result.get(0).get(1));
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedInsert(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("INSERT INTO Fortune (id, message) VALUES ($1, $2)",
        20, "Hello",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdatedRows());
          ctx.assertEquals(0, result.size());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedUpdate(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("UPDATE Fortune SET message = $1 WHERE id = $2",
        "Whatever",
        20,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdatedRows());
          ctx.assertEquals(0, result.size());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedDelete(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("DELETE FROM Fortune where id = $1", 7,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdatedRows());
          ctx.assertEquals(0, result.size());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam1(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("SELECT * FROM Fortune WHERE id=$1", 1,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(1, result.size());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam2(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("SELECT * FROM Fortune WHERE id=$1 OR id=$2", 1, 8,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(2, result.size());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam3(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3", 1, 8, 4,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(3, result.size());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam4(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4", 1, 8, 4, 11,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(4, result.size());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam5(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5", 1, 8, 4, 11, 2,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(5, result.size());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam6(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6",
        1, 8, 4, 11, 2, 9,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(6, result.size());
          async.complete();
        }));
    }));
  }

  @Test
  public void testBatchSelect(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      PostgresBatch batch = PostgresBatch.batch();
      batch.add(5, "A computer program does what you tell it to do, not what you want it to do.");
      batch.add(5, "A computer program does what you tell it to do, not what you want it to do.");
      batch.add(5, "A computer program does what you tell it to do, not what you want it to do.");
      PreparedStatement prepared = conn.prepare("SELECT id, message FROM Fortune WHERE id=$1 AND message =$2");
      prepared.execute(batch, ctx.asyncAssertSuccess(results -> {
        ctx.assertEquals(3, results.size());
        for (int i = 0;i < 3;i++) {
          Result result = results.get(i);
          ctx.assertEquals(0, result.getUpdatedRows());
          ctx.assertEquals(1, result.size());
          ctx.assertNotNull(result.get(0).get(0));
          ctx.assertNotNull(result.get(0).get(1));
          ctx.assertEquals(5, result.get(0).get(0));
          ctx.assertEquals("A computer program does what you tell it to do, not what you want it to do.",
            result.get(0).get(1));
        }
        prepared.close(ctx.asyncAssertSuccess(result -> {
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testClose(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.closeHandler(v -> {
        async.complete();
      });
      conn.close();
    }));
  }

  @Test
  public void testDisconnectAbruptly(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      vertx.setTimer(200, id -> {
        conn.close();
      });
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      PostgresClient client = PostgresClient.create(vertx, new PostgresClientOptions(options)
        .setPort(8080).setHost("localhost"));
      connector.accept(client, ctx.asyncAssertSuccess(conn -> {
        conn.closeHandler(v2 -> {
          async.complete();
        });
      }));
    }));
  }

  @Test
  public void testProtocolError(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    CompletableFuture<Void> connected = new CompletableFuture<>();
    proxy.proxyHandler(conn -> {
      connected.thenAccept(v -> {
        System.out.println("send bogus");
        Buffer bogusMsg = Buffer.buffer();
        bogusMsg.appendByte((byte) 'R'); // Authentication
        bogusMsg.appendInt(0);
        bogusMsg.appendInt(1);
        bogusMsg.setInt(1, bogusMsg.length() - 1);
        conn.clientSocket().write(bogusMsg);
      });
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      PostgresClient client = PostgresClient.create(vertx, new PostgresClientOptions(options)
        .setPort(8080).setHost("localhost"));
      connector.accept(client, ctx.asyncAssertSuccess(conn -> {
        AtomicInteger count = new AtomicInteger();
        conn.exceptionHandler(err -> {
          ctx.assertEquals(err.getClass(), DecoderException.class);
          count.incrementAndGet();
        });
        conn.closeHandler(v -> {
          ctx.assertEquals(1, count.get());
          async.complete();
        });
        connected.complete(null);
      }));
    }));
  }

  @Test
  public void testCloseWithQueryInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
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
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
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
  public void testTx(TestContext ctx) {
    Async async = ctx.async();
    PostgresClient client = PostgresClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
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

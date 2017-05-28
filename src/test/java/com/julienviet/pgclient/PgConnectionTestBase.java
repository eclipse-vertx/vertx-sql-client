package com.julienviet.pgclient;

import io.netty.handler.codec.DecoderException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.TransactionIsolation;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.*;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

@RunWith(VertxUnitRunner.class)
public abstract class PgConnectionTestBase extends PgTestBase {

  private static final String LIST_TABLES = "SELECT table_schema,table_name FROM information_schema.tables ORDER BY table_schema,table_name";
  private static final String CURRENT_DB = "SELECT current_database()";

  Vertx vertx;
  BiConsumer<PgClient, Handler<AsyncResult<PgConnection>>> connector;

  public PgConnectionTestBase(BiConsumer<PgClient, Handler<AsyncResult<PgConnection>>> connector) {
    this.connector = connector;
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
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidDatabase(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, new PgClientOptions(options).setDatabase("blah_db"));
    connector.accept(client, ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("database \"blah_db\" does not exist", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidPassword(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, new PgClientOptions(options).setPassword("incorrect"));
    connector.accept(client, ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("password authentication failed for user \"postgres\"", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidUsername(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, new PgClientOptions(options).setUsername("vertx"));
    connector.accept(client, ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("password authentication failed for user \"vertx\"", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testQuery(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(10000, result.getNumRows());
        for (int i = 0; i < 10000; i++) {
          ctx.assertEquals(2, result.getResults().get(i).size());
          ctx.assertTrue(result.getResults().get(i).getValue(0) instanceof Integer);
          ctx.assertTrue(result.getResults().get(i).getValue(1) instanceof Integer);
        }
        async.complete();
      }));
    }));
  }

  @Test
  public void testMultipleQuery(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, randomnumber from WORLD LIMIT 1;SELECT id, randomnumber from WORLD LIMIT 1", ctx.asyncAssertSuccess(result -> {
        for (int i = 0;i < 2;i++) {
          ctx.assertEquals(1, result.getNumRows());
          for (int j = 0; j < 1; j++) {
            ctx.assertEquals(2, result.getResults().get(j).size());
            ctx.assertTrue(result.getResults().get(j).getValue(0) instanceof Integer);
            ctx.assertTrue(result.getResults().get(j).getValue(1) instanceof Integer);
          }
          result = result.getNext();
        }
        ctx.assertNull(result);
        async.complete();
      }));
    }));
  }

  @Test
  public void testQueueQueries(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num + 1);
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      for (int i = 0;i < num;i++) {
        conn.query("SELECT id, randomnumber from WORLD", ar -> {
          if (ar.succeeded()) {
            ResultSet result = ar.result();
            ctx.assertEquals(10000, result.getNumRows());
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
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(err -> {
        async.complete();
      }));
    }));
  }

  @Test
  public void testUpdate(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.update("UPDATE Fortune SET message = 'Whatever' WHERE id = 9", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getUpdated());
        async.complete();
      }));
    }));
  }

  @Test
  public void testInsert(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.update("INSERT INTO Fortune (id, message) VALUES (13, 'Whatever')", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getUpdated());
        async.complete();
      }));
    }));
  }

  @Test
  public void testDelete(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.update("DELETE FROM Fortune where id = 6", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getUpdated());
        async.complete();
      }));
    }));
  }

  @Test
  public void testTextNullDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT null", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertNull(result.getResults().get(0).getValue(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testTextBoolTrueDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT true", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals(true, result.getResults().get(0).getBoolean(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testBoolFalseTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT false", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals(false, result.getResults().get(0).getBoolean(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testInt2TextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 32767::INT2", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals((short)32767, result.getResults().get(0).getValue(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testInt4TextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 2147483647::INT4", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals(2147483647, result.getResults().get(0).getInteger(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testInt8TextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 9223372036854775807::INT8", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals(9223372036854775807L, result.getResults().get(0).getLong(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testFloat4TextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 3.4028235E38::FLOAT4", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals(3.4028235E38f, result.getResults().get(0).getFloat(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testFloat8TextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 1.7976931348623157E308::FLOAT8", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals(1.7976931348623157E308d, result.getResults().get(0).getDouble(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testNumericTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 919.999999999999999999999999999999999999::NUMERIC", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals(920.0, result.getResults().get(0).getDouble(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testNameTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 'VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X & VERT.X'::NAME",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          String name = result.getResults().get(0).getString(0);
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
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 'pgClient'::CHAR(15)", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        String bpchar = result.getResults().get(0).getString(0);
        ctx.assertEquals("pgClient       ", bpchar);
        ctx.assertEquals(15, bpchar.length());
        async.complete();
      }));
    }));
  }

  @Test
  public void testSingleBlankPaddedCharTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 'V'::CHAR", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        String sbpchar = result.getResults().get(0).getString(0);
        ctx.assertEquals("V", sbpchar);
        ctx.assertEquals(1, sbpchar.length());
        async.complete();
      }));
    }));
  }

  @Test
  public void testSingleCharTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 'X'::\"char\"", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        char character = (char) result.getResults().get(0).getValue(0);
        ctx.assertEquals('X', character);
        async.complete();
      }));
    }));
  }

  @Test
  public void testVarCharTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 'pgClient'::VARCHAR(15)", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals("pgClient", result.getResults().get(0).getString(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 'Vert.x PostgreSQL Client'::TEXT", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals("Vert.x PostgreSQL Client", result.getResults().get(0).getString(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testUUIDTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '50867d3d-0098-4f61-bd31-9309ebf53475'::UUID", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals("50867d3d-0098-4f61-bd31-9309ebf53475", result.getResults().get(0).getString(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testDateTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '1981-05-30'::DATE", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals("1981-05-30", result.getResults().get(0).getString(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testTimeTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '17:55:04.90512'::TIME", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals("17:55:04.90512", result.getResults().get(0).getString(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testTimeTzTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '17:55:04.90512+03:07'::TIMETZ",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals("17:55:04.905120+03:07", result.getResults().get(0).getString(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimestampTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '2017-05-14 19:35:58.237666'::TIMESTAMP", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        ctx.assertEquals(Instant.parse("2017-05-14T19:35:58.237666Z"), result.getResults().get(0).getInstant(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testTimestampTzTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'", ctx.asyncAssertSuccess(v -> {
        conn.query("SELECT '2017-05-14 22:35:58.237666-03'::TIMESTAMPTZ",
          ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.getNumRows());
            ctx.assertEquals(Instant.parse("2017-05-15T01:35:58.237666Z"), result.getResults().get(0).getInstant(0));
            async.complete();
          }));
      }));
    }));
  }

  @Test
  public void testJsonbObjectTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
          " 3.5, \"object\": {}, \"array\" : []}'::JSONB",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          JsonObject jsonObject = result.getResults().get(0).getJsonObject(0);
          ctx.assertEquals(new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
            " 3.5, \"object\": {}, \"array\" : []}"), jsonObject);
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonbArrayTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '[1,true,null,9.5,\"Hi\"]'::JSONB",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          JsonArray jsonArray = result.getResults().get(0).getJsonArray(0);
          ctx.assertEquals(new JsonArray("[1,true,null,9.5,\"Hi\"]"), jsonArray);
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonObjectTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
          " 3.5, \"object\": {}, \"array\" : []}'::JSON",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          JsonObject jsonObject = result.getResults().get(0).getJsonObject(0);
          ctx.assertEquals(new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
            " 3.5, \"object\": {}, \"array\" : []}"), jsonObject);
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonArrayTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '[1,true,null,9.5,\"Hi\"]'::JSON",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          JsonArray jsonArray = result.getResults().get(0).getJsonArray(0);
          ctx.assertEquals(new JsonArray("[1,true,null,9.5,\"Hi\"]"), jsonArray);
          async.complete();
        }));
    }));
  }

  @Test
  public void testByteaTextDataType(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '12345678910'::BYTEA",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          byte[] bytea = result.getResults().get(0).getBinary(0);
          ctx.assertEquals("12345678910", new String(bytea, UTF_8));
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedSelect(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndQuery("SELECT id, message FROM Fortune WHERE id=$1 AND message =$2",
        5,
        "A computer program does what you tell it to do, not what you want it to do.",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertNotNull(result.getResults().get(0).getValue(0));
          ctx.assertNotNull(result.getResults().get(0).getValue(1));
          ctx.assertEquals(5, result.getResults().get(0).getValue(0));
          ctx.assertEquals("A computer program does what you tell it to do, not what you want it to do.",
            result.getResults().get(0).getValue(1));
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedInsert(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("INSERT INTO Fortune (id, message) VALUES ($1, $2)",
        20, "Hello",
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdated());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedUpdate(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("UPDATE Fortune SET message = $1 WHERE id = $2",
        "Whatever",
        20,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdated());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedDelete(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndExecute("DELETE FROM Fortune where id = $1", 7,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdated());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam1(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndQuery("SELECT * FROM Fortune WHERE id=$1", 1,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam2(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndQuery("SELECT * FROM Fortune WHERE id=$1 OR id=$2", 1, 8,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(2, result.getNumRows());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam3(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndQuery("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3", 1, 8, 4,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(3, result.getNumRows());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam4(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndQuery("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4", 1, 8, 4, 11,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(4, result.getNumRows());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam5(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndQuery("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5", 1, 8, 4, 11, 2,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(5, result.getNumRows());
          async.complete();
        }));
    }));
  }

  @Test
  public void testExtendedParam6(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.prepareAndQuery("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6",
        1, 8, 4, 11, 2, 9,
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(6, result.getNumRows());
          async.complete();
        }));
    }));
  }

  @Test
  public void testPreparedQuery(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      PgPreparedStatement ps = conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6");
      PgQuery query = ps.query(1, 8, 4, 11, 2, 9);
      query.execute(ctx.asyncAssertSuccess(results -> {
        ctx.assertEquals(6, results.getNumRows());
        ps.close(ctx.asyncAssertSuccess(result -> {
          async.complete();
        }));
      }));
    }));
  }

  // Need to test partial query close or abortion ?
  @Test
  public void testPreparedPartialQuery(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(v -> {
        PgPreparedStatement ps = conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6");
        PgQuery query = ps.query(1, 8, 4, 11, 2, 9);
        query.fetch(4);
        query.execute(ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(4, results.getNumRows());
          ctx.assertFalse(results.isComplete());
          query.execute(ctx.asyncAssertSuccess(results2 -> {
            ctx.assertNotNull(results2.getColumnNames());
            ctx.assertEquals(2, results2.getNumRows());
            ctx.assertTrue(results2.isComplete());
            ps.close(ctx.asyncAssertSuccess(v2 -> {
              async.complete();
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testBatchUpdate(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      PgPreparedStatement ps = conn.prepare("UPDATE Fortune SET message=$1 WHERE id=$2");
      PgBatch batch = ps.batch();
      batch.add("val0", 1);
      batch.add("val1", 2);
      batch.execute(ctx.asyncAssertSuccess(results -> {
        ctx.assertEquals(2, results.size());
        for (int i = 0;i < 2;i++) {
          UpdateResult result = results.get(i);
          ctx.assertEquals(1, result.getUpdated());
        }
        ps.close(ctx.asyncAssertSuccess(result -> {
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testClose(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
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
      PgClient client = PgClient.create(vertx, new PgClientOptions(options)
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
      PgClient client = PgClient.create(vertx, new PgClientOptions(options)
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
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(2, async.count());
        ctx.assertEquals(10000, result.getNumRows());
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
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
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
  public void testTx(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    connector.accept(client, ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(result1 -> {
        ctx.assertNull(result1);
        conn.query("COMMIT", ctx.asyncAssertSuccess(result2 -> {
          ctx.assertNull(result2);
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testSQLConnection(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.getConnection(c -> {
      SQLConnection conn = c.result();
      conn.query("SELECT 1", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        async.complete();
      }));
    });
  }

  @Test
  public void testSelectForQueryWithParams(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.getConnection(c -> {
      SQLConnection conn = c.result();
      conn.queryWithParams("SELECT * FROM Fortune WHERE id=$1", new JsonArray().add(1) ,
        ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.getNumRows());
        async.complete();
      }));
    });
  }

  @Test
  public void testInsertForUpdateWithParams(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.getConnection(c -> {
      SQLConnection conn = c.result();
      conn.updateWithParams("INSERT INTO Fortune (id, message) VALUES ($1, $2)", new JsonArray().add(1234).add("Yes!"),
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdated());
          async.complete();
        }));
    });
  }

  @Test
  public void testUpdateForUpdateWithParams(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.getConnection(c -> {
      SQLConnection conn = c.result();
      conn.updateWithParams("UPDATE Fortune SET message = $1 WHERE id = $2", new JsonArray().add("Hello").add(1),
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdated());
          async.complete();
        }));
    });
  }

  @Test
  public void testDeleteForUpdateWithParams(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.getConnection(c -> {
      SQLConnection conn = c.result();
      conn.updateWithParams("DELETE FROM Fortune WHERE id = $1", new JsonArray().add(3),
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdated());
          async.complete();
        }));
    });
  }

  @Test
  public void testGetDefaultTx(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.getConnection(c -> {
      SQLConnection conn = c.result();
      conn.getTransactionIsolation(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(TransactionIsolation.READ_COMMITTED, result);
        async.complete();
      }));
    });
  }

  @Test
  public void testSetUnsupportedTx(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.getConnection(c -> {
      SQLConnection conn = c.result();
      conn.setTransactionIsolation(TransactionIsolation.NONE, ctx.asyncAssertFailure(result -> {
        ctx.assertEquals("None transaction isolation is not supported", result.getMessage());
        async.complete();
      }));
    });
  }

  @Test
  public void testSetAndGetReadUncommittedTx(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.getConnection(c -> {
      SQLConnection conn = c.result();
      conn.setTransactionIsolation(TransactionIsolation.READ_UNCOMMITTED, ctx.asyncAssertSuccess(result -> {
        conn.getTransactionIsolation(ctx.asyncAssertSuccess(res -> {
          ctx.assertEquals(TransactionIsolation.READ_UNCOMMITTED, res);
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testSetAndGetReadCommittedTx(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.getConnection(c -> {
      SQLConnection conn = c.result();
      conn.setTransactionIsolation(TransactionIsolation.READ_COMMITTED, ctx.asyncAssertSuccess(result -> {
        conn.getTransactionIsolation(ctx.asyncAssertSuccess(res -> {
          ctx.assertEquals(TransactionIsolation.READ_COMMITTED, res);
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testSetAndGetRepeatableReadTx(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.getConnection(c -> {
      SQLConnection conn = c.result();
      conn.setTransactionIsolation(TransactionIsolation.REPEATABLE_READ, ctx.asyncAssertSuccess(result -> {
        conn.getTransactionIsolation(ctx.asyncAssertSuccess(res -> {
          ctx.assertEquals(TransactionIsolation.REPEATABLE_READ, res);
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testSetAndGetSerializableTx(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.getConnection(c -> {
      SQLConnection conn = c.result();
      conn.setTransactionIsolation(TransactionIsolation.SERIALIZABLE, ctx.asyncAssertSuccess(result -> {
        conn.getTransactionIsolation(ctx.asyncAssertSuccess(res -> {
          ctx.assertEquals(TransactionIsolation.SERIALIZABLE, res);
          async.complete();
        }));
      }));
    });
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

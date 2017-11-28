package com.julienviet.pgclient;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.time.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class DataTypeTextTest extends DataTypeTestBase {

  @Override
  protected PgClientOptions options() {
    return new PgClientOptions(options).setCachePreparedStatements(false);
  }

  @Test
  public void testNull(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT null")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertNull(result.getResults().get(0).getValue(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testBoolean(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT true, false")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals(true, result.getResults().get(0).getBoolean(0));
          ctx.assertEquals(false, result.getResults().get(0).getBoolean(1));
          async.complete();
        }));
    }));
  }

  @Test
  public void testInt2(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 32767::INT2")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals((short)32767, result.getResults().get(0).getValue(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testInt4(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 2147483647::INT4")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals(2147483647, result.getResults().get(0).getInteger(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testInt8(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 9223372036854775807::INT8")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals(9223372036854775807L, result.getResults().get(0).getLong(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testFloat4(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 3.4028235E38::FLOAT4")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals(3.4028235E38f, result.getResults().get(0).getFloat(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testFloat8(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 1.7976931348623157E308::FLOAT8")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals(1.7976931348623157E308d, result.getResults().get(0).getDouble(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testNumeric(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 919.999999999999999999999999999999999999::NUMERIC")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals(920.0, result.getResults().get(0).getDouble(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testName(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X & VERT.X'::NAME")
        .execute(ctx.asyncAssertSuccess(result ->{
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
  public void testBlankPaddedChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'pgClient'::CHAR(15)")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          String bpchar = result.getResults().get(0).getString(0);
          ctx.assertEquals("pgClient       ", bpchar);
          ctx.assertEquals(15, bpchar.length());
          async.complete();
        }));
    }));
  }

  @Test
  public void testSingleBlankPaddedChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'V'::CHAR")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          String sbpchar = result.getResults().get(0).getString(0);
          ctx.assertEquals("V", sbpchar);
          ctx.assertEquals(1, sbpchar.length());
          async.complete();
        }));
    }));
  }

  @Test
  public void testSingleChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'X'::\"char\"")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          char character = (char) result.getResults().get(0).getValue(0);
          ctx.assertEquals('X', character);
          async.complete();
        }));
    }));
  }

  @Test
  public void testVarChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'pgClient'::VARCHAR(15)")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals("pgClient", result.getResults().get(0).getString(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testText(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'Vert.x PostgreSQL Client'::TEXT")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals("Vert.x PostgreSQL Client", result.getResults().get(0).getString(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testUUID(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '50867d3d-0098-4f61-bd31-9309ebf53475'::UUID")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals("50867d3d-0098-4f61-bd31-9309ebf53475", result.getResults().get(0).getString(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testDate(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '1981-05-30'::DATE")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals("1981-05-30", result.getResults().get(0).getString(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testTime(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '17:55:04.905120'::TIME")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals("17:55:04.905120", result.getResults().get(0).getString(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimeTz(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '17:55:04.90512+03:07'::TIMETZ")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals("17:55:04.905120+03:07", result.getResults().get(0).getString(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimestamp(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '2017-05-14 19:35:58.237666'::TIMESTAMP")
        .execute(ctx.asyncAssertSuccess(result ->{
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals(Instant.parse("2017-05-14T19:35:58.237666Z"), result.getResults().get(0).getInstant(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimestampTz(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'").execute(ctx.asyncAssertSuccess(v -> {
        conn.query("SELECT '2017-05-14 22:35:58.237666-03'::TIMESTAMPTZ").execute(
          ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.getNumRows());
            ctx.assertEquals(Instant.parse("2017-05-15T01:35:58.237666Z"), result.getResults().get(0).getInstant(0));
            async.complete();
          }));
      }));
    }));
  }

  @Test
  public void testJsonbObject(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '  {\"str\":\"blah\", \"int\" : 1, \"float\" :" +
        " 3.5, \"object\": {}, \"array\" : []   }'::JSONB")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          JsonObject jsonObject = result.getResults().get(0).getJsonObject(0);
          ctx.assertEquals(new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
            " 3.5, \"object\": {}, \"array\" : []}"), jsonObject);
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonbArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '  [1,true,null,9.5,\"Hi\" ] '::JSONB").execute(
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          JsonArray jsonArray = result.getResults().get(0).getJsonArray(0);
          ctx.assertEquals(new JsonArray("[1,true,null,9.5,\"Hi\"]"), jsonArray);
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonObject(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '    {\"str\":\"blah\", \"int\" : 1, \"float\" :" +
        " 3.5, \"object\": {}, \"array\" : []  }    '::JSON").execute(
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
  public void testJsonArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '     [1,true,null,9.5,\"Hi\"]     '::JSON").execute(
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          JsonArray jsonArray = result.getResults().get(0).getJsonArray(0);
          ctx.assertEquals(new JsonArray("[1,true,null,9.5,\"Hi\"]"), jsonArray);
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonbScalar(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT ' true '::JSONB, ' false '::JSONB, ' null '::JSONB, ' 7.502 '::JSONB, ' 8 '::JSONB, '\" Really Awesome! \"'::JSONB")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals(true, result.getResults().get(0).getBoolean(0));
          ctx.assertEquals(false, result.getResults().get(0).getBoolean(1));
          ctx.assertNull(result.getResults().get(0).getValue(2));
          ctx.assertEquals(7.502f, result.getResults().get(0).getFloat(3));
          ctx.assertEquals(7.502d, result.getResults().get(0).getDouble(3));
          ctx.assertEquals(8, result.getResults().get(0).getInteger(4));
          ctx.assertEquals(8L, result.getResults().get(0).getLong(4));
          ctx.assertEquals(" Really Awesome! ", result.getResults().get(0).getString(5));
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonScalar(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT ' true '::JSON, ' false '::JSON, ' null '::JSON, ' 7.502 '::JSON, ' 8 '::JSON, '\" Really Awesome! \"'::JSON")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          ctx.assertEquals(true, result.getResults().get(0).getBoolean(0));
          ctx.assertEquals(false, result.getResults().get(0).getBoolean(1));
          ctx.assertNull(result.getResults().get(0).getValue(2));
          ctx.assertEquals(7.502f, result.getResults().get(0).getFloat(3));
          ctx.assertEquals(7.502d, result.getResults().get(0).getDouble(3));
          ctx.assertEquals(8, result.getResults().get(0).getInteger(4));
          ctx.assertEquals(8L, result.getResults().get(0).getLong(4));
          ctx.assertEquals(" Really Awesome! ", result.getResults().get(0).getString(5));
          async.complete();
        }));
    }));
  }

  @Test
  public void testBytea(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '12345678910'::BYTEA, '\u00DE\u00AD\u00BE\u00EF'::BYTEA").execute(
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getNumRows());
          byte[] bytea1 = result.getResults().get(0).getBinary(0);
          byte[] bytea2 = result.getResults().get(0).getBinary(1);
          ctx.assertEquals("12345678910", new String(bytea1, UTF_8));
          ctx.assertEquals("\u00DE\u00AD\u00BE\u00EF", new String(bytea2, UTF_8));
          async.complete();
        }));
    }));
  }

}

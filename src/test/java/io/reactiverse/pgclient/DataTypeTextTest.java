package io.reactiverse.pgclient;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.UUID;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class DataTypeTextTest extends DataTypeTestBase {

  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options).setCachePreparedStatements(false);
  }

  @Test
  public void testNull(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT null \"NullValue\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "NullValue").forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testBoolean(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT true \"TrueValue\", false \"FalseValue\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "TrueValue")
            .returns(Tuple::getValue, Row::getValue, true)
            .returns(Tuple::getBoolean, Row::getBoolean, true)
            .forRow(row);
          ColumnChecker.checkColumn(1, "FalseValue")
            .returns(Tuple::getBoolean, Row::getBoolean, false)
            .returns(Tuple::getValue, Row::getValue, false)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testInt2(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 32767::INT2 \"Short\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Short")
            .returns(Tuple::getValue, Row::getValue, (short) 32767)
            .returns(Tuple::getInteger, Row::getInteger, 32767)
            .returns(Tuple::getLong, Row::getLong, 32767L)
            .returns(Tuple::getFloat, Row::getFloat, 32767f)
            .returns(Tuple::getDouble, Row::getDouble, 32767d)
            .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("32767"))
            .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("32767"))
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testInt4(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 2147483647::INT4 \"Integer\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Integer")
            .returns(Tuple::getInteger, Row::getInteger, 2147483647)
            .returns(Tuple::getValue, Row::getValue, 2147483647)
            .returns(Tuple::getLong, Row::getLong, 2147483647L)
            .returns(Tuple::getFloat, Row::getFloat, 2147483647f)
            .returns(Tuple::getDouble, Row::getDouble, 2147483647D)
            .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("2147483647"))
            .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("2147483647"))
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testInt8(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 9223372036854775807::INT8 \"Long\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Long")
            .returns(Tuple::getValue, Row::getValue, 9223372036854775807L)
            .returns(Tuple::getInteger, Row::getInteger, -1)
            .returns(Tuple::getLong, Row::getLong, 9223372036854775807L)
            .returns(Tuple::getFloat, Row::getFloat, 9223372036854775807f)
            .returns(Tuple::getDouble, Row::getDouble, 9223372036854775807d)
            .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("9223372036854775807"))
            .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("9223372036854775807"))
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testFloat4(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 3.4028235E38::FLOAT4 \"Float\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Float")
            .returns(Tuple::getValue, Row::getValue, 3.4028235E38f)
            .returns(Tuple::getInteger, Row::getInteger, 2147483647)
            .returns(Tuple::getLong, Row::getLong, 9223372036854775807L)
            .returns(Tuple::getFloat, Row::getFloat, 3.4028235E38f)
            .returns(Tuple::getDouble, Row::getDouble, 3.4028234663852886E38d)
            .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("3.4028235E38"))
            .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("3.4028235E38"))
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testFloat8(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 1.7976931348623157E308::FLOAT8 \"Double\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Double")
            .returns(Tuple::getValue, Row::getValue, 1.7976931348623157E308d)
            .returns(Tuple::getInteger, Row::getInteger, 2147483647)
            .returns(Tuple::getLong, Row::getLong, 9223372036854775807L)
            .returns(Tuple::getFloat, Row::getFloat, Float.POSITIVE_INFINITY)
            .returns(Tuple::getDouble, Row::getDouble, 1.7976931348623157E308d)
            .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("1.7976931348623157E308"))
            .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("1.7976931348623157E308"))
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testNumeric(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 919.999999999999999999999999999999999999::NUMERIC \"Numeric\", 'NaN'::NUMERIC \"NaN\"", ctx.asyncAssertSuccess(result -> {
          Numeric numeric = Numeric.parse("919.999999999999999999999999999999999999");
          Numeric nan = Numeric.parse("NaN");
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Numeric")
            .returns(Tuple::getValue, Row::getValue, numeric)
            .returns(Tuple::getInteger, Row::getInteger, 919)
            .returns(Tuple::getLong, Row::getLong, 919L)
            .returns(Tuple::getFloat, Row::getFloat, 920f)
            .returns(Tuple::getDouble, Row::getDouble, 920.0)
            .returns(Tuple::getBigDecimal, Row::getBigDecimal, numeric.bigDecimalValue())
            .returns(Tuple::getNumeric, Row::getNumeric, numeric)
            .forRow(row);
          ColumnChecker.checkColumn(1, "NaN")
            .returns(Tuple::getValue, Row::getValue, nan)
            .returns(Tuple::getInteger, Row::getInteger, 0)
            .returns(Tuple::getLong, Row::getLong, 0L)
            .returns(Tuple::getFloat, Row::getFloat, Float.NaN)
            .returns(Tuple::getDouble, Row::getDouble, Double.NaN)
            .fails(Tuple::getBigDecimal, Row::getBigDecimal)
            .returns(Tuple::getNumeric, Row::getNumeric, nan)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testName(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X & VERT.X'::NAME \"Name\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Name")
            .returns(Tuple::getValue, Row::getValue, "VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ")
            .returns(Tuple::getString, Row::getString, "VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testBlankPaddedChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'pgClient'::CHAR(15) \"Char\" ", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Char")
            .returns(Tuple::getValue, Row::getValue, "pgClient       ")
            .returns(Tuple::getString, Row::getString, "pgClient       ")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testSingleBlankPaddedChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'V'::CHAR \"Char\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Char")
            .returns(Tuple::getValue, Row::getValue, "V")
            .returns(Tuple::getString, Row::getString, "V")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testSingleChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'X'::\"char\" \"Character\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Character")
            .returns(Tuple::getValue, Row::getValue, 'X')
            .returns(Tuple::getCharacter, Row::getCharacter, 'X')
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testVarChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'pgClient'::VARCHAR(15) \"Driver\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Driver")
            .returns(Tuple::getValue, Row::getValue, "pgClient")
            .returns(Tuple::getString, Row::getString, "pgClient")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testText(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'Vert.x PostgreSQL Client'::TEXT \"Text\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Text")
            .returns(Tuple::getValue, Row::getValue, "Vert.x PostgreSQL Client")
            .returns(Tuple::getString, Row::getString, "Vert.x PostgreSQL Client")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testUUID(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '50867d3d-0098-4f61-bd31-9309ebf53475'::UUID \"uuid\"", ctx.asyncAssertSuccess(result -> {
          UUID uuid = UUID.fromString("50867d3d-0098-4f61-bd31-9309ebf53475");
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "uuid")
            .returns(Tuple::getValue, Row::getValue, uuid)
            .returns(Tuple::getUUID, Row::getUUID, uuid)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testDate(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '1981-05-30'::DATE \"LocalDate\"", ctx.asyncAssertSuccess(result -> {
          LocalDate ld = LocalDate.parse("1981-05-30");
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "LocalDate")
            .returns(Tuple::getValue, Row::getValue, ld)
            .returns(Tuple::getLocalDate, Row::getLocalDate, ld)
            .returns(Tuple::getTemporal, Row::getTemporal, ld)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testTime(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '17:55:04.905120'::TIME \"LocalTime\"", ctx.asyncAssertSuccess(result -> {
          LocalTime lt = LocalTime.parse("17:55:04.905120");
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "LocalTime")
            .returns(Tuple::getValue, Row::getValue, lt)
            .returns(Tuple::getLocalTime, Row::getLocalTime, lt)
            .returns(Tuple::getTemporal, Row::getTemporal, lt)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimeTz(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '17:55:04.90512+03:07'::TIMETZ \"OffsetTime\"", ctx.asyncAssertSuccess(result -> {
          OffsetTime ot = OffsetTime.parse("17:55:04.905120+03:07");
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "OffsetTime")
            .returns(Tuple::getOffsetTime, Row::getOffsetTime, ot)
            .returns(Tuple::getTemporal, Row::getTemporal, ot)
            .returns(Tuple::getValue, Row::getValue, ot)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimestamp(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '2017-05-14 19:35:58.237666'::TIMESTAMP \"LocalDateTime\"", ctx.asyncAssertSuccess(result -> {
          LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "LocalDateTime")
            .returns(Tuple::getValue, Row::getValue, ldt)
            .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, ldt)
            .returns(Tuple::getTemporal, Row::getTemporal, ldt)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimestampTz(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'", ctx.asyncAssertSuccess(v -> {
        conn.query("SELECT '2017-05-14 22:35:58.237666-03'::TIMESTAMPTZ \"OffsetDateTime\"", ctx.asyncAssertSuccess(result -> {
          OffsetDateTime odt = OffsetDateTime.parse("2017-05-15T01:35:58.237666Z");
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "OffsetDateTime")
            .returns(Tuple::getValue, Row::getValue, odt)
            .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, odt)
            .returns(Tuple::getTemporal, Row::getTemporal, odt)
            .forRow(row);
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testJSONB(TestContext ctx) {
    testJson(ctx, "JSONB");
  }

  @Test
  public void testJSON(TestContext ctx) {
    testJson(ctx, "JSON");
  }

  private void testJson(TestContext ctx, String type) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT " +
        "'  {\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []   }'::" + type + " \"JsonObject\"," +
        "'  [1,true,null,9.5,\"Hi\" ] '::" + type + " \"JsonArray\"," +
        "' true '::" + type + " \"TrueValue\"," +
        "' false '::" + type + " \"FalseValue\"," +
        "' null '::" + type + " \"NullValue\"," +
        "' 7.502 '::" + type + " \"Number1\"," +
        "' 8 '::" + type + " \"Number2\"," +
        "'\" Really Awesome! \"'::" + type + " \"Text\"", ctx.asyncAssertSuccess(result -> {
        JsonObject object =  new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []}");
        JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ColumnChecker.checkColumn(0, "JsonObject")
          .returns(Tuple::getValue, Row::getValue, Json.create(object))
          .returns(Tuple::getJsonObject, Row::getJsonObject, object)
          .forRow(row);
        ColumnChecker.checkColumn(1, "JsonArray")
          .returns(Tuple::getValue, Row::getValue, Json.create(array))
          .returns(Tuple::getJsonArray, Row::getJsonArray, array)
          .forRow(row);
        ColumnChecker.checkColumn(2, "TrueValue")
          .returns(Tuple::getValue, Row::getValue, Json.create(true))
          .returns(Tuple::getBoolean, Row::getBoolean, true)
          .forRow(row);
        ColumnChecker.checkColumn(3, "FalseValue")
          .returns(Tuple::getValue, Row::getValue, Json.create(false))
          .returns(Tuple::getBoolean, Row::getBoolean, false)
          .forRow(row);
        ColumnChecker.checkColumn(4, "NullValue")
          .returns(Tuple::getValue, Row::getValue, Json.create(null))
          .forRow(row);
        ColumnChecker.checkColumn(5, "Number1")
          .returns(Tuple::getValue, Row::getValue, Json.create(7.502d))
          .returns(Tuple::getInteger, Row::getInteger, 7)
          .returns(Tuple::getLong, Row::getLong, 7L)
          .returns(Tuple::getFloat, Row::getFloat, 7.502f)
          .returns(Tuple::getDouble, Row::getDouble, 7.502d)
          .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("7.502"))
          .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("7.502"))
          .forRow(row);
        ColumnChecker.checkColumn(6, "Number2")
          .returns(Tuple::getValue, Row::getValue, Json.create(8))
          .returns(Tuple::getInteger, Row::getInteger, 8)
          .returns(Tuple::getLong, Row::getLong, 8L)
          .returns(Tuple::getFloat, Row::getFloat, 8f)
          .returns(Tuple::getDouble, Row::getDouble, 8d)
          .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(8))
          .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("8"))
          .forRow(row);
        ColumnChecker.checkColumn(7, "Text")
          .returns(Tuple::getValue, Row::getValue, Json.create(" Really Awesome! "))
          .returns(Tuple::getString, Row::getString, " Really Awesome! ")
          .forRow(row);
        async.complete();
      }));
    }));
  }

  @Test
  public void testBytea(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '12345678910'::BYTEA \"Buffer1\", '\u00DE\u00AD\u00BE\u00EF'::BYTEA \"Buffer2\"", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ColumnChecker.checkColumn(0, "Buffer1")
          .returns(Tuple::getValue, Row::getValue, Buffer.buffer("12345678910"))
          .returns(Tuple::getBuffer, Row::getBuffer, Buffer.buffer("12345678910"))
          .forRow(row);
        ColumnChecker.checkColumn(1, "Buffer2")
          .returns(Tuple::getValue, Row::getValue, Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"))
          .returns(Tuple::getBuffer, Row::getBuffer, Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"))
          .forRow(row);
        async.complete();
      }));
    }));
  }
}

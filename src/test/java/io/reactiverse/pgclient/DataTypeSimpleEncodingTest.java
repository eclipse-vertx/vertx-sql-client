package io.reactiverse.pgclient;

import io.reactiverse.pgclient.data.Interval;
import io.reactiverse.pgclient.data.Json;
import io.reactiverse.pgclient.data.LineSegment;
import io.reactiverse.pgclient.data.Numeric;
import io.reactiverse.pgclient.data.Point;
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
public class DataTypeSimpleEncodingTest extends DataTypeTestBase {

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

  private void testNumber(TestContext ctx, Number[] values, String type) {
    Async async = ctx.async(values.length);
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      for (Number value : values) {
        conn
          .query("SELECT " + value +"::" + type + " \"col\"", ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "col")
              .returns(Tuple::getValue, Row::getValue, value)
              .returns(Tuple::getShort, Row::getShort, value.shortValue())
              .returns(Tuple::getInteger, Row::getInteger, value.intValue())
              .returns(Tuple::getLong, Row::getLong, value.longValue())
              .returns(Tuple::getFloat, Row::getFloat, value.floatValue())
              .returns(Tuple::getDouble, Row::getDouble, value.doubleValue())
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("" + value))
              .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("" + value))
              .forRow(row);
            async.countDown();
          }));
      }
    }));
  }

  @Test
  public void testInt2(TestContext ctx) {
    testNumber(ctx, new Number[] { (short)32767, (short) -1 }, "INT2");
  }

  @Test
  public void testInt4(TestContext ctx) {
    testNumber(ctx, new Number[] { 2147483647, -1 }, "INT4");
  }

  @Test
  public void testInt8(TestContext ctx) {
    testNumber(ctx, new Number[] { 9223372036854775807L, -1L }, "INT8");
  }

  @Test
  public void testFloat4(TestContext ctx) {
    testNumber(ctx, new Number[] { 3.4028235E38f, -1f }, "FLOAT4");
  }

  @Test
  public void testFloat8(TestContext ctx) {
    testNumber(ctx, new Number[] { 1.7976931348623157E308D, -1D }, "FLOAT8");
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
            .returns(Tuple::getShort, Row::getShort, (short) 919)
            .returns(Tuple::getInteger, Row::getInteger, 919)
            .returns(Tuple::getLong, Row::getLong, 919L)
            .returns(Tuple::getFloat, Row::getFloat, 920f)
            .returns(Tuple::getDouble, Row::getDouble, 920.0)
            .returns(Tuple::getBigDecimal, Row::getBigDecimal, numeric.bigDecimalValue())
            .returns(Tuple::getNumeric, Row::getNumeric, numeric)
            .forRow(row);
          ColumnChecker.checkColumn(1, "NaN")
            .returns(Tuple::getValue, Row::getValue, nan)
            .returns(Tuple::getShort, Row::getShort, (short) 0)
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
  public void testPoint(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT Point(10.1,20.45) \"p\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "p")
            .returns(Tuple::getValue, Row::getValue, new Point(10.1, 20.45))
            .returns(Tuple::getPoint, Row::getPoint, new Point(10.1, 20.45))
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testPointArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT (ARRAY[Point(10.1,20.45)]) \"p\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "p")
            .returns(Tuple::getValue, Row::getValue, new Point[] {new Point(10.1, 20.45)})
            .returns(Tuple::getPointArray, Row::getPointArray, new Point[] {new Point(10.1, 20.45)})
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testLineSegment(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT lseg(point(1.0,1.0),point(2.0,2.0)) \"Lseg\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Lseg")
            .returns(Tuple::getValue, Row::getValue, new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0)))
            .returns(Tuple::getLineSegment, Row::getLineSegment, new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0)))
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testLineSegmentArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT (ARRAY[lseg(point(1.0,1.0),point(2.0,2.0))]) \"Lseg\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Lseg")
            .returns(Tuple::getValue, Row::getValue, new LineSegment[]{new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0))})
            .returns(Tuple::getLineSegmentArray, Row::getLineSegmentArray, new LineSegment[]{new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0))})
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
            .returns(Tuple::getValue, Row::getValue, "X")
            .returns(Tuple::getString, Row::getString, "X")
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
  public void testEnum(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT \"currentMood\" FROM \"EnumDataType\" WHERE \"id\" = 5", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "currentMood")
            .returns(Tuple::getValue, Row::getValue, "ok")
            .returns(Tuple::getString, Row::getString, "ok")
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
  public void testInterval(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '10 years 3 months 332 days 20 hours 20 minutes 20.999991 seconds'::INTERVAL \"Interval\"",
          ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
            Interval interval = Interval.of()
              .years(10)
              .months(3)
              .days(332)
              .hours(20)
              .minutes(20)
              .seconds(20)
              .microseconds(999991);
            ColumnChecker.checkColumn(0, "Interval")
              .returns(Tuple::getValue, Row::getValue, interval)
              .returns(Tuple::getInterval, Row::getInterval, interval)
              .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testCustomType(TestContext ctx) {
    Async async = ctx.async();
    String expected = "Anytown";
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT (address).city FROM \"CustomDataType\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(2, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "city")
            .returns(Tuple::getValue, Row::getValue, expected)
            .returns(Tuple::getString, Row::getString, expected)
            .forRow(row);
          async.complete();
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
          .forRow(row);
        ColumnChecker.checkColumn(1, "JsonArray")
          .returns(Tuple::getValue, Row::getValue, Json.create(array))
          .forRow(row);
        ColumnChecker.checkColumn(2, "TrueValue")
          .returns(Tuple::getValue, Row::getValue, Json.create(true))
          .returns(Tuple::getJson, Row::getJson, Json.create(true))
          .forRow(row);
        ColumnChecker.checkColumn(3, "FalseValue")
          .returns(Tuple::getValue, Row::getValue, Json.create(false))
          .returns(Tuple::getJson, Row::getJson, Json.create(false))
          .forRow(row);
        ColumnChecker.checkColumn(4, "NullValue")
          .returns(Tuple::getValue, Row::getValue, Json.create(null))
          .forRow(row);
        ColumnChecker.checkColumn(5, "Number1")
          .returns(Tuple::getValue, Row::getValue, Json.create(7.502d))
          .returns(Tuple::getJson, Row::getJson, Json.create(7.502d))
          .forRow(row);
        ColumnChecker.checkColumn(6, "Number2")
          .returns(Tuple::getValue, Row::getValue, Json.create(8))
          .returns(Tuple::getJson, Row::getJson, Json.create(8))
          .forRow(row);
        ColumnChecker.checkColumn(7, "Text")
          .returns(Tuple::getValue, Row::getValue, Json.create(" Really Awesome! "))
          .returns(Tuple::getJson, Row::getJson, Json.create(" Really Awesome! "))
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

  // Array tests

  @Test
  public void testDecodeBOOLArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Boolean", "ArrayDataType", Tuple::getBooleanArray, Row::getBooleanArray, true);
  }

  @Test
  public void testDecodeINT2Array(TestContext ctx) {
    testDecodeXXXArray(ctx, "Short", "ArrayDataType", Tuple::getShortArray, Row::getShortArray, (short)1);
  }

  @Test
  public void testDecodeINT4Array(TestContext ctx) {
    testDecodeXXXArray(ctx, "Integer", "ArrayDataType", Tuple::getIntegerArray, Row::getIntegerArray, 2);
  }

  @Test
  public void testDecodeINT8Array(TestContext ctx) {
    testDecodeXXXArray(ctx, "Long", "ArrayDataType", Tuple::getLongArray, Row::getLongArray, 3L);
  }

  @Test
  public void testDecodeFLOAT4Array(TestContext ctx) {
    testDecodeXXXArray(ctx, "Float", "ArrayDataType", Tuple::getFloatArray, Row::getFloatArray, 4.1f);
  }

  @Test
  public void testDecodeFLOAT8Array(TestContext ctx) {
    testDecodeXXXArray(ctx, "Double", "ArrayDataType", Tuple::getDoubleArray, Row::getDoubleArray, 5.2d);
  }

  @Test
  public void testDecodeCHARArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Char", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, "01234567");
  }

  @Test
  public void testDecodeTEXTArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Text", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, "Knock, knock.Who’s there?very long pause….Java.");
  }

  @Test
  public void testDecodeVARCHARArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Varchar", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, "Knock, knock.Who’s there?very long pause….Java.");
  }

  @Test
  public void testDecodeNAMEArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Name", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, "Knock, knock.Who’s there?very long pause….Java.");
  }

  @Test
  public void testDecodeDATEArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "LocalDate", "ArrayDataType", Tuple::getLocalDateArray, Row::getLocalDateArray, LocalDate.parse("1998-05-11"), LocalDate.parse("1998-05-11"));
  }

  @Test
  public void testDecodeTIMEArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "LocalTime", "ArrayDataType", Tuple::getLocalTimeArray, Row::getLocalTimeArray, DataTypeExtendedEncodingTest.lt);
  }

  @Test
  public void testDecodeTIMETZArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "OffsetTime", "ArrayDataType", Tuple::getOffsetTimeArray, Row::getOffsetTimeArray, DataTypeExtendedEncodingTest.dt);
  }

  @Test
  public void testDecodeTIMESTAMPArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "LocalDateTime", "ArrayDataType", Tuple::getLocalDateTimeArray, Row::getLocalDateTimeArray, DataTypeExtendedEncodingTest.ldt);
  }

  @Test
  public void testDecodeTIMESTAMPTZArray(TestContext ctx) {
     testDecodeXXXArray(ctx, "OffsetDateTime", "ArrayDataType", Tuple::getOffsetDateTimeArray, Row::getOffsetDateTimeArray, DataTypeExtendedEncodingTest.odt);
  }

  @Test
  public void testDecodeBYTEAArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Bytea", "ArrayDataType", Tuple::getBufferArray, Row::getBufferArray, Buffer.buffer("HELLO"));
  }

  @Test
  public void testDecodeUUIDArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "UUID", "ArrayDataType", Tuple::getUUIDArray, Row::getUUIDArray, DataTypeExtendedEncodingTest.uuid);
  }

  @Test
  public void testDecodeENUMArray(TestContext ctx) {
    String [] moods = new String [] {"ok", "unhappy", "happy"};
    testDecodeXXXArray(ctx, "Enum", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, moods);
  }

  @Test
  public void testDecodeINTERVALArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Interval", "ArrayDataType", Tuple::getIntervalArray, Row::getIntervalArray, DataTypeExtendedEncodingTest.intervals);
  }


  private Object[] expected = {Json.create(new JsonObject("{\"str\":\"blah\",\"int\":1,\"float\":3.5,\"object\":{},\"array\":[]}")),
    Json.create(new JsonArray("[1,true,null,9.5,\"Hi\"]")),
    Json.create(4),
    Json.create("Hello World"),
    Json.create(true),
    Json.create(false),
    Json.create(null)};

  @Test
  public void testDecodeJSONArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "JSON", "ArrayDataType", Tuple::getJsonArray, Row::getJsonArray,
      expected);
  }

  @Test
  public void testDecodeJSONBArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "JSONB", "ArrayDataType", Tuple::getJsonArray, Row::getJsonArray,
      expected);
  }

  @Test
  public void testDecodeCustomTypeArray(TestContext ctx) {
    String [] addresses = new String [] {"(Anytown,\"Main St\",t)", "(Anytown,\"First St\",f)"};

    testDecodeXXXArray(ctx, "CustomType", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, addresses);
  }

  private <T> void testDecodeXXXArray(TestContext ctx,
                                      String columnName,
                                      String tableName,
                                      ColumnChecker.SerializableBiFunction<Tuple, Integer, Object> byIndexGetter,
                                      ColumnChecker.SerializableBiFunction<Row, String, Object> byNameGetter,
                                      Object... expected) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'",
        ctx.asyncAssertSuccess(res -> {
          conn.query("SELECT \"" + columnName + "\" FROM \"" + tableName + "\" WHERE \"id\" = 1",
            ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, columnName)
              .returns(Tuple::getValue, Row::getValue, expected)
              .returns(byIndexGetter, byNameGetter, expected)
              .forRow(result.iterator().next());
            async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeEmptyArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      // The extra column makes sure that reading the array remains confined in the value since we are doing
      // parsing of the array value
      conn.query("SELECT '{}'::bigint[] \"array\", 1 \"Extra\"",
        ctx.asyncAssertSuccess(result -> {
          ColumnChecker.checkColumn(0, "array")
            .returns(Tuple::getValue, Row::getValue, (Object[]) new Long[0])
            .returns(Tuple::getLongArray, Row::getLongArray, (Object[]) new Long[0])
            .forRow(result.iterator().next());
          async.complete();
        }));
    }));
  }
}

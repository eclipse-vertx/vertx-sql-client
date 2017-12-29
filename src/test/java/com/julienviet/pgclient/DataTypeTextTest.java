package com.julienviet.pgclient;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

import static java.nio.charset.StandardCharsets.*;

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
          ctx.assertNull(row.getValue(0));
          ctx.assertNull(row.getValue("NullValue"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("NullValue"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("NullValue"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("NullValue"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("NullValue"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("NullValue"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("NullValue"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("NullValue"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("NullValue"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("NullValue"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("NullValue"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("NullValue"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("NullValue"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("NullValue"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("NullValue"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("NullValue"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("NullValue"));
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
          ctx.assertEquals(true, row.getBoolean(0));
          ctx.assertEquals(true, row.getValue(0));
          ctx.assertEquals(true, row.getValue("TrueValue"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("TrueValue"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("TrueValue"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("TrueValue"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("TrueValue"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("TrueValue"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("TrueValue"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("TrueValue"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("TrueValue"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("TrueValue"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("TrueValue"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("TrueValue"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("TrueValue"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("TrueValue"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("TrueValue"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("TrueValue"));
          ctx.assertEquals(false, row.getBoolean(1));
          ctx.assertEquals(false, row.getValue(1));
          ctx.assertEquals(false, row.getValue("FalseValue"));
          ctx.assertNull(row.getLong(1));
          ctx.assertNull(row.getLong("FalseValue"));
          ctx.assertNull(row.getInteger(1));
          ctx.assertNull(row.getInteger("FalseValue"));
          ctx.assertNull(row.getFloat(1));
          ctx.assertNull(row.getFloat("FalseValue"));
          ctx.assertNull(row.getDouble(1));
          ctx.assertNull(row.getDouble("FalseValue"));
          ctx.assertNull(row.getCharacter(1));
          ctx.assertNull(row.getCharacter("FalseValue"));
          ctx.assertNull(row.getString(1));
          ctx.assertNull(row.getString("FalseValue"));
          ctx.assertNull(row.getJsonObject(1));
          ctx.assertNull(row.getJsonObject("FalseValue"));
          ctx.assertNull(row.getJsonArray(1));
          ctx.assertNull(row.getJsonArray("FalseValue"));
          ctx.assertNull(row.getBuffer(1));
          ctx.assertNull(row.getBuffer("FalseValue"));
          ctx.assertNull(row.getTemporal(1));
          ctx.assertNull(row.getTemporal("FalseValue"));
          ctx.assertNull(row.getLocalDate(1));
          ctx.assertNull(row.getLocalDate("FalseValue"));
          ctx.assertNull(row.getLocalTime(1));
          ctx.assertNull(row.getLocalTime("FalseValue"));
          ctx.assertNull(row.getOffsetTime(1));
          ctx.assertNull(row.getOffsetTime("FalseValue"));
          ctx.assertNull(row.getLocalDateTime(1));
          ctx.assertNull(row.getLocalDateTime("FalseValue"));
          ctx.assertNull(row.getOffsetDateTime(1));
          ctx.assertNull(row.getOffsetDateTime("FalseValue"));

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
          ctx.assertEquals((short) 32767, row.getValue(0));
          ctx.assertEquals((short) 32767, row.getValue("Short"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Short"));
          ctx.assertEquals(32767L, row.getLong(0));
          ctx.assertEquals(32767L, row.getLong("Short"));
          ctx.assertEquals(32767, row.getInteger(0));
          ctx.assertEquals(32767, row.getInteger("Short"));
          ctx.assertEquals(32767f, row.getFloat(0));
          ctx.assertEquals(32767f, row.getFloat("Short"));
          ctx.assertEquals(32767D, row.getDouble(0));
          ctx.assertEquals(32767D, row.getDouble("Short"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("Short"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("Short"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Short"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Short"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Short"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Short"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Short"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Short"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Short"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Short"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Short"));
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
          ctx.assertEquals(2147483647, row.getInteger(0));
          ctx.assertEquals(2147483647, row.getValue(0));
          ctx.assertEquals(2147483647, row.getInteger("Integer"));
          ctx.assertEquals(2147483647, row.getValue("Integer"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Integer"));
          ctx.assertEquals(2147483647L, row.getLong(0));
          ctx.assertEquals(2147483647L, row.getLong("Integer"));
          ctx.assertEquals(2147483647f, row.getFloat(0));
          ctx.assertEquals(2147483647f, row.getFloat("Integer"));
          ctx.assertEquals(2147483647D, row.getDouble(0));
          ctx.assertEquals(2147483647D, row.getDouble("Integer"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("Integer"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("Integer"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Integer"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Integer"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Integer"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Integer"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Integer"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Integer"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Integer"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Integer"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Integer"));
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
          ctx.assertEquals(9223372036854775807L, row.getLong(0));
          ctx.assertEquals(9223372036854775807L, row.getValue(0));
          ctx.assertEquals(9223372036854775807L, row.getLong("Long"));
          ctx.assertEquals(9223372036854775807L, row.getValue("Long"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Long"));
          ctx.assertEquals(-1, row.getInteger(0));
          ctx.assertEquals(-1, row.getInteger("Long"));
          ctx.assertEquals(9223372036854775807f, row.getFloat(0));
          ctx.assertEquals(9223372036854775807f, row.getFloat("Long"));
          ctx.assertEquals(9223372036854775807d, row.getDouble(0));
          ctx.assertEquals(9223372036854775807d, row.getDouble("Long"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("Long"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("Long"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Long"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Long"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Long"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Long"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Long"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Long"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Long"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Long"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Long"));
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
          ctx.assertEquals(3.4028235E38f, row.getFloat(0));
          ctx.assertEquals(3.4028235E38f, row.getValue(0));
          ctx.assertEquals(3.4028235E38f, row.getFloat("Float"));
          ctx.assertEquals(3.4028235E38f, row.getValue("Float"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Float"));
          ctx.assertEquals(9223372036854775807L, row.getLong(0));
          ctx.assertEquals(9223372036854775807L, row.getLong("Float"));
          ctx.assertEquals(2147483647, row.getInteger(0));
          ctx.assertEquals(2147483647, row.getInteger("Float"));
          ctx.assertEquals(3.4028234663852886E38d, row.getDouble(0));
          ctx.assertEquals(3.4028234663852886E38d, row.getDouble("Float"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("Float"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("Float"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Float"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Float"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Float"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Float"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Float"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Float"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Float"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Float"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Float"));
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
          ctx.assertEquals(1.7976931348623157E308d, row.getDouble(0));
          ctx.assertEquals(1.7976931348623157E308d, row.getValue(0));
          ctx.assertEquals(1.7976931348623157E308d, row.getDouble("Double"));
          ctx.assertEquals(1.7976931348623157E308d, row.getValue("Double"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Double"));
          ctx.assertEquals(9223372036854775807L, row.getLong(0));
          ctx.assertEquals(9223372036854775807L, row.getLong("Double"));
          ctx.assertEquals(2147483647, row.getInteger(0));
          ctx.assertEquals(2147483647, row.getInteger("Double"));
          ctx.assertEquals(Float.POSITIVE_INFINITY, row.getFloat(0));
          ctx.assertEquals(Float.POSITIVE_INFINITY, row.getFloat("Double"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("Double"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("Double"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Double"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Double"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Double"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Double"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Double"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Double"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Double"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Double"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Double"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testNumeric(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 919.999999999999999999999999999999999999::NUMERIC \"Numeric\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals(920.0, row.getDouble(0));
          ctx.assertEquals(920.0, row.getValue(0));
          ctx.assertEquals(920.0, row.getDouble("Numeric"));
          ctx.assertEquals(920.0, row.getValue("Numeric"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Numeric"));
          ctx.assertEquals(920L, row.getLong(0));
          ctx.assertEquals(920L, row.getLong("Numeric"));
          ctx.assertEquals(920, row.getInteger(0));
          ctx.assertEquals(920, row.getInteger("Numeric"));
          ctx.assertEquals(920f, row.getFloat(0));
          ctx.assertEquals(920f, row.getFloat("Numeric"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("Numeric"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("Numeric"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Numeric"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Numeric"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Numeric"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Numeric"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Numeric"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Numeric"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Numeric"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Numeric"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Numeric"));
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
          String name1 = row.getString(0);
          Object value1 = row.getValue(0);
          String name2 = row.getString("Name");
          Object value2 = row.getValue("Name");
          ctx.assertEquals("VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ", name1);
          ctx.assertEquals("VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ", value1);
          ctx.assertEquals("VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ", name2);
          ctx.assertEquals("VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ", value2);
          // must be 63 length
          ctx.assertEquals(63, name1.length());
          ctx.assertEquals(63, name2.length());
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Name"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("Name"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("Name"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("Name"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("Name"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("Name"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Name"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Name"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Name"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Name"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Name"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Name"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Name"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Name"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Name"));
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
          String char1 = row.getString(0);
          Object value1 = row.getValue(0);
          String char2 = row.getString("Char");
          Object value2 = row.getValue("Char");
          ctx.assertEquals("pgClient       ", char1);
          ctx.assertEquals("pgClient       ", value1);
          ctx.assertEquals("pgClient       ", char2);
          ctx.assertEquals("pgClient       ", value2);
          ctx.assertEquals(15, char1.length());
          ctx.assertEquals(15, char2.length());
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Char"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("Char"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("Char"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("Char"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("Char"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("Char"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Char"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Char"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Char"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Char"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Char"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Char"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Char"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Char"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Char"));
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
          String char1 = row.getString(0);
          Object value1 = row.getValue(0);
          String char2 = row.getString("Char");
          Object value2 = row.getValue("Char");
          ctx.assertEquals("V", char1);
          ctx.assertEquals("V", value1);
          ctx.assertEquals("V", char2);
          ctx.assertEquals("V", value2);
          ctx.assertEquals(1, char1.length());
          ctx.assertEquals(1, char2.length());
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Char"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("Char"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("Char"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("Char"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("Char"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("Char"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Char"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Char"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Char"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Char"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Char"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Char"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Char"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Char"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Char"));
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
          ctx.assertEquals('X', row.getValue(0));
          ctx.assertEquals('X', row.getCharacter(0));
          ctx.assertEquals('X', row.getValue("Character"));
          ctx.assertEquals('X', row.getCharacter("Character"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Character"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("Character"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("Character"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("Character"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("Character"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("Character"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Character"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Character"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Character"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Character"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Character"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Character"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Character"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Character"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Character"));
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
          ctx.assertEquals("pgClient", row.getString(0));
          ctx.assertEquals("pgClient", row.getValue(0));
          ctx.assertEquals("pgClient", row.getString("Driver"));
          ctx.assertEquals("pgClient", row.getValue("Driver"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Driver"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("Driver"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("Driver"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("Driver"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("Driver"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("Driver"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Driver"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Driver"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Driver"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Driver"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Driver"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Driver"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Driver"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Driver"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Driver"));
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
          ctx.assertEquals("Vert.x PostgreSQL Client", row.getString(0));
          ctx.assertEquals("Vert.x PostgreSQL Client", row.getValue(0));
          ctx.assertEquals("Vert.x PostgreSQL Client", row.getString("Text"));
          ctx.assertEquals("Vert.x PostgreSQL Client", row.getValue("Text"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("Text"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("Text"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("Text"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("Text"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("Text"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("Text"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("Text"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("Text"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("Text"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("Text"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("Text"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("Text"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("Text"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("Text"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("Text"));
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
          String uuid = "50867d3d-0098-4f61-bd31-9309ebf53475";
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals(uuid, row.getString(0));
          ctx.assertEquals(uuid, row.getValue(0));
          ctx.assertEquals(uuid, row.getString("uuid"));
          ctx.assertEquals(uuid, row.getValue("uuid"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("uuid"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("uuid"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("uuid"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("uuid"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("uuid"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("uuid"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("uuid"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("uuid"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("uuid"));
          ctx.assertNull(row.getTemporal(0));
          ctx.assertNull(row.getTemporal("uuid"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("uuid"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("uuid"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("uuid"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("uuid"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("uuid"));
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
          ctx.assertEquals(ld, row.getLocalDate(0));
          ctx.assertEquals(ld, row.getTemporal(0));
          ctx.assertEquals(ld, row.getValue(0));
          ctx.assertEquals(ld, row.getLocalDate("LocalDate"));
          ctx.assertEquals(ld, row.getTemporal("LocalDate"));
          ctx.assertEquals(ld, row.getValue("LocalDate"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("LocalDate"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("LocalDate"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("LocalDate"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("LocalDate"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("LocalDate"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("LocalDate"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("LocalDate"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("LocalDate"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("LocalDate"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("LocalDate"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("LocalDate"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("LocalDate"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("LocalDate"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("LocalDate"));
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
          ctx.assertEquals(lt, row.getLocalTime(0));
          ctx.assertEquals(lt, row.getTemporal(0));
          ctx.assertEquals(lt, row.getValue(0));
          ctx.assertEquals(lt, row.getLocalTime("LocalTime"));
          ctx.assertEquals(lt, row.getTemporal("LocalTime"));
          ctx.assertEquals(lt, row.getValue("LocalTime"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("LocalTime"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("LocalTime"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("LocalTime"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("LocalTime"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("LocalTime"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("LocalTime"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("LocalTime"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("LocalTime"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("LocalTime"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("LocalTime"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("LocalTime"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("LocalTime"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("LocalTime"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("LocalTime"));
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
          ctx.assertEquals(ot, row.getOffsetTime(0));
          ctx.assertEquals(ot, row.getTemporal(0));
          ctx.assertEquals(ot, row.getValue(0));
          ctx.assertEquals(ot, row.getOffsetTime("OffsetTime"));
          ctx.assertEquals(ot, row.getTemporal("OffsetTime"));
          ctx.assertEquals(ot, row.getValue("OffsetTime"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("OffsetTime"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("OffsetTime"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("OffsetTime"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("OffsetTime"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("OffsetTime"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("OffsetTime"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("OffsetTime"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("OffsetTime"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("OffsetTime"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("OffsetTime"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("OffsetTime"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("OffsetTime"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("OffsetTime"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("OffsetTime"));
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
          ctx.assertEquals(ldt, row.getLocalDateTime(0));
          ctx.assertEquals(ldt, row.getTemporal(0));
          ctx.assertEquals(ldt, row.getValue(0));
          ctx.assertEquals(ldt, row.getLocalDateTime("LocalDateTime"));
          ctx.assertEquals(ldt, row.getTemporal("LocalDateTime"));
          ctx.assertEquals(ldt, row.getValue("LocalDateTime"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("LocalDateTime"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("LocalDateTime"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("LocalDateTime"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("LocalDateTime"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("LocalDateTime"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("LocalDateTime"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("LocalDateTime"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("LocalDateTime"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("LocalDateTime"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("LocalDateTime"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("LocalDateTime"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("LocalDateTime"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("LocalDateTime"));
          ctx.assertNull(row.getOffsetDateTime(0));
          ctx.assertNull(row.getOffsetDateTime("LocalDateTime"));
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
          ctx.assertEquals(odt, row.getOffsetDateTime(0));
          ctx.assertEquals(odt, row.getTemporal(0));
          ctx.assertEquals(odt, row.getValue(0));
          ctx.assertEquals(odt, row.getOffsetDateTime("OffsetDateTime"));
          ctx.assertEquals(odt, row.getTemporal("OffsetDateTime"));
          ctx.assertEquals(odt, row.getValue("OffsetDateTime"));
          ctx.assertNull(row.getBoolean(0));
          ctx.assertNull(row.getBoolean("OffsetDateTime"));
          ctx.assertNull(row.getLong(0));
          ctx.assertNull(row.getLong("OffsetDateTime"));
          ctx.assertNull(row.getInteger(0));
          ctx.assertNull(row.getInteger("OffsetDateTime"));
          ctx.assertNull(row.getFloat(0));
          ctx.assertNull(row.getFloat("OffsetDateTime"));
          ctx.assertNull(row.getDouble(0));
          ctx.assertNull(row.getDouble("OffsetDateTime"));
          ctx.assertNull(row.getCharacter(0));
          ctx.assertNull(row.getCharacter("OffsetDateTime"));
          ctx.assertNull(row.getString(0));
          ctx.assertNull(row.getString("OffsetDateTime"));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonObject("OffsetDateTime"));
          ctx.assertNull(row.getJsonArray(0));
          ctx.assertNull(row.getJsonArray("OffsetDateTime"));
          ctx.assertNull(row.getBuffer(0));
          ctx.assertNull(row.getBuffer("OffsetDateTime"));
          ctx.assertNull(row.getLocalDate(0));
          ctx.assertNull(row.getLocalDate("OffsetDateTime"));
          ctx.assertNull(row.getLocalTime(0));
          ctx.assertNull(row.getLocalTime("OffsetDateTime"));
          ctx.assertNull(row.getOffsetTime(0));
          ctx.assertNull(row.getOffsetTime("OffsetDateTime"));
          ctx.assertNull(row.getLocalDateTime(0));
          ctx.assertNull(row.getLocalDateTime("OffsetDateTime"));
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testJsonbObject(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '  {\"str\":\"blah\", \"int\" : 1, \"float\" :" +
        " 3.5, \"object\": {}, \"array\" : []   }'::JSONB \"JsonObject\"", ctx.asyncAssertSuccess(result -> {
        JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
          " 3.5, \"object\": {}, \"array\" : []}");
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(object, row.getValue(0));
        ctx.assertEquals(object, row.getJsonObject(0));
        ctx.assertEquals(object, row.getValue("JsonObject"));
        ctx.assertEquals(object, row.getJsonObject("JsonObject"));
        ctx.assertNull(row.getBoolean(0));
        ctx.assertNull(row.getBoolean("JsonObject"));
        ctx.assertNull(row.getLong(0));
        ctx.assertNull(row.getLong("JsonObject"));
        ctx.assertNull(row.getInteger(0));
        ctx.assertNull(row.getInteger("JsonObject"));
        ctx.assertNull(row.getFloat(0));
        ctx.assertNull(row.getFloat("JsonObject"));
        ctx.assertNull(row.getDouble(0));
        ctx.assertNull(row.getDouble("JsonObject"));
        ctx.assertNull(row.getCharacter(0));
        ctx.assertNull(row.getCharacter("JsonObject"));
        ctx.assertNull(row.getString(0));
        ctx.assertNull(row.getString("JsonObject"));
        ctx.assertNull(row.getJsonArray(0));
        ctx.assertNull(row.getJsonArray("JsonObject"));
        ctx.assertNull(row.getBuffer(0));
        ctx.assertNull(row.getBuffer("JsonObject"));
        ctx.assertNull(row.getTemporal(0));
        ctx.assertNull(row.getTemporal("JsonObject"));
        ctx.assertNull(row.getLocalDate(0));
        ctx.assertNull(row.getLocalDate("JsonObject"));
        ctx.assertNull(row.getLocalTime(0));
        ctx.assertNull(row.getLocalTime("JsonObject"));
        ctx.assertNull(row.getOffsetTime(0));
        ctx.assertNull(row.getOffsetTime("JsonObject"));
        ctx.assertNull(row.getLocalDateTime(0));
        ctx.assertNull(row.getLocalDateTime("JsonObject"));
        ctx.assertNull(row.getOffsetDateTime(0));
        ctx.assertNull(row.getOffsetDateTime("JsonObject"));
        async.complete();
      }));
    }));
  }

  @Test
  public void testJsonbArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '  [1,true,null,9.5,\"Hi\" ] '::JSONB", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        JsonArray jsonArray = row.getJsonArray(0);
        Object value = row.getValue(0);
        ctx.assertEquals(new JsonArray("[1,true,null,9.5,\"Hi\"]"), jsonArray);
        ctx.assertEquals(new JsonArray("[1,true,null,9.5,\"Hi\"]"), value);
        ctx.assertNull(row.getBoolean(0));
        ctx.assertNull(row.getLong(0));
        ctx.assertNull(row.getInteger(0));
        ctx.assertNull(row.getFloat(0));
        ctx.assertNull(row.getDouble(0));
        ctx.assertNull(row.getCharacter(0));
        ctx.assertNull(row.getString(0));
        ctx.assertNull(row.getJsonObject(0));
        ctx.assertNull(row.getBuffer(0));
        ctx.assertNull(row.getTemporal(0));
        ctx.assertNull(row.getLocalDate(0));
        ctx.assertNull(row.getLocalTime(0));
        ctx.assertNull(row.getOffsetTime(0));
        ctx.assertNull(row.getLocalDateTime(0));
        ctx.assertNull(row.getOffsetDateTime(0));
        async.complete();
      }));
    }));
  }

  @Test
  public void testJsonObject(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '    {\"str\":\"blah\", \"int\" : 1, \"float\" :" +
        " 3.5, \"object\": {}, \"array\" : []  }    '::JSON \"JsonObject\"", ctx.asyncAssertSuccess(result -> {
        JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
          " 3.5, \"object\": {}, \"array\" : []}");
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(object, row.getValue(0));
        ctx.assertEquals(object, row.getJsonObject(0));
        ctx.assertEquals(object, row.getValue("JsonObject"));
        ctx.assertEquals(object, row.getJsonObject("JsonObject"));
        ctx.assertNull(row.getBoolean(0));
        ctx.assertNull(row.getBoolean("JsonObject"));
        ctx.assertNull(row.getLong(0));
        ctx.assertNull(row.getLong("JsonObject"));
        ctx.assertNull(row.getInteger(0));
        ctx.assertNull(row.getInteger("JsonObject"));
        ctx.assertNull(row.getFloat(0));
        ctx.assertNull(row.getFloat("JsonObject"));
        ctx.assertNull(row.getDouble(0));
        ctx.assertNull(row.getDouble("JsonObject"));
        ctx.assertNull(row.getCharacter(0));
        ctx.assertNull(row.getCharacter("JsonObject"));
        ctx.assertNull(row.getString(0));
        ctx.assertNull(row.getString("JsonObject"));
        ctx.assertNull(row.getJsonArray(0));
        ctx.assertNull(row.getJsonArray("JsonObject"));
        ctx.assertNull(row.getBuffer(0));
        ctx.assertNull(row.getBuffer("JsonObject"));
        ctx.assertNull(row.getTemporal(0));
        ctx.assertNull(row.getTemporal("JsonObject"));
        ctx.assertNull(row.getLocalDate(0));
        ctx.assertNull(row.getLocalDate("JsonObject"));
        ctx.assertNull(row.getLocalTime(0));
        ctx.assertNull(row.getLocalTime("JsonObject"));
        ctx.assertNull(row.getOffsetTime(0));
        ctx.assertNull(row.getOffsetTime("JsonObject"));
        ctx.assertNull(row.getLocalDateTime(0));
        ctx.assertNull(row.getLocalDateTime("JsonObject"));
        ctx.assertNull(row.getOffsetDateTime(0));
        ctx.assertNull(row.getOffsetDateTime("JsonObject"));
        async.complete();
      }));
    }));
  }

  @Test
  public void testJsonArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '     [1,true,null,9.5,\"Hi\"]     '::JSON \"Array\"", ctx.asyncAssertSuccess(result -> {
        JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(array, row.getValue(0));
        ctx.assertEquals(array, row.getJsonArray(0));
        ctx.assertEquals(array, row.getValue("Array"));
        ctx.assertEquals(array, row.getJsonArray("Array"));
        ctx.assertNull(row.getBoolean(0));
        ctx.assertNull(row.getBoolean("Array"));
        ctx.assertNull(row.getLong(0));
        ctx.assertNull(row.getLong("Array"));
        ctx.assertNull(row.getInteger(0));
        ctx.assertNull(row.getInteger("Array"));
        ctx.assertNull(row.getFloat(0));
        ctx.assertNull(row.getFloat("Array"));
        ctx.assertNull(row.getDouble(0));
        ctx.assertNull(row.getDouble("Array"));
        ctx.assertNull(row.getCharacter(0));
        ctx.assertNull(row.getCharacter("Array"));
        ctx.assertNull(row.getString(0));
        ctx.assertNull(row.getString("Array"));
        ctx.assertNull(row.getJsonObject(0));
        ctx.assertNull(row.getJsonObject("Array"));
        ctx.assertNull(row.getBuffer(0));
        ctx.assertNull(row.getBuffer("Array"));
        ctx.assertNull(row.getTemporal(0));
        ctx.assertNull(row.getTemporal("Array"));
        ctx.assertNull(row.getLocalDate(0));
        ctx.assertNull(row.getLocalDate("Array"));
        ctx.assertNull(row.getLocalTime(0));
        ctx.assertNull(row.getLocalTime("Array"));
        ctx.assertNull(row.getOffsetTime(0));
        ctx.assertNull(row.getOffsetTime("Array"));
        ctx.assertNull(row.getLocalDateTime(0));
        ctx.assertNull(row.getLocalDateTime("Array"));
        ctx.assertNull(row.getOffsetDateTime(0));
        ctx.assertNull(row.getOffsetDateTime("Array"));
        async.complete();
      }));
    }));
  }

  @Test
  public void testJsonbScalar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT ' true '::JSONB \"TrueValue\", ' false '::JSONB \"FalseValue\", ' null '::JSONB \"NullValue\", ' 7.502 '::JSONB \"Number1\", ' 8 '::JSONB \"Number2\", '\" Really Awesome! \"'::JSONB \"Text\"", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(true, row.getBoolean(0));
        ctx.assertEquals(true, row.getValue(0));
        ctx.assertEquals(true, row.getBoolean("TrueValue"));
        ctx.assertEquals(true, row.getValue("TrueValue"));
        ctx.assertNull(row.getLong(0));
        ctx.assertNull(row.getLong("TrueValue"));
        ctx.assertNull(row.getInteger(0));
        ctx.assertNull(row.getInteger("TrueValue"));
        ctx.assertNull(row.getFloat(0));
        ctx.assertNull(row.getFloat("TrueValue"));
        ctx.assertNull(row.getDouble(0));
        ctx.assertNull(row.getDouble("TrueValue"));
        ctx.assertNull(row.getCharacter(0));
        ctx.assertNull(row.getCharacter("TrueValue"));
        ctx.assertNull(row.getString(0));
        ctx.assertNull(row.getString("TrueValue"));
        ctx.assertNull(row.getJsonObject(0));
        ctx.assertNull(row.getJsonObject("TrueValue"));
        ctx.assertNull(row.getJsonArray(0));
        ctx.assertNull(row.getJsonArray("TrueValue"));
        ctx.assertNull(row.getBuffer(0));
        ctx.assertNull(row.getBuffer("TrueValue"));
        ctx.assertNull(row.getTemporal(0));
        ctx.assertNull(row.getTemporal("TrueValue"));
        ctx.assertNull(row.getLocalDate(0));
        ctx.assertNull(row.getLocalDate("TrueValue"));
        ctx.assertNull(row.getLocalTime(0));
        ctx.assertNull(row.getLocalTime("TrueValue"));
        ctx.assertNull(row.getOffsetTime(0));
        ctx.assertNull(row.getOffsetTime("TrueValue"));
        ctx.assertNull(row.getLocalDateTime(0));
        ctx.assertNull(row.getLocalDateTime("TrueValue"));
        ctx.assertNull(row.getOffsetDateTime(0));
        ctx.assertNull(row.getOffsetDateTime("TrueValue"));

        ctx.assertEquals(false, row.getBoolean(1));
        ctx.assertEquals(false, row.getValue(1));
        ctx.assertEquals(false, row.getBoolean("FalseValue"));
        ctx.assertEquals(false, row.getValue("FalseValue"));
        ctx.assertNull(row.getLong(1));
        ctx.assertNull(row.getLong("FalseValue"));
        ctx.assertNull(row.getInteger(1));
        ctx.assertNull(row.getInteger("FalseValue"));
        ctx.assertNull(row.getFloat(1));
        ctx.assertNull(row.getFloat("FalseValue"));
        ctx.assertNull(row.getDouble(1));
        ctx.assertNull(row.getDouble("FalseValue"));
        ctx.assertNull(row.getCharacter(1));
        ctx.assertNull(row.getCharacter("FalseValue"));
        ctx.assertNull(row.getString(1));
        ctx.assertNull(row.getString("FalseValue"));
        ctx.assertNull(row.getJsonObject(1));
        ctx.assertNull(row.getJsonObject("FalseValue"));
        ctx.assertNull(row.getJsonArray(1));
        ctx.assertNull(row.getJsonArray("FalseValue"));
        ctx.assertNull(row.getBuffer(1));
        ctx.assertNull(row.getBuffer("FalseValue"));
        ctx.assertNull(row.getTemporal(1));
        ctx.assertNull(row.getTemporal("FalseValue"));
        ctx.assertNull(row.getLocalDate(1));
        ctx.assertNull(row.getLocalDate("FalseValue"));
        ctx.assertNull(row.getLocalTime(1));
        ctx.assertNull(row.getLocalTime("FalseValue"));
        ctx.assertNull(row.getOffsetTime(1));
        ctx.assertNull(row.getOffsetTime("FalseValue"));
        ctx.assertNull(row.getLocalDateTime(1));
        ctx.assertNull(row.getLocalDateTime("FalseValue"));
        ctx.assertNull(row.getOffsetDateTime(1));
        ctx.assertNull(row.getOffsetDateTime("FalseValue"));

        ctx.assertNull(row.getValue(2));
        ctx.assertNull(row.getValue("NullValue"));
        ctx.assertNull(row.getBoolean(2));
        ctx.assertNull(row.getBoolean("NullValue"));
        ctx.assertNull(row.getLong(2));
        ctx.assertNull(row.getLong("NullValue"));
        ctx.assertNull(row.getInteger(2));
        ctx.assertNull(row.getInteger("NullValue"));
        ctx.assertNull(row.getFloat(2));
        ctx.assertNull(row.getFloat("NullValue"));
        ctx.assertNull(row.getDouble(2));
        ctx.assertNull(row.getDouble("NullValue"));
        ctx.assertNull(row.getCharacter(2));
        ctx.assertNull(row.getCharacter("NullValue"));
        ctx.assertNull(row.getString(2));
        ctx.assertNull(row.getString("NullValue"));
        ctx.assertNull(row.getJsonObject(2));
        ctx.assertNull(row.getJsonObject("NullValue"));
        ctx.assertNull(row.getJsonArray(2));
        ctx.assertNull(row.getJsonArray("NullValue"));
        ctx.assertNull(row.getBuffer(2));
        ctx.assertNull(row.getBuffer("NullValue"));
        ctx.assertNull(row.getTemporal(2));
        ctx.assertNull(row.getTemporal("NullValue"));
        ctx.assertNull(row.getLocalDate(2));
        ctx.assertNull(row.getLocalDate("NullValue"));
        ctx.assertNull(row.getLocalTime(2));
        ctx.assertNull(row.getLocalTime("NullValue"));
        ctx.assertNull(row.getOffsetTime(2));
        ctx.assertNull(row.getOffsetTime("NullValue"));
        ctx.assertNull(row.getLocalDateTime(2));
        ctx.assertNull(row.getLocalDateTime("NullValue"));
        ctx.assertNull(row.getOffsetDateTime(2));
        ctx.assertNull(row.getOffsetDateTime("NullValue"));

        // ctx.assertEquals(7.502f, row.getFloat(3));
        ctx.assertEquals(7.502d, row.getDouble(3));
        ctx.assertEquals(7.502d, row.getValue(3));
        ctx.assertEquals(7.502d, row.getDouble("Number1"));
        ctx.assertEquals(7.502d, row.getValue("Number1"));
        ctx.assertNull(row.getBoolean(3));
        ctx.assertNull(row.getBoolean("Number1"));
        ctx.assertEquals(7L, row.getLong(3));
        ctx.assertEquals(7L, row.getLong("Number1"));
        ctx.assertEquals(7, row.getInteger(3));
        ctx.assertEquals(7, row.getInteger("Number1"));
        ctx.assertEquals(7.502f, row.getFloat(3));
        ctx.assertEquals(7.502f, row.getFloat("Number1"));
        ctx.assertNull(row.getCharacter(3));
        ctx.assertNull(row.getCharacter("Number1"));
        ctx.assertNull(row.getString(3));
        ctx.assertNull(row.getString("Number1"));
        ctx.assertNull(row.getJsonObject(3));
        ctx.assertNull(row.getJsonObject("Number1"));
        ctx.assertNull(row.getJsonArray(3));
        ctx.assertNull(row.getJsonArray("Number1"));
        ctx.assertNull(row.getBuffer(3));
        ctx.assertNull(row.getBuffer("Number1"));
        ctx.assertNull(row.getTemporal(3));
        ctx.assertNull(row.getTemporal("Number1"));
        ctx.assertNull(row.getLocalDate(3));
        ctx.assertNull(row.getLocalDate("Number1"));
        ctx.assertNull(row.getLocalTime(3));
        ctx.assertNull(row.getLocalTime("Number1"));
        ctx.assertNull(row.getOffsetTime(3));
        ctx.assertNull(row.getOffsetTime("Number1"));
        ctx.assertNull(row.getLocalDateTime(3));
        ctx.assertNull(row.getLocalDateTime("Number1"));
        ctx.assertNull(row.getOffsetDateTime(3));
        ctx.assertNull(row.getOffsetDateTime("Number1"));

        ctx.assertEquals(8, row.getInteger(4));
        ctx.assertEquals(8, row.getValue(4));
        ctx.assertEquals(8, row.getInteger("Number2"));
        ctx.assertEquals(8, row.getValue("Number2"));
        ctx.assertNull(row.getBoolean(4));
        ctx.assertNull(row.getBoolean("Number2"));
        ctx.assertEquals(8L, row.getLong(4));
        ctx.assertEquals(8L, row.getLong("Number2"));
        ctx.assertEquals(8f, row.getFloat(4));
        ctx.assertEquals(8f, row.getFloat("Number2"));
        ctx.assertEquals(8d, row.getDouble(4));
        ctx.assertEquals(8d, row.getDouble("Number2"));
        ctx.assertNull(row.getCharacter(4));
        ctx.assertNull(row.getCharacter("Number2"));
        ctx.assertNull(row.getString(4));
        ctx.assertNull(row.getString("Number2"));
        ctx.assertNull(row.getJsonObject(4));
        ctx.assertNull(row.getJsonObject("Number2"));
        ctx.assertNull(row.getJsonArray(4));
        ctx.assertNull(row.getJsonArray("Number2"));
        ctx.assertNull(row.getBuffer(4));
        ctx.assertNull(row.getBuffer("Number2"));
        ctx.assertNull(row.getTemporal(4));
        ctx.assertNull(row.getTemporal("Number2"));
        ctx.assertNull(row.getLocalDate(4));
        ctx.assertNull(row.getLocalDate("Number2"));
        ctx.assertNull(row.getLocalTime(4));
        ctx.assertNull(row.getLocalTime("Number2"));
        ctx.assertNull(row.getOffsetTime(4));
        ctx.assertNull(row.getOffsetTime("Number2"));
        ctx.assertNull(row.getLocalDateTime(4));
        ctx.assertNull(row.getLocalDateTime("Number2"));
        ctx.assertNull(row.getOffsetDateTime(4));
        ctx.assertNull(row.getOffsetDateTime("Number2"));

        // ctx.assertEquals(8L, row.getLong(4));
        ctx.assertEquals(" Really Awesome! ", row.getString(5));
        ctx.assertEquals(" Really Awesome! ", row.getValue(5));
        ctx.assertEquals(" Really Awesome! ", row.getString("Text"));
        ctx.assertEquals(" Really Awesome! ", row.getValue("Text"));
        ctx.assertNull(row.getBoolean(5));
        ctx.assertNull(row.getBoolean("Text"));
        ctx.assertNull(row.getLong(5));
        ctx.assertNull(row.getLong("Text"));
        ctx.assertNull(row.getInteger(5));
        ctx.assertNull(row.getInteger("Text"));
        ctx.assertNull(row.getFloat(5));
        ctx.assertNull(row.getFloat("Text"));
        ctx.assertNull(row.getDouble(5));
        ctx.assertNull(row.getDouble("Text"));
        ctx.assertNull(row.getCharacter(5));
        ctx.assertNull(row.getCharacter("Text"));
        ctx.assertNull(row.getJsonObject(5));
        ctx.assertNull(row.getJsonObject("Text"));
        ctx.assertNull(row.getJsonArray(5));
        ctx.assertNull(row.getJsonArray("Text"));
        ctx.assertNull(row.getBuffer(5));
        ctx.assertNull(row.getBuffer("Text"));
        ctx.assertNull(row.getTemporal(5));
        ctx.assertNull(row.getTemporal("Text"));
        ctx.assertNull(row.getLocalDate(5));
        ctx.assertNull(row.getLocalDate("Text"));
        ctx.assertNull(row.getLocalTime(5));
        ctx.assertNull(row.getLocalTime("Text"));
        ctx.assertNull(row.getOffsetTime(5));
        ctx.assertNull(row.getOffsetTime("Text"));
        ctx.assertNull(row.getLocalDateTime(5));
        ctx.assertNull(row.getLocalDateTime("Text"));
        ctx.assertNull(row.getOffsetDateTime(5));
        ctx.assertNull(row.getOffsetDateTime("Text"));
        async.complete();
      }));
    }));
  }

  @Test
  public void testJsonScalar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT ' true '::JSON \"TrueValue\", ' false '::JSON \"FalseValue\", ' null '::JSON \"NullValue\", ' 7.502 '::JSON \"Number1\", ' 8 '::JSON \"Number2\", '\" Really Awesome! \"'::JSON \"Text\"", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(true, row.getBoolean(0));
        ctx.assertEquals(true, row.getValue(0));
        ctx.assertEquals(true, row.getBoolean("TrueValue"));
        ctx.assertEquals(true, row.getValue("TrueValue"));
        ctx.assertNull(row.getLong(0));
        ctx.assertNull(row.getLong("TrueValue"));
        ctx.assertNull(row.getInteger(0));
        ctx.assertNull(row.getInteger("TrueValue"));
        ctx.assertNull(row.getFloat(0));
        ctx.assertNull(row.getFloat("TrueValue"));
        ctx.assertNull(row.getDouble(0));
        ctx.assertNull(row.getDouble("TrueValue"));
        ctx.assertNull(row.getCharacter(0));
        ctx.assertNull(row.getCharacter("TrueValue"));
        ctx.assertNull(row.getString(0));
        ctx.assertNull(row.getString("TrueValue"));
        ctx.assertNull(row.getJsonObject(0));
        ctx.assertNull(row.getJsonObject("TrueValue"));
        ctx.assertNull(row.getJsonArray(0));
        ctx.assertNull(row.getJsonArray("TrueValue"));
        ctx.assertNull(row.getBuffer(0));
        ctx.assertNull(row.getBuffer("TrueValue"));
        ctx.assertNull(row.getTemporal(0));
        ctx.assertNull(row.getTemporal("TrueValue"));
        ctx.assertNull(row.getLocalDate(0));
        ctx.assertNull(row.getLocalDate("TrueValue"));
        ctx.assertNull(row.getLocalTime(0));
        ctx.assertNull(row.getLocalTime("TrueValue"));
        ctx.assertNull(row.getOffsetTime(0));
        ctx.assertNull(row.getOffsetTime("TrueValue"));
        ctx.assertNull(row.getLocalDateTime(0));
        ctx.assertNull(row.getLocalDateTime("TrueValue"));
        ctx.assertNull(row.getOffsetDateTime(0));
        ctx.assertNull(row.getOffsetDateTime("TrueValue"));

        ctx.assertEquals(false, row.getBoolean(1));
        ctx.assertEquals(false, row.getValue(1));
        ctx.assertEquals(false, row.getBoolean("FalseValue"));
        ctx.assertEquals(false, row.getValue("FalseValue"));
        ctx.assertNull(row.getLong(1));
        ctx.assertNull(row.getLong("FalseValue"));
        ctx.assertNull(row.getInteger(1));
        ctx.assertNull(row.getInteger("FalseValue"));
        ctx.assertNull(row.getFloat(1));
        ctx.assertNull(row.getFloat("FalseValue"));
        ctx.assertNull(row.getDouble(1));
        ctx.assertNull(row.getDouble("FalseValue"));
        ctx.assertNull(row.getCharacter(1));
        ctx.assertNull(row.getCharacter("FalseValue"));
        ctx.assertNull(row.getString(1));
        ctx.assertNull(row.getString("FalseValue"));
        ctx.assertNull(row.getJsonObject(1));
        ctx.assertNull(row.getJsonObject("FalseValue"));
        ctx.assertNull(row.getJsonArray(1));
        ctx.assertNull(row.getJsonArray("FalseValue"));
        ctx.assertNull(row.getBuffer(1));
        ctx.assertNull(row.getBuffer("FalseValue"));
        ctx.assertNull(row.getTemporal(1));
        ctx.assertNull(row.getTemporal("FalseValue"));
        ctx.assertNull(row.getLocalDate(1));
        ctx.assertNull(row.getLocalDate("FalseValue"));
        ctx.assertNull(row.getLocalTime(1));
        ctx.assertNull(row.getLocalTime("FalseValue"));
        ctx.assertNull(row.getOffsetTime(1));
        ctx.assertNull(row.getOffsetTime("FalseValue"));
        ctx.assertNull(row.getLocalDateTime(1));
        ctx.assertNull(row.getLocalDateTime("FalseValue"));
        ctx.assertNull(row.getOffsetDateTime(1));
        ctx.assertNull(row.getOffsetDateTime("FalseValue"));

        ctx.assertNull(row.getValue(2));
        ctx.assertNull(row.getValue("NullValue"));
        ctx.assertNull(row.getBoolean(2));
        ctx.assertNull(row.getBoolean("NullValue"));
        ctx.assertNull(row.getLong(2));
        ctx.assertNull(row.getLong("NullValue"));
        ctx.assertNull(row.getInteger(2));
        ctx.assertNull(row.getInteger("NullValue"));
        ctx.assertNull(row.getFloat(2));
        ctx.assertNull(row.getFloat("NullValue"));
        ctx.assertNull(row.getDouble(2));
        ctx.assertNull(row.getDouble("NullValue"));
        ctx.assertNull(row.getCharacter(2));
        ctx.assertNull(row.getCharacter("NullValue"));
        ctx.assertNull(row.getString(2));
        ctx.assertNull(row.getString("NullValue"));
        ctx.assertNull(row.getJsonObject(2));
        ctx.assertNull(row.getJsonObject("NullValue"));
        ctx.assertNull(row.getJsonArray(2));
        ctx.assertNull(row.getJsonArray("NullValue"));
        ctx.assertNull(row.getBuffer(2));
        ctx.assertNull(row.getBuffer("NullValue"));
        ctx.assertNull(row.getTemporal(2));
        ctx.assertNull(row.getTemporal("NullValue"));
        ctx.assertNull(row.getLocalDate(2));
        ctx.assertNull(row.getLocalDate("NullValue"));
        ctx.assertNull(row.getLocalTime(2));
        ctx.assertNull(row.getLocalTime("NullValue"));
        ctx.assertNull(row.getOffsetTime(2));
        ctx.assertNull(row.getOffsetTime("NullValue"));
        ctx.assertNull(row.getLocalDateTime(2));
        ctx.assertNull(row.getLocalDateTime("NullValue"));
        ctx.assertNull(row.getOffsetDateTime(2));
        ctx.assertNull(row.getOffsetDateTime("NullValue"));

        // ctx.assertEquals(7.502f, row.getFloat(3));
        ctx.assertEquals(7.502d, row.getDouble(3));
        ctx.assertEquals(7.502d, row.getValue(3));
        ctx.assertEquals(7.502d, row.getDouble("Number1"));
        ctx.assertEquals(7.502d, row.getValue("Number1"));
        ctx.assertNull(row.getBoolean(3));
        ctx.assertNull(row.getBoolean("Number1"));
        ctx.assertEquals(7L, row.getLong(3));
        ctx.assertEquals(7L, row.getLong("Number1"));
        ctx.assertEquals(7, row.getInteger(3));
        ctx.assertEquals(7, row.getInteger("Number1"));
        ctx.assertEquals(7.502f, row.getFloat(3));
        ctx.assertEquals(7.502f, row.getFloat("Number1"));
        ctx.assertNull(row.getCharacter(3));
        ctx.assertNull(row.getCharacter("Number1"));
        ctx.assertNull(row.getString(3));
        ctx.assertNull(row.getString("Number1"));
        ctx.assertNull(row.getJsonObject(3));
        ctx.assertNull(row.getJsonObject("Number1"));
        ctx.assertNull(row.getJsonArray(3));
        ctx.assertNull(row.getJsonArray("Number1"));
        ctx.assertNull(row.getBuffer(3));
        ctx.assertNull(row.getBuffer("Number1"));
        ctx.assertNull(row.getTemporal(3));
        ctx.assertNull(row.getTemporal("Number1"));
        ctx.assertNull(row.getLocalDate(3));
        ctx.assertNull(row.getLocalDate("Number1"));
        ctx.assertNull(row.getLocalTime(3));
        ctx.assertNull(row.getLocalTime("Number1"));
        ctx.assertNull(row.getOffsetTime(3));
        ctx.assertNull(row.getOffsetTime("Number1"));
        ctx.assertNull(row.getLocalDateTime(3));
        ctx.assertNull(row.getLocalDateTime("Number1"));
        ctx.assertNull(row.getOffsetDateTime(3));
        ctx.assertNull(row.getOffsetDateTime("Number1"));

        ctx.assertEquals(8, row.getInteger(4));
        ctx.assertEquals(8, row.getValue(4));
        ctx.assertEquals(8, row.getInteger("Number2"));
        ctx.assertEquals(8, row.getValue("Number2"));
        ctx.assertNull(row.getBoolean(4));
        ctx.assertNull(row.getBoolean("Number2"));
        ctx.assertEquals(8L, row.getLong(4));
        ctx.assertEquals(8L, row.getLong("Number2"));
        ctx.assertEquals(8f, row.getFloat(4));
        ctx.assertEquals(8f, row.getFloat("Number2"));
        ctx.assertEquals(8d, row.getDouble(4));
        ctx.assertEquals(8d, row.getDouble("Number2"));
        ctx.assertNull(row.getCharacter(4));
        ctx.assertNull(row.getCharacter("Number2"));
        ctx.assertNull(row.getString(4));
        ctx.assertNull(row.getString("Number2"));
        ctx.assertNull(row.getJsonObject(4));
        ctx.assertNull(row.getJsonObject("Number2"));
        ctx.assertNull(row.getJsonArray(4));
        ctx.assertNull(row.getJsonArray("Number2"));
        ctx.assertNull(row.getBuffer(4));
        ctx.assertNull(row.getBuffer("Number2"));
        ctx.assertNull(row.getTemporal(4));
        ctx.assertNull(row.getTemporal("Number2"));
        ctx.assertNull(row.getLocalDate(4));
        ctx.assertNull(row.getLocalDate("Number2"));
        ctx.assertNull(row.getLocalTime(4));
        ctx.assertNull(row.getLocalTime("Number2"));
        ctx.assertNull(row.getOffsetTime(4));
        ctx.assertNull(row.getOffsetTime("Number2"));
        ctx.assertNull(row.getLocalDateTime(4));
        ctx.assertNull(row.getLocalDateTime("Number2"));
        ctx.assertNull(row.getOffsetDateTime(4));
        ctx.assertNull(row.getOffsetDateTime("Number2"));

        // ctx.assertEquals(8L, row.getLong(4));
        ctx.assertEquals(" Really Awesome! ", row.getString(5));
        ctx.assertEquals(" Really Awesome! ", row.getValue(5));
        ctx.assertEquals(" Really Awesome! ", row.getString("Text"));
        ctx.assertEquals(" Really Awesome! ", row.getValue("Text"));
        ctx.assertNull(row.getBoolean(5));
        ctx.assertNull(row.getBoolean("Text"));
        ctx.assertNull(row.getLong(5));
        ctx.assertNull(row.getLong("Text"));
        ctx.assertNull(row.getInteger(5));
        ctx.assertNull(row.getInteger("Text"));
        ctx.assertNull(row.getFloat(5));
        ctx.assertNull(row.getFloat("Text"));
        ctx.assertNull(row.getDouble(5));
        ctx.assertNull(row.getDouble("Text"));
        ctx.assertNull(row.getCharacter(5));
        ctx.assertNull(row.getCharacter("Text"));
        ctx.assertNull(row.getJsonObject(5));
        ctx.assertNull(row.getJsonObject("Text"));
        ctx.assertNull(row.getJsonArray(5));
        ctx.assertNull(row.getJsonArray("Text"));
        ctx.assertNull(row.getBuffer(5));
        ctx.assertNull(row.getBuffer("Text"));
        ctx.assertNull(row.getTemporal(5));
        ctx.assertNull(row.getTemporal("Text"));
        ctx.assertNull(row.getLocalDate(5));
        ctx.assertNull(row.getLocalDate("Text"));
        ctx.assertNull(row.getLocalTime(5));
        ctx.assertNull(row.getLocalTime("Text"));
        ctx.assertNull(row.getOffsetTime(5));
        ctx.assertNull(row.getOffsetTime("Text"));
        ctx.assertNull(row.getLocalDateTime(5));
        ctx.assertNull(row.getLocalDateTime("Text"));
        ctx.assertNull(row.getOffsetDateTime(5));
        ctx.assertNull(row.getOffsetDateTime("Text"));
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
        ctx.assertEquals("12345678910", row.getBuffer(0).toString(UTF_8));
        ctx.assertEquals(Buffer.buffer("12345678910"), row.getValue(0));
        ctx.assertEquals(Buffer.buffer("12345678910"), row.getValue("Buffer1"));
        ctx.assertEquals(Buffer.buffer("12345678910"), row.getBuffer(0));
        ctx.assertEquals(Buffer.buffer("12345678910"), row.getBuffer("Buffer1"));
        ctx.assertNull(row.getBoolean(0));
        ctx.assertNull(row.getBoolean("Buffer1"));
        ctx.assertNull(row.getLong(0));
        ctx.assertNull(row.getLong("Buffer1"));
        ctx.assertNull(row.getInteger(0));
        ctx.assertNull(row.getInteger("Buffer1"));
        ctx.assertNull(row.getFloat(0));
        ctx.assertNull(row.getFloat("Buffer1"));
        ctx.assertNull(row.getDouble(0));
        ctx.assertNull(row.getDouble("Buffer1"));
        ctx.assertNull(row.getCharacter(0));
        ctx.assertNull(row.getCharacter("Buffer1"));
        ctx.assertNull(row.getString(0));
        ctx.assertNull(row.getString("Buffer1"));
        ctx.assertNull(row.getJsonObject(0));
        ctx.assertNull(row.getJsonObject("Buffer1"));
        ctx.assertNull(row.getJsonArray(0));
        ctx.assertNull(row.getJsonArray("Buffer1"));
        ctx.assertNull(row.getTemporal(0));
        ctx.assertNull(row.getTemporal("Buffer1"));
        ctx.assertNull(row.getLocalDate(0));
        ctx.assertNull(row.getLocalDate("Buffer1"));
        ctx.assertNull(row.getLocalTime(0));
        ctx.assertNull(row.getLocalTime("Buffer1"));
        ctx.assertNull(row.getOffsetTime(0));
        ctx.assertNull(row.getOffsetTime("Buffer1"));
        ctx.assertNull(row.getLocalDateTime(0));
        ctx.assertNull(row.getLocalDateTime("Buffer1"));
        ctx.assertNull(row.getOffsetDateTime(0));
        ctx.assertNull(row.getOffsetDateTime("Buffer1"));

        ctx.assertEquals("\u00DE\u00AD\u00BE\u00EF", row.getBuffer(1).toString(UTF_8));
        ctx.assertEquals(Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"), row.getValue(1));
        ctx.assertEquals(Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"), row.getValue("Buffer2"));
        ctx.assertEquals(Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"), row.getBuffer(1));
        ctx.assertEquals(Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"), row.getBuffer("Buffer2"));
        ctx.assertNull(row.getBoolean(1));
        ctx.assertNull(row.getBoolean("Buffer2"));
        ctx.assertNull(row.getLong(1));
        ctx.assertNull(row.getLong("Buffer2"));
        ctx.assertNull(row.getInteger(1));
        ctx.assertNull(row.getInteger("Buffer2"));
        ctx.assertNull(row.getFloat(1));
        ctx.assertNull(row.getFloat("Buffer2"));
        ctx.assertNull(row.getDouble(1));
        ctx.assertNull(row.getDouble("Buffer2"));
        ctx.assertNull(row.getCharacter(1));
        ctx.assertNull(row.getCharacter("Buffer2"));
        ctx.assertNull(row.getString(1));
        ctx.assertNull(row.getString("Buffer2"));
        ctx.assertNull(row.getJsonObject(1));
        ctx.assertNull(row.getJsonObject("Buffer2"));
        ctx.assertNull(row.getJsonArray(1));
        ctx.assertNull(row.getJsonArray("Buffer2"));
        ctx.assertNull(row.getTemporal(1));
        ctx.assertNull(row.getTemporal("Buffer2"));
        ctx.assertNull(row.getLocalDate(1));
        ctx.assertNull(row.getLocalDate("Buffer2"));
        ctx.assertNull(row.getLocalTime(1));
        ctx.assertNull(row.getLocalTime("Buffer2"));
        ctx.assertNull(row.getOffsetTime(1));
        ctx.assertNull(row.getOffsetTime("Buffer2"));
        ctx.assertNull(row.getLocalDateTime(1));
        ctx.assertNull(row.getLocalDateTime("Buffer2"));
        ctx.assertNull(row.getOffsetDateTime(1));
        ctx.assertNull(row.getOffsetDateTime("Buffer2"));
        async.complete();
      }));
    }));
  }

}

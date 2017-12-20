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
        .createQuery("SELECT null \"NullValue\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertNull(result.iterator().next().getValue(0));
          ctx.assertNull(result.iterator().next().getValue("NullValue"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("NullValue"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("NullValue"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("NullValue"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("NullValue"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("NullValue"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("NullValue"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("NullValue"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("NullValue"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("NullValue"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("NullValue"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("NullValue"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("NullValue"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("NullValue"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("NullValue"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("NullValue"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("NullValue"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testBoolean(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT true \"TrueValue\", false \"FalseValue\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(true, result.iterator().next().getBoolean(0));
          ctx.assertEquals(true, result.iterator().next().getValue(0));
          ctx.assertEquals(true, result.iterator().next().getValue("TrueValue"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("TrueValue"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("TrueValue"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("TrueValue"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("TrueValue"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("TrueValue"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("TrueValue"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("TrueValue"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("TrueValue"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("TrueValue"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("TrueValue"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("TrueValue"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("TrueValue"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("TrueValue"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("TrueValue"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("TrueValue"));
          ctx.assertEquals(false, result.iterator().next().getBoolean(1));
          ctx.assertEquals(false, result.iterator().next().getValue(1));
          ctx.assertEquals(false, result.iterator().next().getValue("FalseValue"));
          ctx.assertNull(result.iterator().next().getLong(1));
          ctx.assertNull(result.iterator().next().getLong("FalseValue"));
          ctx.assertNull(result.iterator().next().getInteger(1));
          ctx.assertNull(result.iterator().next().getInteger("FalseValue"));
          ctx.assertNull(result.iterator().next().getFloat(1));
          ctx.assertNull(result.iterator().next().getFloat("FalseValue"));
          ctx.assertNull(result.iterator().next().getDouble(1));
          ctx.assertNull(result.iterator().next().getDouble("FalseValue"));
          ctx.assertNull(result.iterator().next().getCharacter(1));
          ctx.assertNull(result.iterator().next().getCharacter("FalseValue"));
          ctx.assertNull(result.iterator().next().getString(1));
          ctx.assertNull(result.iterator().next().getString("FalseValue"));
          ctx.assertNull(result.iterator().next().getJsonObject(1));
          ctx.assertNull(result.iterator().next().getJsonObject("FalseValue"));
          ctx.assertNull(result.iterator().next().getJsonArray(1));
          ctx.assertNull(result.iterator().next().getJsonArray("FalseValue"));
          ctx.assertNull(result.iterator().next().getBuffer(1));
          ctx.assertNull(result.iterator().next().getBuffer("FalseValue"));
          ctx.assertNull(result.iterator().next().getTemporal(1));
          ctx.assertNull(result.iterator().next().getTemporal("FalseValue"));
          ctx.assertNull(result.iterator().next().getLocalDate(1));
          ctx.assertNull(result.iterator().next().getLocalDate("FalseValue"));
          ctx.assertNull(result.iterator().next().getLocalTime(1));
          ctx.assertNull(result.iterator().next().getLocalTime("FalseValue"));
          ctx.assertNull(result.iterator().next().getOffsetTime(1));
          ctx.assertNull(result.iterator().next().getOffsetTime("FalseValue"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(1));
          ctx.assertNull(result.iterator().next().getLocalDateTime("FalseValue"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(1));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("FalseValue"));

          async.complete();
        }));
    }));
  }

  @Test
  public void testInt2(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 32767::INT2 \"Short\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals((short) 32767, result.iterator().next().getValue(0));
          ctx.assertEquals((short) 32767, result.iterator().next().getValue("Short"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Short"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Short"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Short"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Short"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Short"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Short"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("Short"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Short"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Short"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Short"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Short"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Short"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Short"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Short"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Short"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Short"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testInt4(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 2147483647::INT4 \"Integer\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(2147483647, result.iterator().next().getInteger(0));
          ctx.assertEquals(2147483647, result.iterator().next().getValue(0));
          ctx.assertEquals(2147483647, result.iterator().next().getInteger("Integer"));
          ctx.assertEquals(2147483647, result.iterator().next().getValue("Integer"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Integer"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Integer"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Integer"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Integer"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Integer"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("Integer"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Integer"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Integer"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Integer"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Integer"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Integer"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Integer"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Integer"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Integer"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Integer"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testInt8(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 9223372036854775807::INT8 \"Long\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(9223372036854775807L, result.iterator().next().getLong(0));
          ctx.assertEquals(9223372036854775807L, result.iterator().next().getValue(0));
          ctx.assertEquals(9223372036854775807L, result.iterator().next().getLong("Long"));
          ctx.assertEquals(9223372036854775807L, result.iterator().next().getValue("Long"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Long"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Long"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Long"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Long"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Long"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("Long"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Long"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Long"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Long"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Long"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Long"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Long"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Long"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Long"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Long"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testFloat4(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 3.4028235E38::FLOAT4 \"Float\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(3.4028235E38f, result.iterator().next().getFloat(0));
          ctx.assertEquals(3.4028235E38f, result.iterator().next().getValue(0));
          ctx.assertEquals(3.4028235E38f, result.iterator().next().getFloat("Float"));
          ctx.assertEquals(3.4028235E38f, result.iterator().next().getValue("Float"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Float"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Float"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Float"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Float"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Float"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("Float"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Float"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Float"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Float"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Float"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Float"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Float"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Float"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Float"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Float"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testFloat8(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 1.7976931348623157E308::FLOAT8 \"Double\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(1.7976931348623157E308d, result.iterator().next().getDouble(0));
          ctx.assertEquals(1.7976931348623157E308d, result.iterator().next().getValue(0));
          ctx.assertEquals(1.7976931348623157E308d, result.iterator().next().getDouble("Double"));
          ctx.assertEquals(1.7976931348623157E308d, result.iterator().next().getValue("Double"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Double"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Double"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Double"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Double"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Double"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("Double"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Double"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Double"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Double"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Double"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Double"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Double"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Double"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Double"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Double"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testNumeric(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 919.999999999999999999999999999999999999::NUMERIC \"Numeric\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(920.0, result.iterator().next().getDouble(0));
          ctx.assertEquals(920.0, result.iterator().next().getValue(0));
          ctx.assertEquals(920.0, result.iterator().next().getDouble("Numeric"));
          ctx.assertEquals(920.0, result.iterator().next().getValue("Numeric"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Numeric"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Numeric"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Numeric"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Numeric"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Numeric"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("Numeric"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Numeric"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Numeric"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Numeric"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Numeric"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Numeric"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Numeric"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Numeric"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Numeric"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Numeric"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testName(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 'VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X & VERT.X'::NAME \"Name\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          String name1 = result.iterator().next().getString(0);
          Object value1 = result.iterator().next().getValue(0);
          String name2 = result.iterator().next().getString("Name");
          Object value2 = result.iterator().next().getValue("Name");
          ctx.assertEquals("VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ", name1);
          ctx.assertEquals("VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ", value1);
          ctx.assertEquals("VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ", name2);
          ctx.assertEquals("VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ", value2);
          // must be 63 length
          ctx.assertEquals(63, name1.length());
          ctx.assertEquals(63, name2.length());
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Name"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Name"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Name"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Name"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Name"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Name"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Name"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Name"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Name"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Name"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Name"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Name"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Name"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Name"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Name"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testBlankPaddedChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 'pgClient'::CHAR(15) \"Char\" ")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          String char1 = result.iterator().next().getString(0);
          Object value1 = result.iterator().next().getValue(0);
          String char2 = result.iterator().next().getString("Char");
          Object value2 = result.iterator().next().getValue("Char");
          ctx.assertEquals("pgClient       ", char1);
          ctx.assertEquals("pgClient       ", value1);
          ctx.assertEquals("pgClient       ", char2);
          ctx.assertEquals("pgClient       ", value2);
          ctx.assertEquals(15, char1.length());
          ctx.assertEquals(15, char2.length());
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Char"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Char"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Char"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Char"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Char"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Char"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Char"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Char"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Char"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Char"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Char"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Char"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Char"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Char"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Char"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testSingleBlankPaddedChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 'V'::CHAR \"Char\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          String char1 = result.iterator().next().getString(0);
          Object value1 = result.iterator().next().getValue(0);
          String char2 = result.iterator().next().getString("Char");
          Object value2 = result.iterator().next().getValue("Char");
          ctx.assertEquals("V", char1);
          ctx.assertEquals("V", value1);
          ctx.assertEquals("V", char2);
          ctx.assertEquals("V", value2);
          ctx.assertEquals(1, char1.length());
          ctx.assertEquals(1, char2.length());
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Char"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Char"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Char"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Char"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Char"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Char"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Char"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Char"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Char"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Char"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Char"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Char"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Char"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Char"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Char"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testSingleChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 'X'::\"char\" \"Character\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals('X', result.iterator().next().getValue(0));
          ctx.assertEquals('X', result.iterator().next().getCharacter(0));
          ctx.assertEquals('X', result.iterator().next().getValue("Character"));
          ctx.assertEquals('X', result.iterator().next().getCharacter("Character"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Character"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Character"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Character"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Character"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Character"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("Character"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Character"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Character"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Character"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Character"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Character"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Character"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Character"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Character"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Character"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testVarChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 'pgClient'::VARCHAR(15) \"Driver\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals("pgClient", result.iterator().next().getString(0));
          ctx.assertEquals("pgClient", result.iterator().next().getValue(0));
          ctx.assertEquals("pgClient", result.iterator().next().getString("Driver"));
          ctx.assertEquals("pgClient", result.iterator().next().getValue("Driver"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Driver"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Driver"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Driver"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Driver"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Driver"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Driver"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Driver"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Driver"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Driver"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Driver"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Driver"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Driver"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Driver"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Driver"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Driver"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testText(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT 'Vert.x PostgreSQL Client'::TEXT \"Text\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals("Vert.x PostgreSQL Client", result.iterator().next().getString(0));
          ctx.assertEquals("Vert.x PostgreSQL Client", result.iterator().next().getValue(0));
          ctx.assertEquals("Vert.x PostgreSQL Client", result.iterator().next().getString("Text"));
          ctx.assertEquals("Vert.x PostgreSQL Client", result.iterator().next().getValue("Text"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Text"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Text"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Text"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Text"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Text"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Text"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Text"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Text"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Text"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Text"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Text"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Text"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Text"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Text"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Text"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testUUID(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT '50867d3d-0098-4f61-bd31-9309ebf53475'::UUID \"uuid\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          String uuid = "50867d3d-0098-4f61-bd31-9309ebf53475";
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(uuid, result.iterator().next().getString(0));
          ctx.assertEquals(uuid, result.iterator().next().getValue(0));
          ctx.assertEquals(uuid, result.iterator().next().getString("uuid"));
          ctx.assertEquals(uuid, result.iterator().next().getValue("uuid"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("uuid"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("uuid"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("uuid"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("uuid"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("uuid"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("uuid"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("uuid"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("uuid"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("uuid"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("uuid"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("uuid"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("uuid"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("uuid"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("uuid"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("uuid"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testDate(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT '1981-05-30'::DATE \"LocalDate\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          LocalDate ld = LocalDate.parse("1981-05-30");
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(ld, result.iterator().next().getLocalDate(0));
          ctx.assertEquals(ld, result.iterator().next().getTemporal(0));
          ctx.assertEquals(ld, result.iterator().next().getValue(0));
          ctx.assertEquals(ld, result.iterator().next().getLocalDate("LocalDate"));
          ctx.assertEquals(ld, result.iterator().next().getTemporal("LocalDate"));
          ctx.assertEquals(ld, result.iterator().next().getValue("LocalDate"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("LocalDate"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("LocalDate"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("LocalDate"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("LocalDate"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("LocalDate"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("LocalDate"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("LocalDate"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("LocalDate"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("LocalDate"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("LocalDate"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("LocalDate"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("LocalDate"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("LocalDate"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("LocalDate"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testTime(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT '17:55:04.905120'::TIME \"LocalTime\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          LocalTime lt = LocalTime.parse("17:55:04.905120");
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(lt, result.iterator().next().getLocalTime(0));
          ctx.assertEquals(lt, result.iterator().next().getTemporal(0));
          ctx.assertEquals(lt, result.iterator().next().getValue(0));
          ctx.assertEquals(lt, result.iterator().next().getLocalTime("LocalTime"));
          ctx.assertEquals(lt, result.iterator().next().getTemporal("LocalTime"));
          ctx.assertEquals(lt, result.iterator().next().getValue("LocalTime"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("LocalTime"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("LocalTime"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("LocalTime"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("LocalTime"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("LocalTime"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("LocalTime"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("LocalTime"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("LocalTime"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("LocalTime"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("LocalTime"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("LocalTime"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("LocalTime"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("LocalTime"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("LocalTime"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimeTz(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT '17:55:04.90512+03:07'::TIMETZ \"OffsetTime\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          OffsetTime ot = OffsetTime.parse("17:55:04.905120+03:07");
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(ot, result.iterator().next().getOffsetTime(0));
          ctx.assertEquals(ot, result.iterator().next().getTemporal(0));
          ctx.assertEquals(ot, result.iterator().next().getValue(0));
          ctx.assertEquals(ot, result.iterator().next().getOffsetTime("OffsetTime"));
          ctx.assertEquals(ot, result.iterator().next().getTemporal("OffsetTime"));
          ctx.assertEquals(ot, result.iterator().next().getValue("OffsetTime"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("OffsetTime"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("OffsetTime"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("OffsetTime"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("OffsetTime"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("OffsetTime"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("OffsetTime"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("OffsetTime"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("OffsetTime"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("OffsetTime"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("OffsetTime"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("OffsetTime"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("OffsetTime"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("OffsetTime"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("OffsetTime"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimestamp(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .createQuery("SELECT '2017-05-14 19:35:58.237666'::TIMESTAMP \"LocalDateTime\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(ldt, result.iterator().next().getLocalDateTime(0));
          ctx.assertEquals(ldt, result.iterator().next().getTemporal(0));
          ctx.assertEquals(ldt, result.iterator().next().getValue(0));
          ctx.assertEquals(ldt, result.iterator().next().getLocalDateTime("LocalDateTime"));
          ctx.assertEquals(ldt, result.iterator().next().getTemporal("LocalDateTime"));
          ctx.assertEquals(ldt, result.iterator().next().getValue("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("LocalDateTime"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("LocalDateTime"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimestampTz(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SET TIME ZONE 'UTC'").execute(ctx.asyncAssertSuccess(v -> {
        conn.createQuery("SELECT '2017-05-14 22:35:58.237666-03'::TIMESTAMPTZ \"OffsetDateTime\"").execute(
          ctx.asyncAssertSuccess(result -> {
            OffsetDateTime odt = OffsetDateTime.parse("2017-05-15T01:35:58.237666Z");
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(odt, result.iterator().next().getOffsetDateTime(0));
            ctx.assertEquals(odt, result.iterator().next().getTemporal(0));
            ctx.assertEquals(odt, result.iterator().next().getValue(0));
            ctx.assertEquals(odt, result.iterator().next().getOffsetDateTime("OffsetDateTime"));
            ctx.assertEquals(odt, result.iterator().next().getTemporal("OffsetDateTime"));
            ctx.assertEquals(odt, result.iterator().next().getValue("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getBoolean(0));
            ctx.assertNull(result.iterator().next().getBoolean("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getLong(0));
            ctx.assertNull(result.iterator().next().getLong("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getInteger(0));
            ctx.assertNull(result.iterator().next().getInteger("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getFloat(0));
            ctx.assertNull(result.iterator().next().getFloat("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getDouble(0));
            ctx.assertNull(result.iterator().next().getDouble("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getCharacter(0));
            ctx.assertNull(result.iterator().next().getCharacter("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getString(0));
            ctx.assertNull(result.iterator().next().getString("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getJsonObject(0));
            ctx.assertNull(result.iterator().next().getJsonObject("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getJsonArray(0));
            ctx.assertNull(result.iterator().next().getJsonArray("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getBuffer(0));
            ctx.assertNull(result.iterator().next().getBuffer("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getLocalDate(0));
            ctx.assertNull(result.iterator().next().getLocalDate("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getLocalTime(0));
            ctx.assertNull(result.iterator().next().getLocalTime("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getOffsetTime(0));
            ctx.assertNull(result.iterator().next().getOffsetTime("OffsetDateTime"));
            ctx.assertNull(result.iterator().next().getLocalDateTime(0));
            ctx.assertNull(result.iterator().next().getLocalDateTime("OffsetDateTime"));
            async.complete();
          }));
      }));
    }));
  }

  @Test
  public void testJsonbObject(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT '  {\"str\":\"blah\", \"int\" : 1, \"float\" :" +
        " 3.5, \"object\": {}, \"array\" : []   }'::JSONB \"JsonObject\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
            " 3.5, \"object\": {}, \"array\" : []}");
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(object, result.iterator().next().getValue(0));
          ctx.assertEquals(object, result.iterator().next().getJsonObject(0));
          ctx.assertEquals(object, result.iterator().next().getValue("JsonObject"));
          ctx.assertEquals(object, result.iterator().next().getJsonObject("JsonObject"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("JsonObject"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("JsonObject"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("JsonObject"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("JsonObject"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("JsonObject"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("JsonObject"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("JsonObject"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("JsonObject"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("JsonObject"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("JsonObject"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("JsonObject"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("JsonObject"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("JsonObject"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("JsonObject"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("JsonObject"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonbArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT '  [1,true,null,9.5,\"Hi\" ] '::JSONB").execute(
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          JsonArray jsonArray = result.iterator().next().getJsonArray(0);
          Object value = result.iterator().next().getValue(0);
          ctx.assertEquals(new JsonArray("[1,true,null,9.5,\"Hi\"]"), jsonArray);
          ctx.assertEquals(new JsonArray("[1,true,null,9.5,\"Hi\"]"), value);
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonObject(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT '    {\"str\":\"blah\", \"int\" : 1, \"float\" :" +
        " 3.5, \"object\": {}, \"array\" : []  }    '::JSON \"JsonObject\"").execute(
        ctx.asyncAssertSuccess(result -> {
          JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" :" +
            " 3.5, \"object\": {}, \"array\" : []}");
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(object, result.iterator().next().getValue(0));
          ctx.assertEquals(object, result.iterator().next().getJsonObject(0));
          ctx.assertEquals(object, result.iterator().next().getValue("JsonObject"));
          ctx.assertEquals(object, result.iterator().next().getJsonObject("JsonObject"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("JsonObject"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("JsonObject"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("JsonObject"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("JsonObject"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("JsonObject"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("JsonObject"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("JsonObject"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("JsonObject"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("JsonObject"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("JsonObject"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("JsonObject"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("JsonObject"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("JsonObject"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("JsonObject"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("JsonObject"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT '     [1,true,null,9.5,\"Hi\"]     '::JSON \"Array\"").execute(
        ctx.asyncAssertSuccess(result -> {
          JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(array, result.iterator().next().getValue(0));
          ctx.assertEquals(array, result.iterator().next().getJsonArray(0));
          ctx.assertEquals(array, result.iterator().next().getValue("Array"));
          ctx.assertEquals(array, result.iterator().next().getJsonArray("Array"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Array"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Array"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Array"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Array"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Array"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Array"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("Array"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Array"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("Array"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Array"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Array"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Array"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Array"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Array"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Array"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonbScalar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT ' true '::JSONB \"TrueValue\", ' false '::JSONB \"FalseValue\", ' null '::JSONB \"NullValue\", ' 7.502 '::JSONB \"Number1\", ' 8 '::JSONB \"Number2\", '\" Really Awesome! \"'::JSONB \"Text\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(true, result.iterator().next().getBoolean(0));
          ctx.assertEquals(true, result.iterator().next().getValue(0));
          ctx.assertEquals(true, result.iterator().next().getBoolean("TrueValue"));
          ctx.assertEquals(true, result.iterator().next().getValue("TrueValue"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("TrueValue"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("TrueValue"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("TrueValue"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("TrueValue"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("TrueValue"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("TrueValue"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("TrueValue"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("TrueValue"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("TrueValue"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("TrueValue"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("TrueValue"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("TrueValue"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("TrueValue"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("TrueValue"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("TrueValue"));

          ctx.assertEquals(false, result.iterator().next().getBoolean(1));
          ctx.assertEquals(false, result.iterator().next().getValue(1));
          ctx.assertEquals(false, result.iterator().next().getBoolean("FalseValue"));
          ctx.assertEquals(false, result.iterator().next().getValue("FalseValue"));
          ctx.assertNull(result.iterator().next().getLong(1));
          ctx.assertNull(result.iterator().next().getLong("FalseValue"));
          ctx.assertNull(result.iterator().next().getInteger(1));
          ctx.assertNull(result.iterator().next().getInteger("FalseValue"));
          ctx.assertNull(result.iterator().next().getFloat(1));
          ctx.assertNull(result.iterator().next().getFloat("FalseValue"));
          ctx.assertNull(result.iterator().next().getDouble(1));
          ctx.assertNull(result.iterator().next().getDouble("FalseValue"));
          ctx.assertNull(result.iterator().next().getCharacter(1));
          ctx.assertNull(result.iterator().next().getCharacter("FalseValue"));
          ctx.assertNull(result.iterator().next().getString(1));
          ctx.assertNull(result.iterator().next().getString("FalseValue"));
          ctx.assertNull(result.iterator().next().getJsonObject(1));
          ctx.assertNull(result.iterator().next().getJsonObject("FalseValue"));
          ctx.assertNull(result.iterator().next().getJsonArray(1));
          ctx.assertNull(result.iterator().next().getJsonArray("FalseValue"));
          ctx.assertNull(result.iterator().next().getBuffer(1));
          ctx.assertNull(result.iterator().next().getBuffer("FalseValue"));
          ctx.assertNull(result.iterator().next().getTemporal(1));
          ctx.assertNull(result.iterator().next().getTemporal("FalseValue"));
          ctx.assertNull(result.iterator().next().getLocalDate(1));
          ctx.assertNull(result.iterator().next().getLocalDate("FalseValue"));
          ctx.assertNull(result.iterator().next().getLocalTime(1));
          ctx.assertNull(result.iterator().next().getLocalTime("FalseValue"));
          ctx.assertNull(result.iterator().next().getOffsetTime(1));
          ctx.assertNull(result.iterator().next().getOffsetTime("FalseValue"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(1));
          ctx.assertNull(result.iterator().next().getLocalDateTime("FalseValue"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(1));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("FalseValue"));

          ctx.assertNull(result.iterator().next().getValue(2));
          ctx.assertNull(result.iterator().next().getValue("NullValue"));
          ctx.assertNull(result.iterator().next().getBoolean(2));
          ctx.assertNull(result.iterator().next().getBoolean("NullValue"));
          ctx.assertNull(result.iterator().next().getLong(2));
          ctx.assertNull(result.iterator().next().getLong("NullValue"));
          ctx.assertNull(result.iterator().next().getInteger(2));
          ctx.assertNull(result.iterator().next().getInteger("NullValue"));
          ctx.assertNull(result.iterator().next().getFloat(2));
          ctx.assertNull(result.iterator().next().getFloat("NullValue"));
          ctx.assertNull(result.iterator().next().getDouble(2));
          ctx.assertNull(result.iterator().next().getDouble("NullValue"));
          ctx.assertNull(result.iterator().next().getCharacter(2));
          ctx.assertNull(result.iterator().next().getCharacter("NullValue"));
          ctx.assertNull(result.iterator().next().getString(2));
          ctx.assertNull(result.iterator().next().getString("NullValue"));
          ctx.assertNull(result.iterator().next().getJsonObject(2));
          ctx.assertNull(result.iterator().next().getJsonObject("NullValue"));
          ctx.assertNull(result.iterator().next().getJsonArray(2));
          ctx.assertNull(result.iterator().next().getJsonArray("NullValue"));
          ctx.assertNull(result.iterator().next().getBuffer(2));
          ctx.assertNull(result.iterator().next().getBuffer("NullValue"));
          ctx.assertNull(result.iterator().next().getTemporal(2));
          ctx.assertNull(result.iterator().next().getTemporal("NullValue"));
          ctx.assertNull(result.iterator().next().getLocalDate(2));
          ctx.assertNull(result.iterator().next().getLocalDate("NullValue"));
          ctx.assertNull(result.iterator().next().getLocalTime(2));
          ctx.assertNull(result.iterator().next().getLocalTime("NullValue"));
          ctx.assertNull(result.iterator().next().getOffsetTime(2));
          ctx.assertNull(result.iterator().next().getOffsetTime("NullValue"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(2));
          ctx.assertNull(result.iterator().next().getLocalDateTime("NullValue"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(2));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("NullValue"));

          // ctx.assertEquals(7.502f, row.getFloat(3));
          ctx.assertEquals(7.502d, result.iterator().next().getDouble(3));
          ctx.assertEquals(7.502d, result.iterator().next().getValue(3));
          ctx.assertEquals(7.502d, result.iterator().next().getDouble("Number1"));
          ctx.assertEquals(7.502d, result.iterator().next().getValue("Number1"));
          ctx.assertNull(result.iterator().next().getBoolean(3));
          ctx.assertNull(result.iterator().next().getBoolean("Number1"));
          ctx.assertNull(result.iterator().next().getLong(3));
          ctx.assertNull(result.iterator().next().getLong("Number1"));
          ctx.assertNull(result.iterator().next().getInteger(3));
          ctx.assertNull(result.iterator().next().getInteger("Number1"));
          ctx.assertNull(result.iterator().next().getFloat(3));
          ctx.assertNull(result.iterator().next().getFloat("Number1"));
          ctx.assertNull(result.iterator().next().getCharacter(3));
          ctx.assertNull(result.iterator().next().getCharacter("Number1"));
          ctx.assertNull(result.iterator().next().getString(3));
          ctx.assertNull(result.iterator().next().getString("Number1"));
          ctx.assertNull(result.iterator().next().getJsonObject(3));
          ctx.assertNull(result.iterator().next().getJsonObject("Number1"));
          ctx.assertNull(result.iterator().next().getJsonArray(3));
          ctx.assertNull(result.iterator().next().getJsonArray("Number1"));
          ctx.assertNull(result.iterator().next().getBuffer(3));
          ctx.assertNull(result.iterator().next().getBuffer("Number1"));
          ctx.assertNull(result.iterator().next().getTemporal(3));
          ctx.assertNull(result.iterator().next().getTemporal("Number1"));
          ctx.assertNull(result.iterator().next().getLocalDate(3));
          ctx.assertNull(result.iterator().next().getLocalDate("Number1"));
          ctx.assertNull(result.iterator().next().getLocalTime(3));
          ctx.assertNull(result.iterator().next().getLocalTime("Number1"));
          ctx.assertNull(result.iterator().next().getOffsetTime(3));
          ctx.assertNull(result.iterator().next().getOffsetTime("Number1"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(3));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Number1"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(3));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Number1"));

          ctx.assertEquals(8, result.iterator().next().getInteger(4));
          ctx.assertEquals(8, result.iterator().next().getValue(4));
          ctx.assertEquals(8, result.iterator().next().getInteger("Number2"));
          ctx.assertEquals(8, result.iterator().next().getValue("Number2"));
          ctx.assertNull(result.iterator().next().getBoolean(4));
          ctx.assertNull(result.iterator().next().getBoolean("Number2"));
          ctx.assertNull(result.iterator().next().getLong(4));
          ctx.assertNull(result.iterator().next().getLong("Number2"));
          ctx.assertNull(result.iterator().next().getFloat(4));
          ctx.assertNull(result.iterator().next().getFloat("Number2"));
          ctx.assertNull(result.iterator().next().getDouble(4));
          ctx.assertNull(result.iterator().next().getDouble("Number2"));
          ctx.assertNull(result.iterator().next().getCharacter(4));
          ctx.assertNull(result.iterator().next().getCharacter("Number2"));
          ctx.assertNull(result.iterator().next().getString(4));
          ctx.assertNull(result.iterator().next().getString("Number2"));
          ctx.assertNull(result.iterator().next().getJsonObject(4));
          ctx.assertNull(result.iterator().next().getJsonObject("Number2"));
          ctx.assertNull(result.iterator().next().getJsonArray(4));
          ctx.assertNull(result.iterator().next().getJsonArray("Number2"));
          ctx.assertNull(result.iterator().next().getBuffer(4));
          ctx.assertNull(result.iterator().next().getBuffer("Number2"));
          ctx.assertNull(result.iterator().next().getTemporal(4));
          ctx.assertNull(result.iterator().next().getTemporal("Number2"));
          ctx.assertNull(result.iterator().next().getLocalDate(4));
          ctx.assertNull(result.iterator().next().getLocalDate("Number2"));
          ctx.assertNull(result.iterator().next().getLocalTime(4));
          ctx.assertNull(result.iterator().next().getLocalTime("Number2"));
          ctx.assertNull(result.iterator().next().getOffsetTime(4));
          ctx.assertNull(result.iterator().next().getOffsetTime("Number2"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(4));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Number2"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(4));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Number2"));

          // ctx.assertEquals(8L, row.getLong(4));
          ctx.assertEquals(" Really Awesome! ", result.iterator().next().getString(5));
          ctx.assertEquals(" Really Awesome! ", result.iterator().next().getValue(5));
          ctx.assertEquals(" Really Awesome! ", result.iterator().next().getString("Text"));
          ctx.assertEquals(" Really Awesome! ", result.iterator().next().getValue("Text"));
          ctx.assertNull(result.iterator().next().getBoolean(5));
          ctx.assertNull(result.iterator().next().getBoolean("Text"));
          ctx.assertNull(result.iterator().next().getLong(5));
          ctx.assertNull(result.iterator().next().getLong("Text"));
          ctx.assertNull(result.iterator().next().getInteger(5));
          ctx.assertNull(result.iterator().next().getInteger("Text"));
          ctx.assertNull(result.iterator().next().getFloat(5));
          ctx.assertNull(result.iterator().next().getFloat("Text"));
          ctx.assertNull(result.iterator().next().getDouble(5));
          ctx.assertNull(result.iterator().next().getDouble("Text"));
          ctx.assertNull(result.iterator().next().getCharacter(5));
          ctx.assertNull(result.iterator().next().getCharacter("Text"));
          ctx.assertNull(result.iterator().next().getJsonObject(5));
          ctx.assertNull(result.iterator().next().getJsonObject("Text"));
          ctx.assertNull(result.iterator().next().getJsonArray(5));
          ctx.assertNull(result.iterator().next().getJsonArray("Text"));
          ctx.assertNull(result.iterator().next().getBuffer(5));
          ctx.assertNull(result.iterator().next().getBuffer("Text"));
          ctx.assertNull(result.iterator().next().getTemporal(5));
          ctx.assertNull(result.iterator().next().getTemporal("Text"));
          ctx.assertNull(result.iterator().next().getLocalDate(5));
          ctx.assertNull(result.iterator().next().getLocalDate("Text"));
          ctx.assertNull(result.iterator().next().getLocalTime(5));
          ctx.assertNull(result.iterator().next().getLocalTime("Text"));
          ctx.assertNull(result.iterator().next().getOffsetTime(5));
          ctx.assertNull(result.iterator().next().getOffsetTime("Text"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(5));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Text"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(5));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Text"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testJsonScalar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT ' true '::JSON \"TrueValue\", ' false '::JSON \"FalseValue\", ' null '::JSON \"NullValue\", ' 7.502 '::JSON \"Number1\", ' 8 '::JSON \"Number2\", '\" Really Awesome! \"'::JSON \"Text\"")
        .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(true, result.iterator().next().getBoolean(0));
          ctx.assertEquals(true, result.iterator().next().getValue(0));
          ctx.assertEquals(true, result.iterator().next().getBoolean("TrueValue"));
          ctx.assertEquals(true, result.iterator().next().getValue("TrueValue"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("TrueValue"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("TrueValue"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("TrueValue"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("TrueValue"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("TrueValue"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("TrueValue"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("TrueValue"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("TrueValue"));
          ctx.assertNull(result.iterator().next().getBuffer(0));
          ctx.assertNull(result.iterator().next().getBuffer("TrueValue"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("TrueValue"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("TrueValue"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("TrueValue"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("TrueValue"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("TrueValue"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("TrueValue"));

          ctx.assertEquals(false, result.iterator().next().getBoolean(1));
          ctx.assertEquals(false, result.iterator().next().getValue(1));
          ctx.assertEquals(false, result.iterator().next().getBoolean("FalseValue"));
          ctx.assertEquals(false, result.iterator().next().getValue("FalseValue"));
          ctx.assertNull(result.iterator().next().getLong(1));
          ctx.assertNull(result.iterator().next().getLong("FalseValue"));
          ctx.assertNull(result.iterator().next().getInteger(1));
          ctx.assertNull(result.iterator().next().getInteger("FalseValue"));
          ctx.assertNull(result.iterator().next().getFloat(1));
          ctx.assertNull(result.iterator().next().getFloat("FalseValue"));
          ctx.assertNull(result.iterator().next().getDouble(1));
          ctx.assertNull(result.iterator().next().getDouble("FalseValue"));
          ctx.assertNull(result.iterator().next().getCharacter(1));
          ctx.assertNull(result.iterator().next().getCharacter("FalseValue"));
          ctx.assertNull(result.iterator().next().getString(1));
          ctx.assertNull(result.iterator().next().getString("FalseValue"));
          ctx.assertNull(result.iterator().next().getJsonObject(1));
          ctx.assertNull(result.iterator().next().getJsonObject("FalseValue"));
          ctx.assertNull(result.iterator().next().getJsonArray(1));
          ctx.assertNull(result.iterator().next().getJsonArray("FalseValue"));
          ctx.assertNull(result.iterator().next().getBuffer(1));
          ctx.assertNull(result.iterator().next().getBuffer("FalseValue"));
          ctx.assertNull(result.iterator().next().getTemporal(1));
          ctx.assertNull(result.iterator().next().getTemporal("FalseValue"));
          ctx.assertNull(result.iterator().next().getLocalDate(1));
          ctx.assertNull(result.iterator().next().getLocalDate("FalseValue"));
          ctx.assertNull(result.iterator().next().getLocalTime(1));
          ctx.assertNull(result.iterator().next().getLocalTime("FalseValue"));
          ctx.assertNull(result.iterator().next().getOffsetTime(1));
          ctx.assertNull(result.iterator().next().getOffsetTime("FalseValue"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(1));
          ctx.assertNull(result.iterator().next().getLocalDateTime("FalseValue"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(1));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("FalseValue"));

          ctx.assertNull(result.iterator().next().getValue(2));
          ctx.assertNull(result.iterator().next().getValue("NullValue"));
          ctx.assertNull(result.iterator().next().getBoolean(2));
          ctx.assertNull(result.iterator().next().getBoolean("NullValue"));
          ctx.assertNull(result.iterator().next().getLong(2));
          ctx.assertNull(result.iterator().next().getLong("NullValue"));
          ctx.assertNull(result.iterator().next().getInteger(2));
          ctx.assertNull(result.iterator().next().getInteger("NullValue"));
          ctx.assertNull(result.iterator().next().getFloat(2));
          ctx.assertNull(result.iterator().next().getFloat("NullValue"));
          ctx.assertNull(result.iterator().next().getDouble(2));
          ctx.assertNull(result.iterator().next().getDouble("NullValue"));
          ctx.assertNull(result.iterator().next().getCharacter(2));
          ctx.assertNull(result.iterator().next().getCharacter("NullValue"));
          ctx.assertNull(result.iterator().next().getString(2));
          ctx.assertNull(result.iterator().next().getString("NullValue"));
          ctx.assertNull(result.iterator().next().getJsonObject(2));
          ctx.assertNull(result.iterator().next().getJsonObject("NullValue"));
          ctx.assertNull(result.iterator().next().getJsonArray(2));
          ctx.assertNull(result.iterator().next().getJsonArray("NullValue"));
          ctx.assertNull(result.iterator().next().getBuffer(2));
          ctx.assertNull(result.iterator().next().getBuffer("NullValue"));
          ctx.assertNull(result.iterator().next().getTemporal(2));
          ctx.assertNull(result.iterator().next().getTemporal("NullValue"));
          ctx.assertNull(result.iterator().next().getLocalDate(2));
          ctx.assertNull(result.iterator().next().getLocalDate("NullValue"));
          ctx.assertNull(result.iterator().next().getLocalTime(2));
          ctx.assertNull(result.iterator().next().getLocalTime("NullValue"));
          ctx.assertNull(result.iterator().next().getOffsetTime(2));
          ctx.assertNull(result.iterator().next().getOffsetTime("NullValue"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(2));
          ctx.assertNull(result.iterator().next().getLocalDateTime("NullValue"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(2));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("NullValue"));

          // ctx.assertEquals(7.502f, row.getFloat(3));
          ctx.assertEquals(7.502d, result.iterator().next().getDouble(3));
          ctx.assertEquals(7.502d, result.iterator().next().getValue(3));
          ctx.assertEquals(7.502d, result.iterator().next().getDouble("Number1"));
          ctx.assertEquals(7.502d, result.iterator().next().getValue("Number1"));
          ctx.assertNull(result.iterator().next().getBoolean(3));
          ctx.assertNull(result.iterator().next().getBoolean("Number1"));
          ctx.assertNull(result.iterator().next().getLong(3));
          ctx.assertNull(result.iterator().next().getLong("Number1"));
          ctx.assertNull(result.iterator().next().getInteger(3));
          ctx.assertNull(result.iterator().next().getInteger("Number1"));
          ctx.assertNull(result.iterator().next().getFloat(3));
          ctx.assertNull(result.iterator().next().getFloat("Number1"));
          ctx.assertNull(result.iterator().next().getCharacter(3));
          ctx.assertNull(result.iterator().next().getCharacter("Number1"));
          ctx.assertNull(result.iterator().next().getString(3));
          ctx.assertNull(result.iterator().next().getString("Number1"));
          ctx.assertNull(result.iterator().next().getJsonObject(3));
          ctx.assertNull(result.iterator().next().getJsonObject("Number1"));
          ctx.assertNull(result.iterator().next().getJsonArray(3));
          ctx.assertNull(result.iterator().next().getJsonArray("Number1"));
          ctx.assertNull(result.iterator().next().getBuffer(3));
          ctx.assertNull(result.iterator().next().getBuffer("Number1"));
          ctx.assertNull(result.iterator().next().getTemporal(3));
          ctx.assertNull(result.iterator().next().getTemporal("Number1"));
          ctx.assertNull(result.iterator().next().getLocalDate(3));
          ctx.assertNull(result.iterator().next().getLocalDate("Number1"));
          ctx.assertNull(result.iterator().next().getLocalTime(3));
          ctx.assertNull(result.iterator().next().getLocalTime("Number1"));
          ctx.assertNull(result.iterator().next().getOffsetTime(3));
          ctx.assertNull(result.iterator().next().getOffsetTime("Number1"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(3));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Number1"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(3));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Number1"));

          ctx.assertEquals(8, result.iterator().next().getInteger(4));
          ctx.assertEquals(8, result.iterator().next().getValue(4));
          ctx.assertEquals(8, result.iterator().next().getInteger("Number2"));
          ctx.assertEquals(8, result.iterator().next().getValue("Number2"));
          ctx.assertNull(result.iterator().next().getBoolean(4));
          ctx.assertNull(result.iterator().next().getBoolean("Number2"));
          ctx.assertNull(result.iterator().next().getLong(4));
          ctx.assertNull(result.iterator().next().getLong("Number2"));
          ctx.assertNull(result.iterator().next().getFloat(4));
          ctx.assertNull(result.iterator().next().getFloat("Number2"));
          ctx.assertNull(result.iterator().next().getDouble(4));
          ctx.assertNull(result.iterator().next().getDouble("Number2"));
          ctx.assertNull(result.iterator().next().getCharacter(4));
          ctx.assertNull(result.iterator().next().getCharacter("Number2"));
          ctx.assertNull(result.iterator().next().getString(4));
          ctx.assertNull(result.iterator().next().getString("Number2"));
          ctx.assertNull(result.iterator().next().getJsonObject(4));
          ctx.assertNull(result.iterator().next().getJsonObject("Number2"));
          ctx.assertNull(result.iterator().next().getJsonArray(4));
          ctx.assertNull(result.iterator().next().getJsonArray("Number2"));
          ctx.assertNull(result.iterator().next().getBuffer(4));
          ctx.assertNull(result.iterator().next().getBuffer("Number2"));
          ctx.assertNull(result.iterator().next().getTemporal(4));
          ctx.assertNull(result.iterator().next().getTemporal("Number2"));
          ctx.assertNull(result.iterator().next().getLocalDate(4));
          ctx.assertNull(result.iterator().next().getLocalDate("Number2"));
          ctx.assertNull(result.iterator().next().getLocalTime(4));
          ctx.assertNull(result.iterator().next().getLocalTime("Number2"));
          ctx.assertNull(result.iterator().next().getOffsetTime(4));
          ctx.assertNull(result.iterator().next().getOffsetTime("Number2"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(4));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Number2"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(4));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Number2"));

          // ctx.assertEquals(8L, row.getLong(4));
          ctx.assertEquals(" Really Awesome! ", result.iterator().next().getString(5));
          ctx.assertEquals(" Really Awesome! ", result.iterator().next().getValue(5));
          ctx.assertEquals(" Really Awesome! ", result.iterator().next().getString("Text"));
          ctx.assertEquals(" Really Awesome! ", result.iterator().next().getValue("Text"));
          ctx.assertNull(result.iterator().next().getBoolean(5));
          ctx.assertNull(result.iterator().next().getBoolean("Text"));
          ctx.assertNull(result.iterator().next().getLong(5));
          ctx.assertNull(result.iterator().next().getLong("Text"));
          ctx.assertNull(result.iterator().next().getInteger(5));
          ctx.assertNull(result.iterator().next().getInteger("Text"));
          ctx.assertNull(result.iterator().next().getFloat(5));
          ctx.assertNull(result.iterator().next().getFloat("Text"));
          ctx.assertNull(result.iterator().next().getDouble(5));
          ctx.assertNull(result.iterator().next().getDouble("Text"));
          ctx.assertNull(result.iterator().next().getCharacter(5));
          ctx.assertNull(result.iterator().next().getCharacter("Text"));
          ctx.assertNull(result.iterator().next().getJsonObject(5));
          ctx.assertNull(result.iterator().next().getJsonObject("Text"));
          ctx.assertNull(result.iterator().next().getJsonArray(5));
          ctx.assertNull(result.iterator().next().getJsonArray("Text"));
          ctx.assertNull(result.iterator().next().getBuffer(5));
          ctx.assertNull(result.iterator().next().getBuffer("Text"));
          ctx.assertNull(result.iterator().next().getTemporal(5));
          ctx.assertNull(result.iterator().next().getTemporal("Text"));
          ctx.assertNull(result.iterator().next().getLocalDate(5));
          ctx.assertNull(result.iterator().next().getLocalDate("Text"));
          ctx.assertNull(result.iterator().next().getLocalTime(5));
          ctx.assertNull(result.iterator().next().getLocalTime("Text"));
          ctx.assertNull(result.iterator().next().getOffsetTime(5));
          ctx.assertNull(result.iterator().next().getOffsetTime("Text"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(5));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Text"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(5));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Text"));
          async.complete();
        }));
    }));
  }

  @Test
  public void testBytea(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT '12345678910'::BYTEA \"Buffer1\", '\u00DE\u00AD\u00BE\u00EF'::BYTEA \"Buffer2\"").execute(
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals("12345678910", result.iterator().next().getBuffer(0).toString(UTF_8));
          ctx.assertEquals(Buffer.buffer("12345678910"), result.iterator().next().getValue(0));
          ctx.assertEquals(Buffer.buffer("12345678910"), result.iterator().next().getValue("Buffer1"));
          ctx.assertEquals(Buffer.buffer("12345678910"), result.iterator().next().getBuffer(0));
          ctx.assertEquals(Buffer.buffer("12345678910"), result.iterator().next().getBuffer("Buffer1"));
          ctx.assertNull(result.iterator().next().getBoolean(0));
          ctx.assertNull(result.iterator().next().getBoolean("Buffer1"));
          ctx.assertNull(result.iterator().next().getLong(0));
          ctx.assertNull(result.iterator().next().getLong("Buffer1"));
          ctx.assertNull(result.iterator().next().getInteger(0));
          ctx.assertNull(result.iterator().next().getInteger("Buffer1"));
          ctx.assertNull(result.iterator().next().getFloat(0));
          ctx.assertNull(result.iterator().next().getFloat("Buffer1"));
          ctx.assertNull(result.iterator().next().getDouble(0));
          ctx.assertNull(result.iterator().next().getDouble("Buffer1"));
          ctx.assertNull(result.iterator().next().getCharacter(0));
          ctx.assertNull(result.iterator().next().getCharacter("Buffer1"));
          ctx.assertNull(result.iterator().next().getString(0));
          ctx.assertNull(result.iterator().next().getString("Buffer1"));
          ctx.assertNull(result.iterator().next().getJsonObject(0));
          ctx.assertNull(result.iterator().next().getJsonObject("Buffer1"));
          ctx.assertNull(result.iterator().next().getJsonArray(0));
          ctx.assertNull(result.iterator().next().getJsonArray("Buffer1"));
          ctx.assertNull(result.iterator().next().getTemporal(0));
          ctx.assertNull(result.iterator().next().getTemporal("Buffer1"));
          ctx.assertNull(result.iterator().next().getLocalDate(0));
          ctx.assertNull(result.iterator().next().getLocalDate("Buffer1"));
          ctx.assertNull(result.iterator().next().getLocalTime(0));
          ctx.assertNull(result.iterator().next().getLocalTime("Buffer1"));
          ctx.assertNull(result.iterator().next().getOffsetTime(0));
          ctx.assertNull(result.iterator().next().getOffsetTime("Buffer1"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(0));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Buffer1"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Buffer1"));

          ctx.assertEquals("\u00DE\u00AD\u00BE\u00EF", result.iterator().next().getBuffer(1).toString(UTF_8));
          ctx.assertEquals(Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"), result.iterator().next().getValue(1));
          ctx.assertEquals(Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"), result.iterator().next().getValue("Buffer2"));
          ctx.assertEquals(Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"), result.iterator().next().getBuffer(1));
          ctx.assertEquals(Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"), result.iterator().next().getBuffer("Buffer2"));
          ctx.assertNull(result.iterator().next().getBoolean(1));
          ctx.assertNull(result.iterator().next().getBoolean("Buffer2"));
          ctx.assertNull(result.iterator().next().getLong(1));
          ctx.assertNull(result.iterator().next().getLong("Buffer2"));
          ctx.assertNull(result.iterator().next().getInteger(1));
          ctx.assertNull(result.iterator().next().getInteger("Buffer2"));
          ctx.assertNull(result.iterator().next().getFloat(1));
          ctx.assertNull(result.iterator().next().getFloat("Buffer2"));
          ctx.assertNull(result.iterator().next().getDouble(1));
          ctx.assertNull(result.iterator().next().getDouble("Buffer2"));
          ctx.assertNull(result.iterator().next().getCharacter(1));
          ctx.assertNull(result.iterator().next().getCharacter("Buffer2"));
          ctx.assertNull(result.iterator().next().getString(1));
          ctx.assertNull(result.iterator().next().getString("Buffer2"));
          ctx.assertNull(result.iterator().next().getJsonObject(1));
          ctx.assertNull(result.iterator().next().getJsonObject("Buffer2"));
          ctx.assertNull(result.iterator().next().getJsonArray(1));
          ctx.assertNull(result.iterator().next().getJsonArray("Buffer2"));
          ctx.assertNull(result.iterator().next().getTemporal(1));
          ctx.assertNull(result.iterator().next().getTemporal("Buffer2"));
          ctx.assertNull(result.iterator().next().getLocalDate(1));
          ctx.assertNull(result.iterator().next().getLocalDate("Buffer2"));
          ctx.assertNull(result.iterator().next().getLocalTime(1));
          ctx.assertNull(result.iterator().next().getLocalTime("Buffer2"));
          ctx.assertNull(result.iterator().next().getOffsetTime(1));
          ctx.assertNull(result.iterator().next().getOffsetTime("Buffer2"));
          ctx.assertNull(result.iterator().next().getLocalDateTime(1));
          ctx.assertNull(result.iterator().next().getLocalDateTime("Buffer2"));
          ctx.assertNull(result.iterator().next().getOffsetDateTime(1));
          ctx.assertNull(result.iterator().next().getOffsetDateTime("Buffer2"));
          async.complete();
        }));
    }));
  }

}

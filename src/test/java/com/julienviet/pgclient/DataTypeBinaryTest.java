package com.julienviet.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class DataTypeBinaryTest extends DataTypeTestBase {

  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options).setCachePreparedStatements(false);
  }

  @Test
  public void testBoolean(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Boolean\" FROM \"NumericDataType\" WHERE \"Boolean\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.tuple().addBoolean(Boolean.TRUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Boolean.TRUE, result.iterator().next().getBoolean(0));
            ctx.assertEquals(Boolean.TRUE, result.iterator().next().getValue(0));
            ctx.assertEquals(Boolean.TRUE, result.iterator().next().getValue("Boolean"));
            ctx.assertEquals(Boolean.TRUE, result.iterator().next().getBoolean("Boolean"));
            ctx.assertNull(result.iterator().next().getLong(0));
            ctx.assertNull(result.iterator().next().getLong("Boolean"));
            ctx.assertNull(result.iterator().next().getInteger(0));
            ctx.assertNull(result.iterator().next().getInteger("Boolean"));
            ctx.assertNull(result.iterator().next().getFloat(0));
            ctx.assertNull(result.iterator().next().getFloat("Boolean"));
            ctx.assertNull(result.iterator().next().getDouble(0));
            ctx.assertNull(result.iterator().next().getDouble("Boolean"));
            ctx.assertNull(result.iterator().next().getCharacter(0));
            ctx.assertNull(result.iterator().next().getCharacter("Boolean"));
            ctx.assertNull(result.iterator().next().getString(0));
            ctx.assertNull(result.iterator().next().getString("Boolean"));
            ctx.assertNull(result.iterator().next().getJsonObject(0));
            ctx.assertNull(result.iterator().next().getJsonObject("Boolean"));
            ctx.assertNull(result.iterator().next().getJsonArray(0));
            ctx.assertNull(result.iterator().next().getJsonArray("Boolean"));
            ctx.assertNull(result.iterator().next().getBuffer(0));
            ctx.assertNull(result.iterator().next().getBuffer("Boolean"));
            ctx.assertNull(result.iterator().next().getTemporal(0));
            ctx.assertNull(result.iterator().next().getTemporal("Boolean"));
            ctx.assertNull(result.iterator().next().getLocalDate(0));
            ctx.assertNull(result.iterator().next().getLocalDate("Boolean"));
            ctx.assertNull(result.iterator().next().getLocalTime(0));
            ctx.assertNull(result.iterator().next().getLocalTime("Boolean"));
            ctx.assertNull(result.iterator().next().getOffsetTime(0));
            ctx.assertNull(result.iterator().next().getOffsetTime("Boolean"));
            ctx.assertNull(result.iterator().next().getLocalDateTime(0));
            ctx.assertNull(result.iterator().next().getLocalDateTime("Boolean"));
            ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
            ctx.assertNull(result.iterator().next().getOffsetDateTime("Boolean"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testInt2(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Short\" FROM \"NumericDataType\" WHERE \"Short\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.of(Short.MAX_VALUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Short.MAX_VALUE, result.iterator().next().getValue(0));
            ctx.assertEquals(Short.MAX_VALUE, result.iterator().next().getValue("Short"));
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
    }));
  }

  @Test
  public void testInt4(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Integer\" FROM \"NumericDataType\" WHERE \"Integer\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.tuple().addInteger(Integer.MAX_VALUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Integer.MAX_VALUE, result.iterator().next().getInteger(0));
            ctx.assertEquals(Integer.MAX_VALUE, result.iterator().next().getValue(0));
            ctx.assertEquals(Integer.MAX_VALUE, result.iterator().next().getValue("Integer"));
            ctx.assertEquals(Integer.MAX_VALUE, result.iterator().next().getInteger("Integer"));
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
    }));
  }

  @Test
  public void testInt8(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Long\" FROM \"NumericDataType\" WHERE \"Long\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.tuple().addLong(Long.MAX_VALUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Long.MAX_VALUE, result.iterator().next().getLong(0));
            ctx.assertEquals(Long.MAX_VALUE, result.iterator().next().getValue(0));
            ctx.assertEquals(Long.MAX_VALUE, result.iterator().next().getValue("Long"));
            ctx.assertEquals(Long.MAX_VALUE, result.iterator().next().getLong("Long"));
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
    }));
  }

  @Test
  public void testFloat4(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Float\" FROM \"NumericDataType\" WHERE \"Float\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.tuple().addFloat(Float.MAX_VALUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Float.MAX_VALUE, result.iterator().next().getFloat(0));
            ctx.assertEquals(Float.MAX_VALUE, result.iterator().next().getValue(0));
            ctx.assertEquals(Float.MAX_VALUE, result.iterator().next().getFloat("Float"));
            ctx.assertEquals(Float.MAX_VALUE, result.iterator().next().getValue("Float"));
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
    }));
  }

  @Test
  public void testFloat8(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Double\" FROM \"NumericDataType\" WHERE \"Double\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.tuple().addDouble(Double.MAX_VALUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Double.MAX_VALUE, result.iterator().next().getDouble(0));
            ctx.assertEquals(Double.MAX_VALUE, result.iterator().next().getValue(0));
            ctx.assertEquals(Double.MAX_VALUE, result.iterator().next().getDouble("Double"));
            ctx.assertEquals(Double.MAX_VALUE, result.iterator().next().getValue("Double"));
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
    }));
  }

  @Test
  public void testDateBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Date\" FROM \"TemporalDataType\" WHERE \"Date\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.tuple().addLocalDate(LocalDate.parse("1981-05-30")))
            .execute(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              LocalDate ld = LocalDate.parse("1981-05-30");
              ctx.assertEquals(ld, result.iterator().next().getLocalDate(0));
              ctx.assertEquals(ld, result.iterator().next().getValue(0));
              ctx.assertEquals(ld, result.iterator().next().getTemporal(0));
              ctx.assertEquals(ld, result.iterator().next().getLocalDate("Date"));
              ctx.assertEquals(ld, result.iterator().next().getValue("Date"));
              ctx.assertEquals(ld, result.iterator().next().getTemporal("Date"));
              ctx.assertNull(result.iterator().next().getBoolean(0));
              ctx.assertNull(result.iterator().next().getBoolean("Date"));
              ctx.assertNull(result.iterator().next().getLong(0));
              ctx.assertNull(result.iterator().next().getLong("Date"));
              ctx.assertNull(result.iterator().next().getInteger(0));
              ctx.assertNull(result.iterator().next().getInteger("Date"));
              ctx.assertNull(result.iterator().next().getFloat(0));
              ctx.assertNull(result.iterator().next().getFloat("Date"));
              ctx.assertNull(result.iterator().next().getDouble(0));
              ctx.assertNull(result.iterator().next().getDouble("Date"));
              ctx.assertNull(result.iterator().next().getCharacter(0));
              ctx.assertNull(result.iterator().next().getCharacter("Date"));
              ctx.assertNull(result.iterator().next().getString(0));
              ctx.assertNull(result.iterator().next().getString("Date"));
              ctx.assertNull(result.iterator().next().getJsonObject(0));
              ctx.assertNull(result.iterator().next().getJsonObject("Date"));
              ctx.assertNull(result.iterator().next().getJsonArray(0));
              ctx.assertNull(result.iterator().next().getJsonArray("Date"));
              ctx.assertNull(result.iterator().next().getBuffer(0));
              ctx.assertNull(result.iterator().next().getBuffer("Date"));
              ctx.assertNull(result.iterator().next().getLocalTime(0));
              ctx.assertNull(result.iterator().next().getLocalTime("Date"));
              ctx.assertNull(result.iterator().next().getOffsetTime(0));
              ctx.assertNull(result.iterator().next().getOffsetTime("Date"));
              ctx.assertNull(result.iterator().next().getLocalDateTime(0));
              ctx.assertNull(result.iterator().next().getLocalDateTime("Date"));
              ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
              ctx.assertNull(result.iterator().next().getOffsetDateTime("Date"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDateAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Date\" FROM \"TemporalDataType\" WHERE \"Date\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.tuple().addLocalDate(LocalDate.parse("2017-05-30")))
            .execute(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              LocalDate ld = LocalDate.parse("2017-05-30");
              ctx.assertEquals(ld, result.iterator().next().getLocalDate(0));
              ctx.assertEquals(ld, result.iterator().next().getValue(0));
              ctx.assertEquals(ld, result.iterator().next().getTemporal(0));
              ctx.assertEquals(ld, result.iterator().next().getLocalDate("Date"));
              ctx.assertEquals(ld, result.iterator().next().getValue("Date"));
              ctx.assertEquals(ld, result.iterator().next().getTemporal("Date"));
              ctx.assertNull(result.iterator().next().getBoolean(0));
              ctx.assertNull(result.iterator().next().getBoolean("Date"));
              ctx.assertNull(result.iterator().next().getLong(0));
              ctx.assertNull(result.iterator().next().getLong("Date"));
              ctx.assertNull(result.iterator().next().getInteger(0));
              ctx.assertNull(result.iterator().next().getInteger("Date"));
              ctx.assertNull(result.iterator().next().getFloat(0));
              ctx.assertNull(result.iterator().next().getFloat("Date"));
              ctx.assertNull(result.iterator().next().getDouble(0));
              ctx.assertNull(result.iterator().next().getDouble("Date"));
              ctx.assertNull(result.iterator().next().getCharacter(0));
              ctx.assertNull(result.iterator().next().getCharacter("Date"));
              ctx.assertNull(result.iterator().next().getString(0));
              ctx.assertNull(result.iterator().next().getString("Date"));
              ctx.assertNull(result.iterator().next().getJsonObject(0));
              ctx.assertNull(result.iterator().next().getJsonObject("Date"));
              ctx.assertNull(result.iterator().next().getJsonArray(0));
              ctx.assertNull(result.iterator().next().getJsonArray("Date"));
              ctx.assertNull(result.iterator().next().getBuffer(0));
              ctx.assertNull(result.iterator().next().getBuffer("Date"));
              ctx.assertNull(result.iterator().next().getLocalTime(0));
              ctx.assertNull(result.iterator().next().getLocalTime("Date"));
              ctx.assertNull(result.iterator().next().getOffsetTime(0));
              ctx.assertNull(result.iterator().next().getOffsetTime("Date"));
              ctx.assertNull(result.iterator().next().getLocalDateTime(0));
              ctx.assertNull(result.iterator().next().getLocalDateTime("Date"));
              ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
              ctx.assertNull(result.iterator().next().getOffsetDateTime("Date"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testTime(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Time\" FROM \"TemporalDataType\" WHERE \"Time\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.tuple().addLocalTime(LocalTime.parse("17:55:04.905120")))
            .execute(ctx.asyncAssertSuccess(result -> {
              LocalTime lt = LocalTime.parse("17:55:04.905120");
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(lt, result.iterator().next().getLocalTime(0));
              ctx.assertEquals(lt, result.iterator().next().getTemporal(0));
              ctx.assertEquals(lt, result.iterator().next().getValue(0));
              ctx.assertEquals(lt, result.iterator().next().getLocalTime("Time"));
              ctx.assertEquals(lt, result.iterator().next().getTemporal("Time"));
              ctx.assertEquals(lt, result.iterator().next().getValue("Time"));
              ctx.assertNull(result.iterator().next().getBoolean(0));
              ctx.assertNull(result.iterator().next().getBoolean("Time"));
              ctx.assertNull(result.iterator().next().getLong(0));
              ctx.assertNull(result.iterator().next().getLong("Time"));
              ctx.assertNull(result.iterator().next().getInteger(0));
              ctx.assertNull(result.iterator().next().getInteger("Time"));
              ctx.assertNull(result.iterator().next().getFloat(0));
              ctx.assertNull(result.iterator().next().getFloat("Time"));
              ctx.assertNull(result.iterator().next().getDouble(0));
              ctx.assertNull(result.iterator().next().getDouble("Time"));
              ctx.assertNull(result.iterator().next().getCharacter(0));
              ctx.assertNull(result.iterator().next().getCharacter("Time"));
              ctx.assertNull(result.iterator().next().getString(0));
              ctx.assertNull(result.iterator().next().getString("Time"));
              ctx.assertNull(result.iterator().next().getJsonObject(0));
              ctx.assertNull(result.iterator().next().getJsonObject("Time"));
              ctx.assertNull(result.iterator().next().getJsonArray(0));
              ctx.assertNull(result.iterator().next().getJsonArray("Time"));
              ctx.assertNull(result.iterator().next().getBuffer(0));
              ctx.assertNull(result.iterator().next().getBuffer("Time"));
              ctx.assertNull(result.iterator().next().getLocalDate(0));
              ctx.assertNull(result.iterator().next().getLocalDate("Time"));
              ctx.assertNull(result.iterator().next().getOffsetTime(0));
              ctx.assertNull(result.iterator().next().getOffsetTime("Time"));
              ctx.assertNull(result.iterator().next().getLocalDateTime(0));
              ctx.assertNull(result.iterator().next().getLocalDateTime("Time"));
              ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
              ctx.assertNull(result.iterator().next().getOffsetDateTime("Time"));
              async.complete();
            }));
        }));
    }));
  }


  @Test
  public void testTimeTz(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"TimeTz\" FROM \"TemporalDataType\" WHERE \"TimeTz\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.tuple().addOffsetTime(OffsetTime.parse("17:55:04.90512+03:07")))
            .execute(ctx.asyncAssertSuccess(result -> {
              OffsetTime ot = OffsetTime.parse("17:55:04.905120+03:07");
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(ot, result.iterator().next().getOffsetTime(0));
              ctx.assertEquals(ot, result.iterator().next().getTemporal(0));
              ctx.assertEquals(ot, result.iterator().next().getValue(0));
              ctx.assertEquals(ot, result.iterator().next().getOffsetTime("TimeTz"));
              ctx.assertEquals(ot, result.iterator().next().getTemporal("TimeTz"));
              ctx.assertEquals(ot, result.iterator().next().getValue("TimeTz"));
              ctx.assertNull(result.iterator().next().getBoolean(0));
              ctx.assertNull(result.iterator().next().getBoolean("TimeTz"));
              ctx.assertNull(result.iterator().next().getLong(0));
              ctx.assertNull(result.iterator().next().getLong("TimeTz"));
              ctx.assertNull(result.iterator().next().getInteger(0));
              ctx.assertNull(result.iterator().next().getInteger("TimeTz"));
              ctx.assertNull(result.iterator().next().getFloat(0));
              ctx.assertNull(result.iterator().next().getFloat("TimeTz"));
              ctx.assertNull(result.iterator().next().getDouble(0));
              ctx.assertNull(result.iterator().next().getDouble("TimeTz"));
              ctx.assertNull(result.iterator().next().getCharacter(0));
              ctx.assertNull(result.iterator().next().getCharacter("TimeTz"));
              ctx.assertNull(result.iterator().next().getString(0));
              ctx.assertNull(result.iterator().next().getString("TimeTz"));
              ctx.assertNull(result.iterator().next().getJsonObject(0));
              ctx.assertNull(result.iterator().next().getJsonObject("TimeTz"));
              ctx.assertNull(result.iterator().next().getJsonArray(0));
              ctx.assertNull(result.iterator().next().getJsonArray("TimeTz"));
              ctx.assertNull(result.iterator().next().getBuffer(0));
              ctx.assertNull(result.iterator().next().getBuffer("TimeTz"));
              ctx.assertNull(result.iterator().next().getLocalDate(0));
              ctx.assertNull(result.iterator().next().getLocalDate("TimeTz"));
              ctx.assertNull(result.iterator().next().getLocalTime(0));
              ctx.assertNull(result.iterator().next().getLocalTime("TimeTz"));
              ctx.assertNull(result.iterator().next().getLocalDateTime(0));
              ctx.assertNull(result.iterator().next().getLocalDateTime("TimeTz"));
              ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
              ctx.assertNull(result.iterator().next().getOffsetDateTime("TimeTz"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testTimestampBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"Timestamp\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.tuple().addLocalDateTime(LocalDateTime.parse("1800-01-01T23:57:53.237666")))
            .execute(ctx.asyncAssertSuccess(result -> {
              LocalDateTime ldt = LocalDateTime.parse("1800-01-01T23:57:53.237666");
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(ldt, result.iterator().next().getLocalDateTime(0));
              ctx.assertEquals(ldt, result.iterator().next().getTemporal(0));
              ctx.assertEquals(ldt, result.iterator().next().getValue(0));
              ctx.assertEquals(ldt, result.iterator().next().getLocalDateTime("Timestamp"));
              ctx.assertEquals(ldt, result.iterator().next().getTemporal("Timestamp"));
              ctx.assertEquals(ldt, result.iterator().next().getValue("Timestamp"));
              ctx.assertNull(result.iterator().next().getBoolean(0));
              ctx.assertNull(result.iterator().next().getBoolean("Timestamp"));
              ctx.assertNull(result.iterator().next().getLong(0));
              ctx.assertNull(result.iterator().next().getLong("Timestamp"));
              ctx.assertNull(result.iterator().next().getInteger(0));
              ctx.assertNull(result.iterator().next().getInteger("Timestamp"));
              ctx.assertNull(result.iterator().next().getFloat(0));
              ctx.assertNull(result.iterator().next().getFloat("Timestamp"));
              ctx.assertNull(result.iterator().next().getDouble(0));
              ctx.assertNull(result.iterator().next().getDouble("Timestamp"));
              ctx.assertNull(result.iterator().next().getCharacter(0));
              ctx.assertNull(result.iterator().next().getCharacter("Timestamp"));
              ctx.assertNull(result.iterator().next().getString(0));
              ctx.assertNull(result.iterator().next().getString("Timestamp"));
              ctx.assertNull(result.iterator().next().getJsonObject(0));
              ctx.assertNull(result.iterator().next().getJsonObject("Timestamp"));
              ctx.assertNull(result.iterator().next().getJsonArray(0));
              ctx.assertNull(result.iterator().next().getJsonArray("Timestamp"));
              ctx.assertNull(result.iterator().next().getBuffer(0));
              ctx.assertNull(result.iterator().next().getBuffer("Timestamp"));
              ctx.assertNull(result.iterator().next().getLocalDate(0));
              ctx.assertNull(result.iterator().next().getLocalDate("Timestamp"));
              ctx.assertNull(result.iterator().next().getLocalTime(0));
              ctx.assertNull(result.iterator().next().getLocalTime("Timestamp"));
              ctx.assertNull(result.iterator().next().getOffsetTime(0));
              ctx.assertNull(result.iterator().next().getOffsetTime("Timestamp"));
              ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
              ctx.assertNull(result.iterator().next().getOffsetDateTime("Timestamp"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testTimestampAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"Timestamp\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.tuple().addLocalDateTime(LocalDateTime.parse("2017-05-14T19:35:58.237666")))
            .execute(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
              ctx.assertEquals(ldt, result.iterator().next().getLocalDateTime(0));
              ctx.assertEquals(ldt, result.iterator().next().getTemporal(0));
              ctx.assertEquals(ldt, result.iterator().next().getValue(0));
              ctx.assertEquals(ldt, result.iterator().next().getLocalDateTime("Timestamp"));
              ctx.assertEquals(ldt, result.iterator().next().getTemporal("Timestamp"));
              ctx.assertEquals(ldt, result.iterator().next().getValue("Timestamp"));
              ctx.assertNull(result.iterator().next().getBoolean(0));
              ctx.assertNull(result.iterator().next().getBoolean("Timestamp"));
              ctx.assertNull(result.iterator().next().getLong(0));
              ctx.assertNull(result.iterator().next().getLong("Timestamp"));
              ctx.assertNull(result.iterator().next().getInteger(0));
              ctx.assertNull(result.iterator().next().getInteger("Timestamp"));
              ctx.assertNull(result.iterator().next().getFloat(0));
              ctx.assertNull(result.iterator().next().getFloat("Timestamp"));
              ctx.assertNull(result.iterator().next().getDouble(0));
              ctx.assertNull(result.iterator().next().getDouble("Timestamp"));
              ctx.assertNull(result.iterator().next().getCharacter(0));
              ctx.assertNull(result.iterator().next().getCharacter("Timestamp"));
              ctx.assertNull(result.iterator().next().getString(0));
              ctx.assertNull(result.iterator().next().getString("Timestamp"));
              ctx.assertNull(result.iterator().next().getJsonObject(0));
              ctx.assertNull(result.iterator().next().getJsonObject("Timestamp"));
              ctx.assertNull(result.iterator().next().getJsonArray(0));
              ctx.assertNull(result.iterator().next().getJsonArray("Timestamp"));
              ctx.assertNull(result.iterator().next().getBuffer(0));
              ctx.assertNull(result.iterator().next().getBuffer("Timestamp"));
              ctx.assertNull(result.iterator().next().getLocalDate(0));
              ctx.assertNull(result.iterator().next().getLocalDate("Timestamp"));
              ctx.assertNull(result.iterator().next().getLocalTime(0));
              ctx.assertNull(result.iterator().next().getLocalTime("Timestamp"));
              ctx.assertNull(result.iterator().next().getOffsetTime(0));
              ctx.assertNull(result.iterator().next().getOffsetTime("Timestamp"));
              ctx.assertNull(result.iterator().next().getOffsetDateTime(0));
              ctx.assertNull(result.iterator().next().getOffsetDateTime("Timestamp"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testTimestampTzBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SET TIME ZONE 'UTC'").execute(ctx.asyncAssertSuccess(v -> {
        conn.prepare("SELECT \"TimestampTz\" FROM \"TemporalDataType\" WHERE \"TimestampTz\" = $1",
          ctx.asyncAssertSuccess(p -> {
            p.createQuery(Tuple.tuple().addOffsetDateTime(OffsetDateTime.parse("1800-01-01T23:59:59.237666-03:00")))
              .execute(ctx.asyncAssertSuccess(result -> {
                OffsetDateTime odt = OffsetDateTime.parse("1800-01-02T02:59:59.237666Z");
                ctx.assertEquals(1, result.size());
                ctx.assertEquals(odt, result.iterator().next().getOffsetDateTime(0));
                ctx.assertEquals(odt, result.iterator().next().getTemporal(0));
                ctx.assertEquals(odt, result.iterator().next().getValue(0));
                ctx.assertEquals(odt, result.iterator().next().getOffsetDateTime("TimestampTz"));
                ctx.assertEquals(odt, result.iterator().next().getTemporal("TimestampTz"));
                ctx.assertEquals(odt, result.iterator().next().getValue("TimestampTz"));
                ctx.assertNull(result.iterator().next().getBoolean(0));
                ctx.assertNull(result.iterator().next().getBoolean("TimestampTz"));
                ctx.assertNull(result.iterator().next().getLong(0));
                ctx.assertNull(result.iterator().next().getLong("TimestampTz"));
                ctx.assertNull(result.iterator().next().getInteger(0));
                ctx.assertNull(result.iterator().next().getInteger("TimestampTz"));
                ctx.assertNull(result.iterator().next().getFloat(0));
                ctx.assertNull(result.iterator().next().getFloat("TimestampTz"));
                ctx.assertNull(result.iterator().next().getDouble(0));
                ctx.assertNull(result.iterator().next().getDouble("TimestampTz"));
                ctx.assertNull(result.iterator().next().getCharacter(0));
                ctx.assertNull(result.iterator().next().getCharacter("TimestampTz"));
                ctx.assertNull(result.iterator().next().getString(0));
                ctx.assertNull(result.iterator().next().getString("TimestampTz"));
                ctx.assertNull(result.iterator().next().getJsonObject(0));
                ctx.assertNull(result.iterator().next().getJsonObject("TimestampTz"));
                ctx.assertNull(result.iterator().next().getJsonArray(0));
                ctx.assertNull(result.iterator().next().getJsonArray("TimestampTz"));
                ctx.assertNull(result.iterator().next().getBuffer(0));
                ctx.assertNull(result.iterator().next().getBuffer("TimestampTz"));
                ctx.assertNull(result.iterator().next().getLocalDate(0));
                ctx.assertNull(result.iterator().next().getLocalDate("TimestampTz"));
                ctx.assertNull(result.iterator().next().getLocalTime(0));
                ctx.assertNull(result.iterator().next().getLocalTime("TimestampTz"));
                ctx.assertNull(result.iterator().next().getOffsetTime(0));
                ctx.assertNull(result.iterator().next().getOffsetTime("TimestampTz"));
                ctx.assertNull(result.iterator().next().getLocalDateTime(0));
                ctx.assertNull(result.iterator().next().getLocalDateTime("TimestampTz"));
                async.complete();
              }));
          }));
      }));
    }));
  }

  @Test
  public void testTimestampTzAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SET TIME ZONE 'UTC'").execute(ctx.asyncAssertSuccess(v -> {
        conn.prepare("SELECT \"TimestampTz\" FROM \"TemporalDataType\" WHERE \"TimestampTz\" = $1",
          ctx.asyncAssertSuccess(p -> {
            p.createQuery(Tuple.tuple().addOffsetDateTime(OffsetDateTime.parse("2017-05-14T23:59:59.237666-03:00")))
              .execute(ctx.asyncAssertSuccess(result -> {
                ctx.assertEquals(1, result.size());
                OffsetDateTime odt = OffsetDateTime.parse("2017-05-15T02:59:59.237666Z");
                ctx.assertEquals(odt, result.iterator().next().getOffsetDateTime(0));
                ctx.assertEquals(odt, result.iterator().next().getTemporal(0));
                ctx.assertEquals(odt, result.iterator().next().getValue(0));
                ctx.assertEquals(odt, result.iterator().next().getOffsetDateTime("TimestampTz"));
                ctx.assertEquals(odt, result.iterator().next().getTemporal("TimestampTz"));
                ctx.assertEquals(odt, result.iterator().next().getValue("TimestampTz"));
                ctx.assertNull(result.iterator().next().getBoolean(0));
                ctx.assertNull(result.iterator().next().getLong(0));
                ctx.assertNull(result.iterator().next().getInteger(0));
                ctx.assertNull(result.iterator().next().getFloat(0));
                ctx.assertNull(result.iterator().next().getDouble(0));
                ctx.assertNull(result.iterator().next().getCharacter(0));
                ctx.assertNull(result.iterator().next().getString(0));
                ctx.assertNull(result.iterator().next().getJsonObject(0));
                ctx.assertNull(result.iterator().next().getJsonArray(0));
                ctx.assertNull(result.iterator().next().getBuffer(0));
                ctx.assertNull(result.iterator().next().getLocalDate(0));
                ctx.assertNull(result.iterator().next().getLocalTime(0));
                ctx.assertNull(result.iterator().next().getOffsetTime(0));
                ctx.assertNull(result.iterator().next().getLocalDateTime(0));
                async.complete();
              }));
          }));
      }));
    }));
  }
}

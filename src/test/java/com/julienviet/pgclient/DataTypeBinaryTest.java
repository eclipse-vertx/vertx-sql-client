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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Boolean\" FROM \"NumericDataType\" WHERE \"Boolean\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addBoolean(Boolean.TRUE), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(Boolean.TRUE, row.getBoolean(0));
            ctx.assertEquals(Boolean.TRUE, row.getValue(0));
            ctx.assertEquals(Boolean.TRUE, row.getValue("Boolean"));
            ctx.assertEquals(Boolean.TRUE, row.getBoolean("Boolean"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Boolean"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Boolean"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Boolean"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Boolean"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("Boolean"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("Boolean"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("Boolean"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("Boolean"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("Boolean"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("Boolean"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("Boolean"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("Boolean"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("Boolean"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("Boolean"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("Boolean"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testInt2(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Short\" FROM \"NumericDataType\" WHERE \"Short\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.of(Short.MAX_VALUE), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(Short.MAX_VALUE, row.getValue(0));
            ctx.assertEquals(Short.MAX_VALUE, row.getValue("Short"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Short"));
            ctx.assertEquals(32767L, row.getLong(0));
            ctx.assertEquals(32767L, row.getLong("Short"));
            ctx.assertEquals(32767, row.getInteger(0));
            ctx.assertEquals(32767, row.getInteger("Short"));
            ctx.assertEquals(32767f, row.getFloat(0));
            ctx.assertEquals(32767f, row.getFloat("Short"));
            ctx.assertEquals(32767d, row.getDouble(0));
            ctx.assertEquals(32767d, row.getDouble("Short"));
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
    }));
  }

  @Test
  public void testInt4(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Integer\" FROM \"NumericDataType\" WHERE \"Integer\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(Integer.MAX_VALUE), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(Integer.MAX_VALUE, row.getInteger(0));
            ctx.assertEquals(Integer.MAX_VALUE, row.getValue(0));
            ctx.assertEquals(Integer.MAX_VALUE, row.getValue("Integer"));
            ctx.assertEquals(Integer.MAX_VALUE, row.getInteger("Integer"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Integer"));
            ctx.assertEquals(2147483647L, row.getLong(0));
            ctx.assertEquals(2147483647L, row.getLong("Integer"));
            ctx.assertEquals(2147483647f, row.getFloat(0));
            ctx.assertEquals(2147483647f, row.getFloat("Integer"));
            ctx.assertEquals(2147483647d, row.getDouble(0));
            ctx.assertEquals(2147483647d, row.getDouble("Integer"));
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
    }));
  }

  @Test
  public void testInt8(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Long\" FROM \"NumericDataType\" WHERE \"Long\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addLong(Long.MAX_VALUE), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(Long.MAX_VALUE, row.getLong(0));
            ctx.assertEquals(Long.MAX_VALUE, row.getValue(0));
            ctx.assertEquals(Long.MAX_VALUE, row.getValue("Long"));
            ctx.assertEquals(Long.MAX_VALUE, row.getLong("Long"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Long"));
            ctx.assertEquals(-1, row.getInteger(0));
            ctx.assertEquals(-1, row.getInteger("Long"));
            ctx.assertEquals(9.223372E18f, row.getFloat(0));
            ctx.assertEquals(9.223372E18f, row.getFloat("Long"));
            ctx.assertEquals(9.223372036854776E18d, row.getDouble(0));
            ctx.assertEquals(9.223372036854776E18d, row.getDouble("Long"));
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
    }));
  }

  @Test
  public void testFloat4(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Float\" FROM \"NumericDataType\" WHERE \"Float\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addFloat(Float.MAX_VALUE), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(Float.MAX_VALUE, row.getFloat(0));
            ctx.assertEquals(Float.MAX_VALUE, row.getValue(0));
            ctx.assertEquals(Float.MAX_VALUE, row.getFloat("Float"));
            ctx.assertEquals(Float.MAX_VALUE, row.getValue("Float"));
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
    }));
  }

  @Test
  public void testFloat8(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Double\" FROM \"NumericDataType\" WHERE \"Double\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addDouble(Double.MAX_VALUE), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(Double.MAX_VALUE, row.getDouble(0));
            ctx.assertEquals(Double.MAX_VALUE, row.getValue(0));
            ctx.assertEquals(Double.MAX_VALUE, row.getDouble("Double"));
            ctx.assertEquals(Double.MAX_VALUE, row.getValue("Double"));
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
    }));
  }

  @Test
  public void testDateBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Date\" FROM \"TemporalDataType\" WHERE \"Date\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addLocalDate(LocalDate.parse("1981-05-30")), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            LocalDate ld = LocalDate.parse("1981-05-30");
            Row row = result.iterator().next();
            ctx.assertEquals(ld, row.getLocalDate(0));
            ctx.assertEquals(ld, row.getValue(0));
            ctx.assertEquals(ld, row.getTemporal(0));
            ctx.assertEquals(ld, row.getLocalDate("Date"));
            ctx.assertEquals(ld, row.getValue("Date"));
            ctx.assertEquals(ld, row.getTemporal("Date"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Date"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Date"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Date"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Date"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Date"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("Date"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("Date"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("Date"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("Date"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("Date"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("Date"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("Date"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("Date"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("Date"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDateAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Date\" FROM \"TemporalDataType\" WHERE \"Date\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addLocalDate(LocalDate.parse("2017-05-30")), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            LocalDate ld = LocalDate.parse("2017-05-30");
            Row row = result.iterator().next();
            ctx.assertEquals(ld, row.getLocalDate(0));
            ctx.assertEquals(ld, row.getValue(0));
            ctx.assertEquals(ld, row.getTemporal(0));
            ctx.assertEquals(ld, row.getLocalDate("Date"));
            ctx.assertEquals(ld, row.getValue("Date"));
            ctx.assertEquals(ld, row.getTemporal("Date"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Date"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Date"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Date"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Date"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Date"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("Date"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("Date"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("Date"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("Date"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("Date"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("Date"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("Date"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("Date"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("Date"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testTime(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Time\" FROM \"TemporalDataType\" WHERE \"Time\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addLocalTime(LocalTime.parse("17:55:04.905120")), ctx.asyncAssertSuccess(result -> {
            LocalTime lt = LocalTime.parse("17:55:04.905120");
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(lt, row.getLocalTime(0));
            ctx.assertEquals(lt, row.getTemporal(0));
            ctx.assertEquals(lt, row.getValue(0));
            ctx.assertEquals(lt, row.getLocalTime("Time"));
            ctx.assertEquals(lt, row.getTemporal("Time"));
            ctx.assertEquals(lt, row.getValue("Time"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Time"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Time"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Time"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Time"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Time"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("Time"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("Time"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("Time"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("Time"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("Time"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("Time"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("Time"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("Time"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("Time"));
            async.complete();
          }));
        }));
    }));
  }


  @Test
  public void testTimeTz(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"TimeTz\" FROM \"TemporalDataType\" WHERE \"TimeTz\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addOffsetTime(OffsetTime.parse("17:55:04.90512+03:07")), ctx.asyncAssertSuccess(result -> {
            OffsetTime ot = OffsetTime.parse("17:55:04.905120+03:07");
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(ot, row.getOffsetTime(0));
            ctx.assertEquals(ot, row.getTemporal(0));
            ctx.assertEquals(ot, row.getValue(0));
            ctx.assertEquals(ot, row.getOffsetTime("TimeTz"));
            ctx.assertEquals(ot, row.getTemporal("TimeTz"));
            ctx.assertEquals(ot, row.getValue("TimeTz"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("TimeTz"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("TimeTz"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("TimeTz"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("TimeTz"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("TimeTz"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("TimeTz"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("TimeTz"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("TimeTz"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("TimeTz"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("TimeTz"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("TimeTz"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("TimeTz"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("TimeTz"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("TimeTz"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testTimestampBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"Timestamp\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addLocalDateTime(LocalDateTime.parse("1800-01-01T23:57:53.237666")), ctx.asyncAssertSuccess(result -> {
            LocalDateTime ldt = LocalDateTime.parse("1800-01-01T23:57:53.237666");
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(ldt, row.getLocalDateTime(0));
            ctx.assertEquals(ldt, row.getTemporal(0));
            ctx.assertEquals(ldt, row.getValue(0));
            ctx.assertEquals(ldt, row.getLocalDateTime("Timestamp"));
            ctx.assertEquals(ldt, row.getTemporal("Timestamp"));
            ctx.assertEquals(ldt, row.getValue("Timestamp"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Timestamp"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Timestamp"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Timestamp"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Timestamp"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Timestamp"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("Timestamp"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("Timestamp"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("Timestamp"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("Timestamp"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("Timestamp"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("Timestamp"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("Timestamp"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("Timestamp"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("Timestamp"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testTimestampAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"Timestamp\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addLocalDateTime(LocalDateTime.parse("2017-05-14T19:35:58.237666")), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
            Row row = result.iterator().next();
            ctx.assertEquals(ldt, row.getLocalDateTime(0));
            ctx.assertEquals(ldt, row.getTemporal(0));
            ctx.assertEquals(ldt, row.getValue(0));
            ctx.assertEquals(ldt, row.getLocalDateTime("Timestamp"));
            ctx.assertEquals(ldt, row.getTemporal("Timestamp"));
            ctx.assertEquals(ldt, row.getValue("Timestamp"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Timestamp"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Timestamp"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Timestamp"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Timestamp"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Timestamp"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("Timestamp"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("Timestamp"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("Timestamp"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("Timestamp"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("Timestamp"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("Timestamp"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("Timestamp"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("Timestamp"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("Timestamp"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testTimestampTzBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'", ctx.asyncAssertSuccess(v -> {
        conn.prepare("SELECT \"TimestampTz\" FROM \"TemporalDataType\" WHERE \"TimestampTz\" = $1",
          ctx.asyncAssertSuccess(p -> {
            p.execute(Tuple.tuple().addOffsetDateTime(OffsetDateTime.parse("1800-01-01T23:59:59.237666-03:00")), ctx.asyncAssertSuccess(result -> {
              OffsetDateTime odt = OffsetDateTime.parse("1800-01-02T02:59:59.237666Z");
              ctx.assertEquals(1, result.size());
              Row row = result.iterator().next();
              ctx.assertEquals(odt, row.getOffsetDateTime(0));
              ctx.assertEquals(odt, row.getTemporal(0));
              ctx.assertEquals(odt, row.getValue(0));
              ctx.assertEquals(odt, row.getOffsetDateTime("TimestampTz"));
              ctx.assertEquals(odt, row.getTemporal("TimestampTz"));
              ctx.assertEquals(odt, row.getValue("TimestampTz"));
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("TimestampTz"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("TimestampTz"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("TimestampTz"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("TimestampTz"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("TimestampTz"));
              ctx.assertNull(row.getCharacter(0));
              ctx.assertNull(row.getCharacter("TimestampTz"));
              ctx.assertNull(row.getString(0));
              ctx.assertNull(row.getString("TimestampTz"));
              ctx.assertNull(row.getJsonObject(0));
              ctx.assertNull(row.getJsonObject("TimestampTz"));
              ctx.assertNull(row.getJsonArray(0));
              ctx.assertNull(row.getJsonArray("TimestampTz"));
              ctx.assertNull(row.getBuffer(0));
              ctx.assertNull(row.getBuffer("TimestampTz"));
              ctx.assertNull(row.getLocalDate(0));
              ctx.assertNull(row.getLocalDate("TimestampTz"));
              ctx.assertNull(row.getLocalTime(0));
              ctx.assertNull(row.getLocalTime("TimestampTz"));
              ctx.assertNull(row.getOffsetTime(0));
              ctx.assertNull(row.getOffsetTime("TimestampTz"));
              ctx.assertNull(row.getLocalDateTime(0));
              ctx.assertNull(row.getLocalDateTime("TimestampTz"));
              async.complete();
            }));
          }));
      }));
    }));
  }

  @Test
  public void testTimestampTzAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'", ctx.asyncAssertSuccess(v -> {
        conn.prepare("SELECT \"TimestampTz\" FROM \"TemporalDataType\" WHERE \"TimestampTz\" = $1",
          ctx.asyncAssertSuccess(p -> {
            p.execute(Tuple.tuple().addOffsetDateTime(OffsetDateTime.parse("2017-05-14T23:59:59.237666-03:00")), ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              OffsetDateTime odt = OffsetDateTime.parse("2017-05-15T02:59:59.237666Z");
              Row row = result.iterator().next();
              ctx.assertEquals(odt, row.getOffsetDateTime(0));
              ctx.assertEquals(odt, row.getTemporal(0));
              ctx.assertEquals(odt, row.getValue(0));
              ctx.assertEquals(odt, row.getOffsetDateTime("TimestampTz"));
              ctx.assertEquals(odt, row.getTemporal("TimestampTz"));
              ctx.assertEquals(odt, row.getValue("TimestampTz"));
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getCharacter(0));
              ctx.assertNull(row.getString(0));
              ctx.assertNull(row.getJsonObject(0));
              ctx.assertNull(row.getJsonArray(0));
              ctx.assertNull(row.getBuffer(0));
              ctx.assertNull(row.getLocalDate(0));
              ctx.assertNull(row.getLocalTime(0));
              ctx.assertNull(row.getOffsetTime(0));
              ctx.assertNull(row.getLocalDateTime(0));
              async.complete();
            }));
          }));
      }));
    }));
  }
}

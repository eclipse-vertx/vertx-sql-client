package com.julienviet.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.UUID;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class DataTypeBinaryTest extends DataTypeTestBase {

  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options).setCachePreparedStatements(false);
  }

  @Test
  public void testDecodeBoolean(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Boolean\" FROM \"NumericDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Boolean"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeBoolean(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Boolean\" = $1  WHERE \"id\" = $2 RETURNING \"Boolean\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addBoolean(Boolean.FALSE)
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertEquals(Boolean.FALSE, row.getBoolean(0));
              ctx.assertEquals(Boolean.FALSE, row.getValue(0));
              ctx.assertEquals(Boolean.FALSE, row.getValue("Boolean"));
              ctx.assertEquals(Boolean.FALSE, row.getBoolean("Boolean"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("Boolean"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("Boolean"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("Boolean"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("Boolean"));
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
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("Boolean"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeInt2(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Short\" FROM \"NumericDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.of(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Short"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeInt2(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Short\" = $1 WHERE \"id\" = $2 RETURNING \"Short\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.of(Short.MIN_VALUE, 2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertEquals(Short.MIN_VALUE, row.getValue(0));
            ctx.assertEquals(Short.MIN_VALUE, row.getValue("Short"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Short"));
            ctx.assertEquals(-32768L, row.getLong(0));
            ctx.assertEquals(-32768L, row.getLong("Short"));
            ctx.assertEquals(-32768, row.getInteger(0));
            ctx.assertEquals(-32768, row.getInteger("Short"));
            ctx.assertEquals(-32768f, row.getFloat(0));
            ctx.assertEquals(-32768f, row.getFloat("Short"));
            ctx.assertEquals(-32768d, row.getDouble(0));
            ctx.assertEquals(-32768d, row.getDouble("Short"));
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Short"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeInt4(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Integer\" FROM \"NumericDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Integer"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeInt4(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Integer\" = $1 WHERE \"id\" = $2 RETURNING \"Integer\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addInteger(Integer.MIN_VALUE)
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertEquals(Integer.MIN_VALUE, row.getInteger(0));
              ctx.assertEquals(Integer.MIN_VALUE, row.getValue(0));
              ctx.assertEquals(Integer.MIN_VALUE, row.getValue("Integer"));
              ctx.assertEquals(Integer.MIN_VALUE, row.getInteger("Integer"));
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("Integer"));
              ctx.assertEquals(-2147483648L, row.getLong(0));
              ctx.assertEquals(-2147483648L, row.getLong("Integer"));
              ctx.assertEquals(-2147483648f, row.getFloat(0));
              ctx.assertEquals(-2147483648f, row.getFloat("Integer"));
              ctx.assertEquals(-2147483648d, row.getDouble(0));
              ctx.assertEquals(-2147483648d, row.getDouble("Integer"));
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
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("Integer"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeInt8(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Long\" FROM \"NumericDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Long"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeInt8(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Long\" = $1 WHERE \"id\" = $2 RETURNING \"Long\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addLong(Long.MIN_VALUE)
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertEquals(Long.MIN_VALUE, row.getLong(0));
              ctx.assertEquals(Long.MIN_VALUE, row.getValue(0));
              ctx.assertEquals(Long.MIN_VALUE, row.getValue("Long"));
              ctx.assertEquals(Long.MIN_VALUE, row.getLong("Long"));
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("Long"));
              ctx.assertEquals(0, row.getInteger(0));
              ctx.assertEquals(0, row.getInteger("Long"));
              ctx.assertEquals(-9.223372E18f, row.getFloat(0));
              ctx.assertEquals(-9.223372E18f, row.getFloat("Long"));
              ctx.assertEquals(-9.223372036854776E18d, row.getDouble(0));
              ctx.assertEquals(-9.223372036854776E18d, row.getDouble("Long"));
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
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("Long"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeFloat4(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Float\" FROM \"NumericDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Float"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeFloat4(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Float\" = $1 WHERE \"id\" = $2 RETURNING \"Float\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addFloat(Float.MIN_VALUE)
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertEquals(Float.MIN_VALUE, row.getFloat(0));
              ctx.assertEquals(Float.MIN_VALUE, row.getValue(0));
              ctx.assertEquals(Float.MIN_VALUE, row.getFloat("Float"));
              ctx.assertEquals(Float.MIN_VALUE, row.getValue("Float"));
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("Float"));
              ctx.assertEquals(0L, row.getLong(0));
              ctx.assertEquals(0L, row.getLong("Float"));
              ctx.assertEquals(0, row.getInteger(0));
              ctx.assertEquals(0, row.getInteger("Float"));
              ctx.assertEquals(1.401298464324817E-45d, row.getDouble(0));
              ctx.assertEquals(1.401298464324817E-45d, row.getDouble("Float"));
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
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("Float"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeFloat8(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Double\" FROM \"NumericDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Double"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeFloat8(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Double\" = $1 WHERE \"id\" = $2 RETURNING \"Double\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addDouble(Double.MIN_VALUE)
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertEquals(Double.MIN_VALUE, row.getDouble(0));
              ctx.assertEquals(Double.MIN_VALUE, row.getValue(0));
              ctx.assertEquals(Double.MIN_VALUE, row.getDouble("Double"));
              ctx.assertEquals(Double.MIN_VALUE, row.getValue("Double"));
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("Double"));
              ctx.assertEquals(0L, row.getLong(0));
              ctx.assertEquals(0L, row.getLong("Double"));
              ctx.assertEquals(0, row.getInteger(0));
              ctx.assertEquals(0, row.getInteger("Double"));
              ctx.assertEquals(0.0f, row.getFloat(0));
              ctx.assertEquals(0.0f, row.getFloat("Double"));
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
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("Double"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeDateBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Date\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Date"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeDateBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"Date\" = $1 WHERE \"id\" = $2 RETURNING \"Date\"",
        ctx.asyncAssertSuccess(p -> {
          LocalDate ld = LocalDate.parse("1981-06-30");
          p.execute(Tuple.tuple()
            .addLocalDate(ld)
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Date"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeDateAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Date\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Date"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeDateAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"Date\" = $1 WHERE \"id\" = $2 RETURNING \"Date\"",
        ctx.asyncAssertSuccess(p -> {
          LocalDate ld = LocalDate.parse("2018-05-30");
          p.execute(Tuple.tuple()
              .addLocalDate(ld)
              .addInteger(4)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
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
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("Date"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeTime(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Time\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            LocalTime lt = LocalTime.parse("17:55:04.905120");
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Time"));
            async.complete();
          }));
        }));
    }));
  }


  @Test
  public void testEncodeTime(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE  \"TemporalDataType\" SET \"Time\" = $1 WHERE \"id\" = $2 RETURNING \"Time\"",
        ctx.asyncAssertSuccess(p -> {
          LocalTime lt = LocalTime.parse("22:55:04.905120");
          p.execute(Tuple.tuple()
              .addLocalTime(lt)
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
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
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("Time"));
              async.complete();
            }));
        }));
    }));
  }


  @Test
  public void testDecodeTimeTz(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"TimeTz\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            OffsetTime ot = OffsetTime.parse("17:55:04.905120+03:07");
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("TimeTz"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeTimeTz(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"TimeTz\" = $1 WHERE \"id\" = $2 RETURNING \"TimeTz\"",
        ctx.asyncAssertSuccess(p -> {
          OffsetTime ot = OffsetTime.parse("20:55:04.905120+03:07");
          p.execute(Tuple.tuple()
            .addOffsetTime(ot)
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("TimeTz"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeTimestampBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(3), ctx.asyncAssertSuccess(result -> {
            LocalDateTime ldt = LocalDateTime.parse("1800-01-01T23:57:53.237666");
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Timestamp"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeTimestampBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"Timestamp\" = $1 WHERE \"id\" = $2 RETURNING \"Timestamp\"",
        ctx.asyncAssertSuccess(p -> {
          LocalDateTime ldt = LocalDateTime.parse("1900-02-01T23:57:53.237666");
          p.execute(Tuple.tuple()
            .addLocalDateTime(ldt)
            .addInteger(4), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Timestamp"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeTimestampAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Timestamp"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeTimestampAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"Timestamp\" =$1 WHERE \"id\" = $2 RETURNING \"Timestamp\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addLocalDateTime(LocalDateTime.parse("2017-05-14T19:35:58.237666"))
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
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
  public void testDecodeTimestampTzBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'", ctx.asyncAssertSuccess(v -> {
        conn.prepare("SELECT \"TimestampTz\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
          ctx.asyncAssertSuccess(p -> {
            p.execute(Tuple.tuple().addInteger(3), ctx.asyncAssertSuccess(result -> {
              OffsetDateTime odt = OffsetDateTime.parse("1800-01-02T02:59:59.237666Z");
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
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
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("TimestampTz"));
              async.complete();
            }));
          }));
      }));
    }));
  }

  @Test
  public void testEncodeTimestampTzBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'", ctx.asyncAssertSuccess(v -> {
        conn.prepare("UPDATE \"TemporalDataType\" SET \"TimestampTz\" =$1 WHERE \"id\" = $2 RETURNING \"TimestampTz\"",
          ctx.asyncAssertSuccess(p -> {
            p.execute(Tuple.tuple()
                .addOffsetDateTime(OffsetDateTime.parse("1800-02-01T23:59:59.237666-03:00"))
                .addInteger(3)
              , ctx.asyncAssertSuccess(result -> {
                OffsetDateTime odt = OffsetDateTime.parse("1800-02-02T02:59:59.237666Z");
                ctx.assertEquals(1, result.updatedCount());
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
                ctx.assertNull(row.getUUID(0));
                ctx.assertNull(row.getUUID("TimestampTz"));
                async.complete();
              }));
          }));
      }));
    }));
  }

  @Test
  public void testDecodeTimestampTzAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'", ctx.asyncAssertSuccess(v -> {
        conn.prepare("SELECT \"TimestampTz\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
          ctx.asyncAssertSuccess(p -> {
            p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              OffsetDateTime odt = OffsetDateTime.parse("2017-05-15T02:59:59.237666Z");
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
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("TimestampTz"));
              async.complete();
            }));
          }));
      }));
    }));
  }

  @Test
  public void testEncodeTimestampTzAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'", ctx.asyncAssertSuccess(v -> {
        conn.prepare("UPDATE \"TemporalDataType\" SET \"TimestampTz\" = $1 WHERE \"id\" = $2 RETURNING \"TimestampTz\"",
          ctx.asyncAssertSuccess(p -> {
            p.execute(Tuple.tuple()
                .addOffsetDateTime(OffsetDateTime.parse("2017-06-14T23:59:59.237666-03:00"))
                .addInteger(1)
              , ctx.asyncAssertSuccess(result -> {
                ctx.assertEquals(1, result.size());
                ctx.assertEquals(1, result.updatedCount());
                Row row = result.iterator().next();
                OffsetDateTime odt = OffsetDateTime.parse("2017-06-15T02:59:59.237666Z");
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
                ctx.assertNull(row.getUUID(0));
                ctx.assertNull(row.getUUID("TimestampTz"));
                async.complete();
              }));
          }));
      }));
    }));
  }

  @Test
  public void testDecodeUUID(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"uuid\" FROM \"CharacterDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            UUID uuid = UUID.fromString("6f790482-b5bd-438b-a8b7-4a0bed747011");
            ctx.assertEquals(uuid, row.getUUID(0));
            ctx.assertEquals(uuid, row.getValue(0));
            ctx.assertEquals(uuid, row.getUUID("uuid"));
            ctx.assertEquals(uuid, row.getValue("uuid"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("uuid"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("uuid"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("uuid"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("uuid"));
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
    }));
  }

  @Test
  public void testEncodeUUID(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"uuid\" = $1 WHERE \"id\" = $2 RETURNING \"uuid\"",
        ctx.asyncAssertSuccess(p -> {
          UUID uuid = UUID.fromString("92b53cf1-2ad0-49f9-be9d-ca48966e43ee");
          p.execute(Tuple.tuple()
            .addUUID(uuid)
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertEquals(uuid, row.getUUID(0));
            ctx.assertEquals(uuid, row.getValue(0));
            ctx.assertEquals(uuid, row.getUUID("uuid"));
            ctx.assertEquals(uuid, row.getValue("uuid"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("uuid"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("uuid"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("uuid"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("uuid"));
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
    }));
  }


  @Test
  public void testDecodeName(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Name\" FROM \"CharacterDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            String name = "What is my name ?";
            ctx.assertEquals(name, row.getString(0));
            ctx.assertEquals(name, row.getValue(0));
            ctx.assertEquals(name, row.getString("Name"));
            ctx.assertEquals(name, row.getValue("Name"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Name"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Name"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Name"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Name"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Name"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("Name"));
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
    }));
  }

  @Test
  public void testEncodeName(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"Name\" = upper($1) WHERE \"id\" = $2 RETURNING \"Name\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addString("vert.x")
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            String name = "VERT.X";
            ctx.assertEquals(name, row.getString(0));
            ctx.assertEquals(name, row.getValue(0));
            ctx.assertEquals(name, row.getString("Name"));
            ctx.assertEquals(name, row.getValue("Name"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Name"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Name"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Name"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Name"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Name"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("Name"));
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
    }));
  }


  @Test
  public void testDecodeChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"SingleChar\" FROM \"CharacterDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            String singleChar = "A";
            ctx.assertEquals(singleChar, row.getString(0));
            ctx.assertEquals(singleChar, row.getValue(0));
            ctx.assertEquals(singleChar, row.getString("SingleChar"));
            ctx.assertEquals(singleChar, row.getValue("SingleChar"));;
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("SingleChar"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("SingleChar"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("SingleChar"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("SingleChar"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("SingleChar"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("SingleChar"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("SingleChar"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("SingleChar"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("SingleChar"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("SingleChar"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("SingleChar"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("SingleChar"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("SingleChar"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("SingleChar"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("SingleChar"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"SingleChar\" = upper($1) WHERE \"id\" = $2 RETURNING \"SingleChar\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addString("b")
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            String singleChar = "B";
            ctx.assertEquals(singleChar, row.getString(0));
            ctx.assertEquals(singleChar, row.getValue(0));
            ctx.assertEquals(singleChar, row.getString("SingleChar"));
            ctx.assertEquals(singleChar, row.getValue("SingleChar"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("SingleChar"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("SingleChar"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("SingleChar"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("SingleChar"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("SingleChar"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("SingleChar"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("SingleChar"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("SingleChar"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("SingleChar"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("SingleChar"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("SingleChar"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("SingleChar"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("SingleChar"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("SingleChar"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("SingleChar"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeFixedChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"FixedChar\" FROM \"CharacterDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            String name = "YES";
            ctx.assertEquals(name, row.getString(0));
            ctx.assertEquals(name, row.getValue(0));
            ctx.assertEquals(name, row.getString("FixedChar"));
            ctx.assertEquals(name, row.getValue("FixedChar"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("FixedChar"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("FixedChar"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("FixedChar"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("FixedChar"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("FixedChar"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("FixedChar"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("FixedChar"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("FixedChar"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("FixedChar"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("FixedChar"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("FixedChar"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("FixedChar"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("FixedChar"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("FixedChar"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("FixedChar"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeFixedChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"FixedChar\" = upper($1) WHERE \"id\" = $2 RETURNING \"FixedChar\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addString("no")
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            String name = "NO ";
            ctx.assertEquals(name, row.getString(0));
            ctx.assertEquals(name, row.getValue(0));
            ctx.assertEquals(name, row.getString("FixedChar"));
            ctx.assertEquals(name, row.getValue("FixedChar"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("FixedChar"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("FixedChar"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("FixedChar"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("FixedChar"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("FixedChar"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("FixedChar"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("FixedChar"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("FixedChar"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("FixedChar"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("FixedChar"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("FixedChar"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("FixedChar"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("FixedChar"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("FixedChar"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("FixedChar"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeText(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Text\" FROM \"CharacterDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            String name = "Hello World";
            ctx.assertEquals(name, row.getString(0));
            ctx.assertEquals(name, row.getValue(0));
            ctx.assertEquals(name, row.getString("Text"));
            ctx.assertEquals(name, row.getValue("Text"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Text"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Text"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Text"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Text"));
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
    }));
  }

  @Test
  public void testEncodeText(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"Text\" = upper($1) WHERE \"id\" = $2 RETURNING \"Text\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addString("Hello World")
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            String name = "HELLO WORLD";
            ctx.assertEquals(name, row.getString(0));
            ctx.assertEquals(name, row.getValue(0));
            ctx.assertEquals(name, row.getString("Text"));
            ctx.assertEquals(name, row.getValue("Text"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Text"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Text"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Text"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Text"));
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
    }));
  }

  @Test
  public void testDecodeVarCharacter(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"VarCharacter\" FROM \"CharacterDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            String name = "Great!";
            ctx.assertEquals(name, row.getString(0));
            ctx.assertEquals(name, row.getValue(0));
            ctx.assertEquals(name, row.getString("VarCharacter"));
            ctx.assertEquals(name, row.getValue("VarCharacter"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("VarCharacter"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("VarCharacter"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("VarCharacter"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("VarCharacter"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("VarCharacter"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("VarCharacter"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("VarCharacter"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("VarCharacter"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("VarCharacter"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("VarCharacter"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("VarCharacter"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("VarCharacter"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("VarCharacter"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("VarCharacter"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("VarCharacter"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeVarCharacter(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"VarCharacter\" = upper($1) WHERE \"id\" = $2 RETURNING \"VarCharacter\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addString("Great!")
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            String name = "GREAT!";
            ctx.assertEquals(name, row.getString(0));
            ctx.assertEquals(name, row.getValue(0));
            ctx.assertEquals(name, row.getString("VarCharacter"));
            ctx.assertEquals(name, row.getValue("VarCharacter"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("VarCharacter"));
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("VarCharacter"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("VarCharacter"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("VarCharacter"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("VarCharacter"));
            ctx.assertNull(row.getCharacter(0));
            ctx.assertNull(row.getCharacter("VarCharacter"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("VarCharacter"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("VarCharacter"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("VarCharacter"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("VarCharacter"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("VarCharacter"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("VarCharacter"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("VarCharacter"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("VarCharacter"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("VarCharacter"));
            async.complete();
          }));
        }));
    }));
  }


}

package io.vertx.mysqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.time.LocalDateTime;

@RunWith(VertxUnitRunner.class)
public class DateTimeBinaryCodecTest extends DateTimeCodecTest {
  @Test
  public void testBinaryDecodeAll(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT `test_year`, `test_timestamp`, `test_datetime` FROM datatype WHERE id = 1", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(3, row.size());
        ctx.assertEquals((short) 2019, row.getValue(0));
        ctx.assertEquals(LocalDateTime.of(2000, 1, 1, 10, 20, 30), row.getValue(1));
        ctx.assertEquals(LocalDateTime.of(2000, 1, 1, 10, 20, 30, 123456000), row.getValue(2));
        conn.close();
      }));
    }));
  }

  @Test
  public void testEncodeNegative(TestContext ctx) {
    testEncodeTime(ctx, Duration.ofHours(-11).minusMinutes(12), Duration.ofHours(-11).minusMinutes(12));
  }

  @Test
  public void testEncodeMaxTime(TestContext ctx) {
    testEncodeTime(ctx, Duration.ofHours(838).plusMinutes(59).plusSeconds(59), Duration.ofHours(838).plusMinutes(59).plusSeconds(59));
  }

  @Test
  public void testEncodeMinTime(TestContext ctx) {
    testEncodeTime(ctx, Duration.ofHours(-838).minusMinutes(59).minusSeconds(59), Duration.ofHours(-838).minusMinutes(59).minusSeconds(59));
  }

  @Test
  public void testEncodeMaxTimeOverflow(TestContext ctx) {
    testEncodeTime(ctx, Duration.ofDays(120).plusHours(19).plusMinutes(27).plusSeconds(30), Duration.ofHours(838).plusMinutes(59).plusSeconds(59));
  }

  @Test
  public void testEncodeMinTimeOverflow(TestContext ctx) {
    testEncodeTime(ctx, Duration.ofDays(-120).plusHours(-19).plusMinutes(-27).plusSeconds(-30), Duration.ofHours(-838).plusMinutes(-59).plusSeconds(-59));
  }

  @Test
  public void testEncodeFractionalSecondsPart(TestContext ctx) {
    testEncodeTime(ctx, Duration.ofHours(11).plusMinutes(12).plusNanos(123456000), Duration.ofHours(11).plusMinutes(12).plusNanos(123456000));
  }

  @Test
  public void testDecodeYear(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "test_year", (short) 2019);
  }

  @Test
  public void testEncodeYear(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_year", (short) 2008);
  }

  @Test
  public void testDecodeTimestamp(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "test_timestamp", LocalDateTime.of(2000, 1, 1, 10, 20, 30));
  }

  @Test
  public void testEncodeTimestamp(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_timestamp", LocalDateTime.of(2001, 6, 20, 19, 40, 0));
  }

  @Test
  public void testDecodeDatetime(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "test_datetime", LocalDateTime.of(2000, 1, 1, 10, 20, 30, 123456000));
  }

  @Test
  public void testEncodeDatetimeWithoutTime(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_datetime", LocalDateTime.of(2001, 6, 20, 0, 0, 0, 0));
  }

  @Test
  public void testEncodeDatetimeWithoutMicrosecond(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_datetime", LocalDateTime.of(2001, 6, 20, 19, 40, 10));
  }

  @Test
  public void testEncodeDatetimeWithMicrosecond(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_datetime", LocalDateTime.of(2001, 6, 20, 19, 40, 0, 5000000));
  }

  @Test
  public void testEncodeDatetimeWithOnlyMicrosecond(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_datetime", LocalDateTime.of(2001, 6, 20, 0, 0, 0, 123456000));
  }

  private void testEncodeTime(TestContext ctx, Duration param, Duration expected) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("UPDATE basicdatatype SET `test_time` = ?" + " WHERE id = 2", Tuple.tuple().addValue(expected), ctx.asyncAssertSuccess(updateResult -> {
        conn.preparedQuery("SELECT `test_time` FROM basicdatatype WHERE id = 2", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals(expected, row.getValue(0));
          ctx.assertEquals(expected, row.getValue("test_time"));
          conn.close();
        }));
      }));
    }));
  }

  @Override
  protected <T> void testDecodeGeneric(TestContext ctx, String data, String dataType, String columnName, T expected) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT CAST(\'" + data + "\' AS " + dataType + ") " + columnName, ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(expected, row.getValue(0));
        ctx.assertEquals(expected, row.getValue(columnName));
        conn.close();
      }));
    }));
  }
}

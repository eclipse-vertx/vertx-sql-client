/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class DateTimeBinaryCodecTest extends DateTimeCodecTest {
  @Test
  public void testBinaryDecodeAll(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT `test_year`, `test_timestamp`, `test_datetime` FROM datatype WHERE id = 1").execute(ctx.asyncAssertSuccess(result -> {
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
  public void testEncodeTimeFromLocalTimeWithFractionalSecondsPart(TestContext ctx) {
    testEncodeTime(ctx, LocalTime.of(11, 12, 0, 123456000), Duration.ofHours(11).plusMinutes(12).plusNanos(123456000), LocalTime.of(11, 12, 0, 123456000));
  }

  @Test
  public void testEncodeTimeFromLocalTimeWithoutFractionalSecondsPart(TestContext ctx) {
    testEncodeTime(ctx, LocalTime.of(11, 12, 0, 0), Duration.ofHours(11).plusMinutes(12), LocalTime.of(11, 12, 0, 0));
  }

  @Test
  public void testEncodeZeroLocalTime(TestContext ctx) {
    testEncodeTime(ctx, LocalTime.of(0, 0, 0, 0), Duration.ofHours(0), LocalTime.of(0, 0, 0, 0));
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

  @Test
  public void testEncodeCastStringToDate(TestContext ctx) {
    testBinaryDecode(ctx, "SELECT * FROM basicdatatype WHERE id = 1 AND `test_date` = ?", Tuple.of("2019-01-01"), result -> {
      ctx.assertEquals(1, result.size());
      RowIterator<Row> iterator = result.iterator();
      Row row = iterator.next();
      ctx.assertEquals(1, row.getInteger("id"));
      ctx.assertEquals(LocalDate.of(2019, 1, 1), row.getLocalDate("test_date"));
    });
  }

  @Test
  public void testEncodeCastStringToTime(TestContext ctx) {
    testBinaryDecode(ctx, "SELECT * FROM basicdatatype WHERE id = 1 AND `test_time` = ?", Tuple.of("18:45:02"), result -> {
      ctx.assertEquals(1, result.size());
      RowIterator<Row> iterator = result.iterator();
      Row row = iterator.next();
      ctx.assertEquals(1, row.getInteger("id"));
      Duration expected = Duration.ZERO.plusHours(18).plusMinutes(45).plusSeconds(2);
      ctx.assertEquals(expected, row.getValue("test_time"));
    });
  }

  private void testEncodeTime(TestContext ctx, Duration param, Duration expected) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("UPDATE basicdatatype SET `test_time` = ?" + " WHERE id = 2").execute(Tuple.tuple().addValue(param), ctx.asyncAssertSuccess(updateResult -> {
        conn.preparedQuery("SELECT `test_time` FROM basicdatatype WHERE id = 2").execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals(expected, row.getValue(0));
          ctx.assertEquals(expected, row.getValue("test_time"));
          conn.close();
        }));
      }));
    }));
  }

  private void testEncodeTime(TestContext ctx, LocalTime param, Duration expectedDuration, LocalTime expectedLocalTime) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("UPDATE basicdatatype SET `test_time` = ?" + " WHERE id = 2").execute(Tuple.tuple().addValue(param), ctx.asyncAssertSuccess(updateResult -> {
        conn.preparedQuery("SELECT `test_time` FROM basicdatatype WHERE id = 2").execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals(expectedDuration, row.getValue(0));
          ctx.assertEquals(expectedDuration, row.getValue("test_time"));
          ctx.assertEquals(expectedLocalTime, row.getLocalTime(0));
          ctx.assertEquals(expectedLocalTime, row.getLocalTime("test_time"));
          conn.close();
        }));
      }));
    }));
  }

  @Override
  protected <T> void testDecodeGeneric(TestContext ctx, String data, String dataType, String columnName, T expected) {
    testDecodeGeneric(ctx, data, dataType, row -> {
      ctx.assertEquals(expected, row.getValue(0));
      ctx.assertEquals(expected, row.getValue(columnName));
    }, columnName);
  }

  @Override
  protected void testDecodeGeneric(TestContext ctx, String data, String dataType, Consumer<Row> valueAccessor, String columnName) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT CAST(\'" + data + "\' AS " + dataType + ") " + columnName).execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        valueAccessor.accept(row);
        conn.close();
      }));
    }));
  }
}

package io.vertx.tests.pgclient.data;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.data.Interval;
import io.vertx.tests.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

import java.time.*;

public class DateTimeTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {

  private static final LocalDateTime NOW = LocalDateTime.now(ZoneOffset.UTC);
  private static final LocalDateTime TODAY = LocalDateTime.of(NOW.toLocalDate(), LocalTime.MIDNIGHT);

  @Test
  public void testDate(TestContext ctx) {
    testDate(ctx, "1981-05-30", LocalDate.parse("1981-05-30"));
  }

  @Test
  public void testDatePlusToday(TestContext ctx) {
    testDate(ctx, "today", TODAY.toLocalDate());
  }

  @Test
  public void testDatePlusInfinity(TestContext ctx) {
    testDate(ctx, "infinity", LocalDate.MAX);
  }

  @Test
  public void testDateMinusInfinity(TestContext ctx) {
    testDate(ctx, "-infinity", LocalDate.MIN);
  }

  private void testDate(TestContext ctx, String value, LocalDate ld) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SET TIME ZONE 'UTC'")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(v -> {
        conn
          .query("SELECT '" + value + "'::DATE \"LocalDate\"")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(result -> {
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
    }));
  }

  @Test
  public void testTime(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '17:55:04.905120'::TIME \"LocalTime\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
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
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '17:55:04.90512+03:07'::TIMETZ \"OffsetTime\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
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
    testTimestamp(ctx, "2017-05-14 19:35:58.237666", LocalDateTime.parse("2017-05-14T19:35:58.237666"));
  }

  @Test
  public void testTimestampToday(TestContext ctx) {
    testTimestamp(ctx, "today", TODAY);
  }

  @Test
  public void testTimestampPlusInfinity(TestContext ctx) {
    testTimestamp(ctx, "infinity", LocalDateTime.MAX);
  }

  @Test
  public void testTimestampMinusInfinity(TestContext ctx) {
    testTimestamp(ctx, "-infinity", LocalDateTime.MIN);
  }

  private void testTimestamp(TestContext ctx, String value, LocalDateTime expected) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '" + value + "'::TIMESTAMP \"LocalDateTime\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "LocalDateTime")
            .returns(Tuple::getValue, Row::getValue, expected)
            .returns(Tuple::getLocalTime, Row::getLocalTime, expected.toLocalTime())
            .returns(Tuple::getLocalDate, Row::getLocalDate, expected.toLocalDate())
            .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, expected)
            .returns(Tuple::getTemporal, Row::getTemporal, expected)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testTimestampTz(TestContext ctx) {
    testTimestampTz(ctx, "2017-05-14 22:35:58.237666-03", OffsetDateTime.parse("2017-05-15T01:35:58.237666Z"));
  }

  @Test
  public void testTimestampTzToday(TestContext ctx) {
    testTimestampTz(ctx, "today", OffsetDateTime.of(TODAY, ZoneOffset.UTC));
  }

  @Test
  public void testTimestampTzPlusInfinity(TestContext ctx) {
    testTimestampTz(ctx, "infinity", OffsetDateTime.MAX);
  }

  @Test
  public void testTimestampTzMinusInfinity(TestContext ctx) {
    testTimestampTz(ctx, "-infinity", OffsetDateTime.MIN);
  }

  private void testTimestampTz(TestContext ctx, String value, OffsetDateTime expected) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SET TIME ZONE 'UTC'")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(v -> {
        conn
          .query("SELECT '" + value + "'::TIMESTAMPTZ \"OffsetDateTime\"")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "OffsetDateTime")
            .returns(Tuple::getValue, Row::getValue, expected)
            .returns(Tuple::getOffsetTime, Row::getOffsetTime, expected.toOffsetTime())
            .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, expected)
            .returns(Tuple::getLocalDate, Row::getLocalDate, expected.toLocalDate())
            .returns(Tuple::getLocalTime, Row::getLocalTime, expected.toLocalTime())
            .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, expected.toLocalDateTime())
            .returns(Tuple::getTemporal, Row::getTemporal, expected)
            .forRow(row);
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testInterval(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '10 years 3 months 332 days 20 hours 20 minutes 20.999991 seconds'::INTERVAL \"Interval\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
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
            .returns(Interval.class, interval)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testDecodeDATEArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['1998-05-11' :: DATE, '1998-05-11' :: DATE]", "LocalDate", Tuple::getArrayOfLocalDates, Row::getArrayOfLocalDates, LocalDate.parse("1998-05-11"), LocalDate.parse("1998-05-11"));
  }

  @Test
  public void testDecodeTIMEArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['17:55:04.90512' :: TIME WITHOUT TIME ZONE]", "LocalTime", Tuple::getArrayOfLocalTimes, Row::getArrayOfLocalTimes, lt);
  }

  @Test
  public void testDecodeTIMETZArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['17:55:04.90512+03' :: TIME WITH TIME ZONE]", "OffsetTime", Tuple::getArrayOfOffsetTimes, Row::getArrayOfOffsetTimes, dt);
  }

  @Test
  public void testDecodeTIMESTAMPArray(TestContext ctx) {
    ColumnChecker checker = ColumnChecker.checkColumn(0, "LocalDateTime")
      .returns(Tuple::getValue, Row::getValue, new Object[]{ldt})
      .returns(Tuple::getArrayOfLocalTimes, Row::getArrayOfLocalTimes, new Object[]{ldt.toLocalTime()})
      .returns(Tuple::getArrayOfLocalDates, Row::getArrayOfLocalDates, new Object[]{ldt.toLocalDate()})
      .returns(Tuple::getArrayOfLocalDateTimes, Row::getArrayOfLocalDateTimes, new Object[]{ldt});
    testDecodeGenericArray(ctx, "ARRAY ['2017-05-14 19:35:58.237666' :: TIMESTAMP WITHOUT TIME ZONE]", "LocalDateTime", checker);

  }

  @Test
  public void testDecodeTIMESTAMPTZArray(TestContext ctx) {
    ColumnChecker checker = ColumnChecker.checkColumn(0, "OffsetDateTime")
      .returns(Tuple::getValue, Row::getValue, new Object[]{odt})
      .returns(Tuple::getArrayOfOffsetTimes, Row::getArrayOfOffsetTimes, new Object[]{odt.toOffsetTime()})
      .returns(Tuple::getArrayOfOffsetDateTimes, Row::getArrayOfOffsetDateTimes, new Object[]{odt});
    testDecodeGenericArray(ctx, "ARRAY ['2017-05-14 23:59:59.237666-03' :: TIMESTAMP WITH TIME ZONE]", "OffsetDateTime", checker);
  }

  @Test
  public void testDecodeINTERVALArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['11 years 2 months 2 days 20 hours 20 minutes 20.999991 seconds'::INTERVAL, '20 minutes 20.123456 seconds'::INTERVAL, '30 months ago'::INTERVAL]", "Interval", Interval.class, intervals);
  }
}

package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

public class DateTimeTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
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
  public void testDecodeDATEArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['1998-05-11' :: DATE, '1998-05-11' :: DATE]", "LocalDate", Tuple::getLocalDateArray, Row::getLocalDateArray, LocalDate.parse("1998-05-11"), LocalDate.parse("1998-05-11"));
  }

  @Test
  public void testDecodeTIMEArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['17:55:04.90512' :: TIME WITHOUT TIME ZONE]", "LocalTime", Tuple::getLocalTimeArray, Row::getLocalTimeArray, lt);
  }

  @Test
  public void testDecodeTIMETZArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['17:55:04.90512+03' :: TIME WITH TIME ZONE]", "OffsetTime", Tuple::getOffsetTimeArray, Row::getOffsetTimeArray, dt);
  }

  @Test
  public void testDecodeTIMESTAMPArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['2017-05-14 19:35:58.237666' :: TIMESTAMP WITHOUT TIME ZONE]", "LocalDateTime", Tuple::getLocalDateTimeArray, Row::getLocalDateTimeArray, ldt);

  }

  @Test
  public void testDecodeTIMESTAMPTZArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['2017-05-14 23:59:59.237666-03' :: TIMESTAMP WITH TIME ZONE]", "OffsetDateTime", Tuple::getOffsetDateTimeArray, Row::getOffsetDateTimeArray, odt);
  }

  @Test
  public void testDecodeINTERVALArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['10 years 3 months 332 days 20 hours 20 minutes 20.999991 seconds'::INTERVAL, '20 minutes 20.123456 seconds'::INTERVAL, '30 months ago'::INTERVAL]", "Interval", Tuple::getIntervalArray, Row::getIntervalArray, intervals);
  }
}

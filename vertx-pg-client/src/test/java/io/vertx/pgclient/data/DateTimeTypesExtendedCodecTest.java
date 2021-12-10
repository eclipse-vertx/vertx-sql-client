package io.vertx.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

public class DateTimeTypesExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {

  @Test
  public void testDecodeDateBeforePgEpoch(TestContext ctx) {
    testDecodeDataTimeGeneric(ctx, "DATE", "Date", Tuple::getLocalDate, Row::getLocalDate, LocalDate.parse("1981-05-30"));
  }

  @Test
  public void testDecodeDatePlusInfinity(TestContext ctx) {
    testDecodeDataTimeGeneric(ctx, "DATE", "Date", Tuple::getLocalDate, Row::getLocalDate, LocalDate.MAX);
  }

  @Test
  public void testDecodeDateMinuxInfinity(TestContext ctx) {
    testDecodeDataTimeGeneric(ctx, "DATE", "Date", Tuple::getLocalDate, Row::getLocalDate, LocalDate.MIN);
  }

  @Test
  public void testEncodeDateBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"Date\" = $1 WHERE \"id\" = $2 RETURNING \"Date\"",
        ctx.asyncAssertSuccess(p -> {
          LocalDate ld = LocalDate.parse("1981-06-30");
          p.query().execute(Tuple.tuple()
            .addLocalDate(ld)
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Date")
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
  public void testDecodeDateAfterPgEpoch(TestContext ctx) {
    testDecodeDataTimeGeneric(ctx, "DATE", "Date", Tuple::getLocalDate, Row::getLocalDate, LocalDate.parse("2017-05-30"));
  }

  @Test
  public void testEncodeDateAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"Date\" = $1 WHERE \"id\" = $2 RETURNING \"Date\"",
        ctx.asyncAssertSuccess(p -> {
          LocalDate ld = LocalDate.parse("2018-05-30");
          p.query().execute(Tuple.tuple()
              .addLocalDate(ld)
              .addInteger(4)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.rowCount());
              Row row = result.iterator().next();
              ColumnChecker.checkColumn(0, "Date")
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
  public void testDecodeTime(TestContext ctx) {
    testDecodeDataTimeGeneric(ctx, "TIME WITHOUT TIME ZONE", "Time", Tuple::getLocalTime, Row::getLocalTime, LocalTime.parse("17:55:04.905120"));
  }

  @Test
  public void testEncodeTime(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE  \"TemporalDataType\" SET \"Time\" = $1 WHERE \"id\" = $2 RETURNING \"Time\"",
        ctx.asyncAssertSuccess(p -> {
          LocalTime lt = LocalTime.parse("22:55:04.905120");
          p.query().execute(Tuple.tuple()
              .addLocalTime(lt)
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.rowCount());
              Row row = result.iterator().next();
              ColumnChecker.checkColumn(0, "Time")
                .returns(Tuple::getValue, Row::getValue, lt)
                .returns(Tuple::getLocalTime, Row::getLocalTime, lt)
                .returns(Tuple::getTemporal, Row::getTemporal, lt)
                .forRow(row);
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeTimeTz(TestContext ctx) {
    testDecodeDataTimeGeneric(ctx, "TIME WITH TIME ZONE", "TimeTz", Tuple::getOffsetTime, Row::getOffsetTime, OffsetTime.parse("17:55:04.905120+03:07"));
  }

  @Test
  public void testEncodeTimeTz(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"TimeTz\" = $1 WHERE \"id\" = $2 RETURNING \"TimeTz\"",
        ctx.asyncAssertSuccess(p -> {
          OffsetTime ot = OffsetTime.parse("20:55:04.905120+03:07");
          p.query().execute(Tuple.tuple()
            .addOffsetTime(ot)
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "TimeTz")
              .returns(Tuple::getValue, Row::getValue, ot)
              .returns(Tuple::getOffsetTime, Row::getOffsetTime, ot)
              .returns(Tuple::getTemporal, Row::getTemporal, ot)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeTimestampBeforePgEpoch(TestContext ctx) {
    LocalDateTime expected = LocalDateTime.parse("1800-01-01T23:57:53.237666");
    ColumnChecker checker = ColumnChecker.checkColumn(0, "Timestamp")
      .returns(Tuple::getValue, Row::getValue, expected)
      .returns(Tuple::getLocalTime, Row::getLocalTime, expected.toLocalTime())
      .returns(Tuple::getLocalDate, Row::getLocalDate, expected.toLocalDate())
      .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, expected)
      .returns(Tuple::getTemporal, Row::getTemporal, expected);
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITHOUT TIME ZONE", "Timestamp", checker, expected);
  }

  @Test
  public void testDecodeTimestampPlusInfinity(TestContext ctx) {
    LocalDateTime expected = LocalDateTime.MAX;
    ColumnChecker checker = ColumnChecker.checkColumn(0, "Timestamp")
      .returns(Tuple::getValue, Row::getValue, expected)
      .returns(Tuple::getLocalTime, Row::getLocalTime, expected.toLocalTime())
      .returns(Tuple::getLocalDate, Row::getLocalDate, expected.toLocalDate())
      .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, expected)
      .returns(Tuple::getTemporal, Row::getTemporal, expected);
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITHOUT TIME ZONE", "Timestamp", checker, expected);
  }

  @Test
  public void testDecodeTimestampMinusInfinity(TestContext ctx) {
    LocalDateTime expected = LocalDateTime.MIN;
    ColumnChecker checker = ColumnChecker.checkColumn(0, "Timestamp")
      .returns(Tuple::getValue, Row::getValue, expected)
      .returns(Tuple::getLocalTime, Row::getLocalTime, expected.toLocalTime())
      .returns(Tuple::getLocalDate, Row::getLocalDate, expected.toLocalDate())
      .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, expected)
      .returns(Tuple::getTemporal, Row::getTemporal, expected);
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITHOUT TIME ZONE", "Timestamp", checker, expected);
  }

  @Test
  public void testEncodeTimestampBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"Timestamp\" = $1 WHERE \"id\" = $2 RETURNING \"Timestamp\"",
        ctx.asyncAssertSuccess(p -> {
          LocalDateTime ldt = LocalDateTime.parse("1900-02-01T23:57:53.237666");
          p.query().execute(Tuple.tuple()
            .addLocalDateTime(ldt)
            .addInteger(4), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Timestamp")
              .returns(Tuple::getValue, Row::getValue, ldt)
              .returns(Tuple::getLocalDate, Row::getLocalDate, ldt.toLocalDate())
              .returns(Tuple::getLocalTime, Row::getLocalTime, ldt.toLocalTime())
              .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, ldt)
              .returns(Tuple::getTemporal, Row::getTemporal, ldt)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeTimestampAfterPgEpoch(TestContext ctx) {
    ColumnChecker checker = ColumnChecker.checkColumn(0, "Timestamp")
      .returns(Tuple::getValue, Row::getValue, ldt)
      .returns(Tuple::getLocalDate, Row::getLocalDate, ldt.toLocalDate())
      .returns(Tuple::getLocalTime, Row::getLocalTime, ldt.toLocalTime())
      .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, ldt)
      .returns(Tuple::getTemporal, Row::getTemporal, ldt);
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITHOUT TIME ZONE", "Timestamp", checker, ldt);
  }

  @Test
  public void testEncodeTimestampAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"Timestamp\" =$1 WHERE \"id\" = $2 RETURNING \"Timestamp\"",
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple()
              .addLocalDateTime(LocalDateTime.parse("2017-05-14T19:35:58.237666"))
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
              Row row = result.iterator().next();
              ColumnChecker.checkColumn(0, "Timestamp")
                .returns(Tuple::getValue, Row::getValue, ldt)
                .returns(Tuple::getLocalTime, Row::getLocalTime, ldt.toLocalTime())
                .returns(Tuple::getLocalDate, Row::getLocalDate, ldt.toLocalDate())
                .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, ldt)
                .returns(Tuple::getTemporal, Row::getTemporal, ldt)
                .forRow(row);
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeTimestampTzBeforePgEpoch(TestContext ctx) {
    OffsetDateTime expected = OffsetDateTime.parse("1800-01-02T02:59:59.237666Z");
    ColumnChecker checker = ColumnChecker.checkColumn(0, "TimestampTz")
      .returns(Tuple::getValue, Row::getValue, expected)
      .returns(Tuple::getOffsetTime, Row::getOffsetTime, expected.toOffsetTime())
      .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, expected)
      .returns(Tuple::getLocalDate, Row::getLocalDate, expected.toLocalDate())
      .returns(Tuple::getLocalTime, Row::getLocalTime, expected.toLocalTime())
      .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, expected.toLocalDateTime())
      .returns(Tuple::getTemporal, Row::getTemporal, expected);
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITH TIME ZONE", "TimestampTz", checker, expected);
  }

  @Test
  public void testDecodeTimestampTzPlusInfinity(TestContext ctx) {
    OffsetDateTime expected = OffsetDateTime.MAX;
    ColumnChecker checker = ColumnChecker.checkColumn(0, "TimestampTz")
      .returns(Tuple::getValue, Row::getValue, expected)
      .returns(Tuple::getOffsetTime, Row::getOffsetTime, expected.toOffsetTime())
      .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, expected)
      .returns(Tuple::getLocalDate, Row::getLocalDate, expected.toLocalDate())
      .returns(Tuple::getLocalTime, Row::getLocalTime, expected.toLocalTime())
      .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, expected.toLocalDateTime())
      .returns(Tuple::getTemporal, Row::getTemporal, expected);
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITH TIME ZONE", "TimestampTz", checker, expected);
  }

  @Test
  public void testDecodeTimestampTzMinusInfinity(TestContext ctx) {
    OffsetDateTime expected = OffsetDateTime.MIN;
    ColumnChecker checker = ColumnChecker.checkColumn(0, "TimestampTz")
      .returns(Tuple::getValue, Row::getValue, expected)
      .returns(Tuple::getOffsetTime, Row::getOffsetTime, expected.toOffsetTime())
      .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, expected)
      .returns(Tuple::getLocalDate, Row::getLocalDate, expected.toLocalDate())
      .returns(Tuple::getLocalTime, Row::getLocalTime, expected.toLocalTime())
      .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, expected.toLocalDateTime())
      .returns(Tuple::getTemporal, Row::getTemporal, expected);
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITH TIME ZONE", "TimestampTz", checker, expected);
  }

  @Test
  public void testEncodeTimestampTzBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'").execute(ctx.asyncAssertSuccess(v -> {
        conn.prepare("UPDATE \"TemporalDataType\" SET \"TimestampTz\" =$1 WHERE \"id\" = $2 RETURNING \"TimestampTz\"",
          ctx.asyncAssertSuccess(p -> {
            p.query().execute(Tuple.tuple()
                .addOffsetDateTime(OffsetDateTime.parse("1800-02-01T23:59:59.237666-03:00"))
                .addInteger(3)
              , ctx.asyncAssertSuccess(result -> {
                OffsetDateTime odt = OffsetDateTime.parse("1800-02-02T02:59:59.237666Z");
                ctx.assertEquals(1, result.rowCount());
                ctx.assertEquals(1, result.size());
                Row row = result.iterator().next();
                ColumnChecker.checkColumn(0, "TimestampTz")
                  .returns(Tuple::getValue, Row::getValue, odt)
                  .returns(Tuple::getOffsetTime, Row::getOffsetTime, odt.toOffsetTime())
                  .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, odt)
                  .returns(Tuple::getLocalDate, Row::getLocalDate, odt.toLocalDate())
                  .returns(Tuple::getLocalTime, Row::getLocalTime, odt.toLocalTime())
                  .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, odt.toLocalDateTime())
                  .returns(Tuple::getTemporal, Row::getTemporal, odt)
                  .forRow(row);
                async.complete();
              }));
          }));
      }));
    }));
  }

  @Test
  public void testDecodeTimestampTzAfterPgEpoch(TestContext ctx) {
    OffsetDateTime expected = OffsetDateTime.parse("2017-05-15T02:59:59.237666Z");
    ColumnChecker checker = ColumnChecker.checkColumn(0, "TimestampTz")
      .returns(Tuple::getValue, Row::getValue, expected)
      .returns(Tuple::getOffsetTime, Row::getOffsetTime, expected.toOffsetTime())
      .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, expected)
      .returns(Tuple::getLocalDate, Row::getLocalDate, expected.toLocalDate())
      .returns(Tuple::getLocalTime, Row::getLocalTime, expected.toLocalTime())
      .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, expected.toLocalDateTime())
      .returns(Tuple::getTemporal, Row::getTemporal, expected);
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITH TIME ZONE", "TimestampTz", checker, expected);
  }

  @Test
  public void testEncodeTimestampTzAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'").execute(ctx.asyncAssertSuccess(v -> {
        conn.prepare("UPDATE \"TemporalDataType\" SET \"TimestampTz\" = $1 WHERE \"id\" = $2 RETURNING \"TimestampTz\"",
          ctx.asyncAssertSuccess(p -> {
            p.query().execute(Tuple.tuple()
                .addOffsetDateTime(OffsetDateTime.parse("2017-06-14T23:59:59.237666-03:00"))
                .addInteger(1)
              , ctx.asyncAssertSuccess(result -> {
                ctx.assertEquals(1, result.size());
                ctx.assertEquals(1, result.rowCount());
                OffsetDateTime odt = OffsetDateTime.parse("2017-06-15T02:59:59.237666Z");
                Row row = result.iterator().next();
                ColumnChecker.checkColumn(0, "TimestampTz")
                  .returns(Tuple::getValue, Row::getValue, odt)
                  .returns(Tuple::getOffsetTime, Row::getOffsetTime, odt.toOffsetTime())
                  .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, odt)
                  .returns(Tuple::getLocalDate, Row::getLocalDate, odt.toLocalDate())
                  .returns(Tuple::getLocalTime, Row::getLocalTime, odt.toLocalTime())
                  .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, odt.toLocalDateTime())
                  .returns(Tuple::getTemporal, Row::getTemporal, odt)
                  .forRow(row);
                async.complete();
              }));
          }));
      }));
    }));
  }

  @Test
  public void testDecodeInterval(TestContext ctx) {
    Interval interval = Interval.of()
      .years(10)
      .months(3)
      .days(332)
      .hours(20)
      .minutes(20)
      .seconds(20)
      .microseconds(999999);

    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1 :: INTERVAL \"Interval\"",
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple().addValue(interval), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(0, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Interval")
              .returns(Tuple::getValue, Row::getValue, interval)
              .returns(Interval.class, interval)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeInterval(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"Interval\" = $1 WHERE \"id\" = $2 RETURNING \"Interval\"",
        ctx.asyncAssertSuccess(p -> {
          // 2000 years 1 months 403 days 59 hours 35 minutes 13.999998 seconds
          Interval expected = Interval.of()
            .years(2000)
            .months(1)
            .days(403)
            .hours(59)
            .minutes(35)
            .seconds(13)
            .microseconds(999998);
          p.query().execute(Tuple.tuple()
            .addValue(expected)
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Interval")
              .returns(Tuple::getValue, Row::getValue, expected)
              .returns(Interval.class, expected)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeLocalDateArray(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: DATE [] \"LocalDate\"", new LocalDate[][]{new LocalDate[]{LocalDate.parse("1998-05-11"), LocalDate.parse("1998-05-11")}}, Row::getArrayOfLocalDates);
  }

  @Test
  public void testEncodeLocalDateArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"LocalDate\" = $1  WHERE \"id\" = $2 RETURNING \"LocalDate\"",
        ctx.asyncAssertSuccess(p -> {
          final LocalDate dt = LocalDate.parse("1998-05-12");
          p.query().execute(Tuple.tuple()
              .addArrayOfLocalDate(new LocalDate[]{dt})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "LocalDate")
                .returns(Tuple::getValue, Row::getValue, new LocalDate[]{dt})
                .returns(Tuple::getArrayOfLocalDates, Row::getArrayOfLocalDates, new LocalDate[]{dt})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeLocalTimeArray(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: TIME WITHOUT TIME ZONE [] \"LocalTime\"", new LocalTime[][]{new LocalTime[]{lt}}, Row::getArrayOfLocalTimes);
  }

  @Test
  public void testEncodeLocalTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"LocalTime\" = $1  WHERE \"id\" = $2 RETURNING \"LocalTime\"",
        ctx.asyncAssertSuccess(p -> {
          final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSS");
          final LocalTime dt = LocalTime.parse("17:55:04.90512", dtf);
          p.query().execute(Tuple.tuple()
              .addArrayOfLocalTime(new LocalTime[]{dt})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "LocalTime")
                .returns(Tuple::getValue, Row::getValue, new LocalTime[]{dt})
                .returns(Tuple::getArrayOfLocalTimes, Row::getArrayOfLocalTimes, new LocalTime[]{dt})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeOffsetTimeArray(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: TIME WITH TIME ZONE [] \"OffsetTime\"", new OffsetTime[][]{new OffsetTime[]{dt}}, Row::getArrayOfOffsetTimes);
  }

  @Test
  public void testEncodeOffsetTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"OffsetTime\" = $1  WHERE \"id\" = $2 RETURNING \"OffsetTime\"",
        ctx.asyncAssertSuccess(p -> {
          final OffsetTime dt = OffsetTime.parse("17:56:04.90512+03:07");
          p.query().execute(Tuple.tuple()
              .addArrayOfOffsetTime(new OffsetTime[]{dt})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "OffsetTime")
                .returns(Tuple::getValue, Row::getValue, new OffsetTime[]{dt})
                .returns(Tuple::getArrayOfOffsetTimes, Row::getArrayOfOffsetTimes, new OffsetTime[]{dt})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeLocalDateTimeArray(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: TIMESTAMP WITHOUT TIME ZONE [] \"LocalDateTime\"", new LocalDateTime[][]{new LocalDateTime[]{LocalDateTime.parse("2017-05-14T19:35:58.237666")}}, Row::getArrayOfLocalDateTimes);
  }

  @Test
  public void testEncodeLocalDateTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"LocalDateTime\" = $1  WHERE \"id\" = $2 RETURNING \"LocalDateTime\"",
        ctx.asyncAssertSuccess(p -> {
          final LocalDateTime dt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
          p.query().execute(Tuple.tuple()
              .addArrayOfLocalDateTime(new LocalDateTime[]{dt})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "LocalDateTime")
                .returns(Tuple::getValue, Row::getValue, new LocalDateTime[]{dt})
                .returns(Tuple::getArrayOfLocalTimes, Row::getArrayOfLocalTimes, new LocalTime[]{dt.toLocalTime()})
                .returns(Tuple::getArrayOfLocalDates, Row::getArrayOfLocalDates, new LocalDate[]{dt.toLocalDate()})
                .returns(Tuple::getArrayOfLocalDateTimes, Row::getArrayOfLocalDateTimes, new LocalDateTime[]{dt})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeOffsetDateTimeArray(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: TIMESTAMP WITH TIME ZONE [] \"OffsetDateTime\"", new OffsetDateTime[][]{new OffsetDateTime[]{odt}}, Row::getArrayOfOffsetDateTimes);
  }

  @Test
  public void testEncodeOffsetDateTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"OffsetDateTime\" = $1  WHERE \"id\" = $2 RETURNING \"OffsetDateTime\"",
        ctx.asyncAssertSuccess(p -> {
          final OffsetDateTime dt = OffsetDateTime.parse("2017-05-14T19:35:58.237666Z");
          p.query().execute(Tuple.tuple()
              .addArrayOfOffsetDateTime(new OffsetDateTime[]{dt})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "OffsetDateTime")
                .returns(Tuple::getValue, Row::getValue, new OffsetDateTime[]{dt})
                .returns(Tuple::getArrayOfOffsetTimes, Row::getArrayOfOffsetTimes, new OffsetTime[]{dt.toOffsetTime()})
                .returns(Tuple::getArrayOfOffsetDateTimes, Row::getArrayOfOffsetDateTimes, new OffsetDateTime[]{dt})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeIntervalArray(TestContext ctx) {
    testGenericArray(ctx, "SELECT $1 :: INTERVAL [] \"Interval\"", new Interval[][]{intervals}, Interval.class);
  }

  @Test
  public void testEncodeIntervalArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Interval\" = $1  WHERE \"id\" = $2 RETURNING \"Interval\"",
        ctx.asyncAssertSuccess(p -> {
          Interval[] intervals = new Interval[]{
            Interval.of().years(10).months(3).days(332).hours(20).minutes(20).seconds(20).microseconds(999991),
            Interval.of().minutes(20).seconds(20).microseconds(123456),
            Interval.of().years(-2).months(-6),
            Interval.of()
          };
          p.query().execute(Tuple.tuple()
              .addValue(intervals)
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Interval")
                .returns(Tuple::getValue, Row::getValue, intervals)
                .returns(Interval.class, intervals)
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  private <T> void testDecodeDataTimeGeneric(TestContext ctx,
                                             String dataType,
                                             String columnName,
                                             ColumnChecker.SerializableBiFunction<Tuple, Integer, T> byIndexGetter,
                                             ColumnChecker.SerializableBiFunction<Row, String, T> byNameGetter,
                                             T expected) {
    testDecodeDataTimeGeneric(ctx, dataType, columnName, ColumnChecker.checkColumn(0, columnName)
      .returns(Tuple::getValue, Row::getValue, expected)
      .returns(byIndexGetter, byNameGetter, expected)
      .returns(Tuple::getTemporal, Row::getTemporal, expected), expected);
  }

  private <T> void testDecodeDataTimeGeneric(TestContext ctx,
                                             String dataType,
                                             String columnName,
                                             ColumnChecker checker,
                                             T expected) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1 :: " + dataType + " \"" + columnName + "\"",
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple().addValue(expected), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(0, result.rowCount());
            Row row = result.iterator().next();
            checker.forRow(row);
            async.complete();
          }));
        }));
    }));
  }
}

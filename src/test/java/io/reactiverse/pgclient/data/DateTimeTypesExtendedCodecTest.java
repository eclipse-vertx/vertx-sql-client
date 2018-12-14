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
import java.time.format.DateTimeFormatter;

public class DateTimeTypesExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {
  @Test
  public void testDecodeDateBeforePgEpoch(TestContext ctx) {
    testDecodeDataTimeGeneric(ctx, "DATE", "Date", Tuple::getLocalDate, Row::getLocalDate, LocalDate.parse("1981-05-30"));
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"Date\" = $1 WHERE \"id\" = $2 RETURNING \"Date\"",
        ctx.asyncAssertSuccess(p -> {
          LocalDate ld = LocalDate.parse("2018-05-30");
          p.execute(Tuple.tuple()
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE  \"TemporalDataType\" SET \"Time\" = $1 WHERE \"id\" = $2 RETURNING \"Time\"",
        ctx.asyncAssertSuccess(p -> {
          LocalTime lt = LocalTime.parse("22:55:04.905120");
          p.execute(Tuple.tuple()
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"TemporalDataType\" SET \"TimeTz\" = $1 WHERE \"id\" = $2 RETURNING \"TimeTz\"",
        ctx.asyncAssertSuccess(p -> {
          OffsetTime ot = OffsetTime.parse("20:55:04.905120+03:07");
          p.execute(Tuple.tuple()
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
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITHOUT TIME ZONE", "Timestamp", Tuple::getLocalDateTime, Row::getLocalDateTime, LocalDateTime.parse("1800-01-01T23:57:53.237666"));
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
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Timestamp")
              .returns(Tuple::getValue, Row::getValue, ldt)
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
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITHOUT TIME ZONE", "Timestamp", Tuple::getLocalDateTime, Row::getLocalDateTime, ldt);
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
              ColumnChecker.checkColumn(0, "Timestamp")
                .returns(Tuple::getValue, Row::getValue, ldt)
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
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITH TIME ZONE", "TimestampTz", Tuple::getOffsetDateTime, Row::getOffsetDateTime, OffsetDateTime.parse("1800-01-02T02:59:59.237666Z"));
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
                ctx.assertEquals(1, result.rowCount());
                ctx.assertEquals(1, result.size());
                Row row = result.iterator().next();
                ColumnChecker.checkColumn(0, "TimestampTz")
                  .returns(Tuple::getValue, Row::getValue, odt)
                  .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, odt)
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
    testDecodeDataTimeGeneric(ctx, "TIMESTAMP WITH TIME ZONE", "TimestampTz", Tuple::getOffsetDateTime, Row::getOffsetDateTime, OffsetDateTime.parse("2017-05-15T02:59:59.237666Z"));
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
                ctx.assertEquals(1, result.rowCount());
                OffsetDateTime odt = OffsetDateTime.parse("2017-06-15T02:59:59.237666Z");
                Row row = result.iterator().next();
                ColumnChecker.checkColumn(0, "TimestampTz")
                  .returns(Tuple::getValue, Row::getValue, odt)
                  .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, odt)
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1 :: INTERVAL \"Interval\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInterval(interval), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Interval")
              .returns(Tuple::getValue, Row::getValue, interval)
              .returns(Tuple::getInterval, Row::getInterval, interval)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeInterval(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
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
          p.execute(Tuple.tuple()
            .addInterval(expected)
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Interval")
              .returns(Tuple::getValue, Row::getValue, expected)
              .returns(Tuple::getInterval, Row::getInterval, expected)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeLocalDateArray(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: DATE [] \"LocalDate\"", new LocalDate[][]{new LocalDate[]{LocalDate.parse("1998-05-11"), LocalDate.parse("1998-05-11")}}, Row::getLocalDateArray);
  }

  @Test
  public void testEncodeLocalDateArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"LocalDate\" = $1  WHERE \"id\" = $2 RETURNING \"LocalDate\"",
        ctx.asyncAssertSuccess(p -> {
          final LocalDate dt = LocalDate.parse("1998-05-12");
          p.execute(Tuple.tuple()
              .addLocalDateArray(new LocalDate[]{dt})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "LocalDate")
                .returns(Tuple::getValue, Row::getValue, new LocalDate[]{dt})
                .returns(Tuple::getLocalDateArray, Row::getLocalDateArray, new LocalDate[]{dt})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeLocalTimeArray(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: TIME WITHOUT TIME ZONE [] \"LocalTime\"", new LocalTime[][]{new LocalTime[]{lt}}, Row::getLocalTimeArray);
  }

  @Test
  public void testEncodeLocalTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"LocalTime\" = $1  WHERE \"id\" = $2 RETURNING \"LocalTime\"",
        ctx.asyncAssertSuccess(p -> {
          final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSS");
          final LocalTime dt = LocalTime.parse("17:55:04.90512", dtf);
          p.execute(Tuple.tuple()
              .addLocalTimeArray(new LocalTime[]{dt})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "LocalTime")
                .returns(Tuple::getValue, Row::getValue, new LocalTime[]{dt})
                .returns(Tuple::getLocalTimeArray, Row::getLocalTimeArray, new LocalTime[]{dt})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeOffsetTimeArray(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: TIME WITH TIME ZONE [] \"OffsetTime\"", new OffsetTime[][]{new OffsetTime[]{dt}}, Row::getOffsetTimeArray);
  }

  @Test
  public void testEncodeOffsetTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"OffsetTime\" = $1  WHERE \"id\" = $2 RETURNING \"OffsetTime\"",
        ctx.asyncAssertSuccess(p -> {
          final OffsetTime dt = OffsetTime.parse("17:56:04.90512+03:07");
          p.execute(Tuple.tuple()
              .addOffsetTimeArray(new OffsetTime[]{dt})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "OffsetTime")
                .returns(Tuple::getValue, Row::getValue, new OffsetTime[]{dt})
                .returns(Tuple::getOffsetTimeArray, Row::getOffsetTimeArray, new OffsetTime[]{dt})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeLocalDateTimeArray(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: TIMESTAMP WITHOUT TIME ZONE [] \"LocalDateTime\"", new LocalDateTime[][]{new LocalDateTime[]{LocalDateTime.parse("2017-05-14T19:35:58.237666")}}, Row::getLocalDateTimeArray);
  }

  @Test
  public void testEncodeLocalDateTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"LocalDateTime\" = $1  WHERE \"id\" = $2 RETURNING \"LocalDateTime\"",
        ctx.asyncAssertSuccess(p -> {
          final LocalDateTime dt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
          p.execute(Tuple.tuple()
              .addLocalDateTimeArray(new LocalDateTime[]{dt})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "LocalDateTime")
                .returns(Tuple::getValue, Row::getValue, new LocalDateTime[]{dt})
                .returns(Tuple::getLocalDateTimeArray, Row::getLocalDateTimeArray, new LocalDateTime[]{dt})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeOffsetDateTimeArray(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: TIMESTAMP WITH TIME ZONE [] \"OffsetDateTime\"", new OffsetDateTime[][]{new OffsetDateTime[]{odt}}, Row::getOffsetDateTimeArray);
  }

  @Test
  public void testEncodeOffsetDateTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"OffsetDateTime\" = $1  WHERE \"id\" = $2 RETURNING \"OffsetDateTime\"",
        ctx.asyncAssertSuccess(p -> {
          final OffsetDateTime dt = OffsetDateTime.parse("2017-05-14T19:35:58.237666Z");
          p.execute(Tuple.tuple()
              .addOffsetDateTimeArray(new OffsetDateTime[]{dt})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "OffsetDateTime")
                .returns(Tuple::getValue, Row::getValue, new OffsetDateTime[]{dt})
                .returns(Tuple::getOffsetDateTimeArray, Row::getOffsetDateTimeArray, new OffsetDateTime[]{dt})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeIntervalArray(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: INTERVAL [] \"Interval\"", new Interval[][]{intervals}, Row::getIntervalArray);
  }

  @Test
  public void testEncodeIntervalArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Interval\" = $1  WHERE \"id\" = $2 RETURNING \"Interval\"",
        ctx.asyncAssertSuccess(p -> {
          Interval[] intervals = new Interval[]{
            Interval.of().years(10).months(3).days(332).hours(20).minutes(20).seconds(20).microseconds(999991),
            Interval.of().minutes(20).seconds(20).microseconds(123456),
            Interval.of().years(-2).months(-6),
            Interval.of()
          };
          p.execute(Tuple.tuple()
              .addIntervalArray(intervals)
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Interval")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(intervals))
                .returns(Tuple::getIntervalArray, Row::getIntervalArray, ColumnChecker.toObjectArray(intervals))
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1 :: " + dataType + " \"" + columnName + "\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addValue(expected), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, columnName)
              .returns(Tuple::getValue, Row::getValue, expected)
              .returns(byIndexGetter, byNameGetter, expected)
              .returns(Tuple::getTemporal, Row::getTemporal, expected)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }
}

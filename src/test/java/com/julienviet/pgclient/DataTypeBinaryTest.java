package com.julienviet.pgclient;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
            ColumnChecker.checkColumn(0, "Boolean")
              .returns(Tuple::getValue, Row::getValue, true)
              .returns(Tuple::getBoolean, Row::getBoolean, true)
              .forRow(row);
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
              ColumnChecker.checkColumn(0, "Boolean")
                .returns(Tuple::getValue, Row::getValue, false)
                .returns(Tuple::getBoolean, Row::getBoolean, false)
                .forRow(row);
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
            ColumnChecker.checkColumn(0, "Short")
              .returns(Tuple::getValue, Row::getValue, Short.MAX_VALUE)
              .returns(Tuple::getInteger, Row::getInteger, 32767)
              .returns(Tuple::getLong, Row::getLong, 32767L)
              .returns(Tuple::getFloat, Row::getFloat, 32767f)
              .returns(Tuple::getDouble, Row::getDouble, 32767d)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(32767))
              .returns(Tuple::getNumeric, Row::getNumeric, Numeric.create(32767))
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "Short")
              .returns(Tuple::getValue, Row::getValue, Short.MIN_VALUE)
              .returns(Tuple::getInteger, Row::getInteger, -32768)
              .returns(Tuple::getLong, Row::getLong, -32768L)
              .returns(Tuple::getFloat, Row::getFloat, -32768f)
              .returns(Tuple::getDouble, Row::getDouble, -32768d)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(-32768))
              .returns(Tuple::getNumeric, Row::getNumeric, Numeric.create(-32768))
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "Integer")
              .returns(Tuple::getValue, Row::getValue, Integer.MAX_VALUE)
              .returns(Tuple::getInteger, Row::getInteger, Integer.MAX_VALUE)
              .returns(Tuple::getLong, Row::getLong, 2147483647L)
              .returns(Tuple::getFloat, Row::getFloat, 2147483647f)
              .returns(Tuple::getDouble, Row::getDouble, 2147483647d)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(2147483647))
              .returns(Tuple::getNumeric, Row::getNumeric, Numeric.create(2147483647))
              .forRow(row);
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
              ColumnChecker.checkColumn(0, "Integer")
                .returns(Tuple::getValue, Row::getValue, Integer.MIN_VALUE)
                .returns(Tuple::getInteger, Row::getInteger, Integer.MIN_VALUE)
                .returns(Tuple::getLong, Row::getLong, -2147483648L)
                .returns(Tuple::getFloat, Row::getFloat, -2147483648f)
                .returns(Tuple::getDouble, Row::getDouble, -2147483648d)
                .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(-2147483648))
                .returns(Tuple::getNumeric, Row::getNumeric, Numeric.create(-2147483648))
                .forRow(row);
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
            ColumnChecker.checkColumn(0, "Long")
              .returns(Tuple::getValue, Row::getValue, Long.MAX_VALUE)
              .returns(Tuple::getInteger, Row::getInteger, -1)
              .returns(Tuple::getLong, Row::getLong, Long.MAX_VALUE)
              .returns(Tuple::getFloat, Row::getFloat, 9.223372E18f)
              .returns(Tuple::getDouble, Row::getDouble, 9.223372036854776E18d)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(Long.MAX_VALUE))
              .returns(Tuple::getNumeric, Row::getNumeric, Numeric.create(Long.MAX_VALUE))
              .forRow(row);
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
              ColumnChecker.checkColumn(0, "Long")
                .returns(Tuple::getValue, Row::getValue, Long.MIN_VALUE)
                .returns(Tuple::getInteger, Row::getInteger, 0)
                .returns(Tuple::getLong, Row::getLong, Long.MIN_VALUE)
                .returns(Tuple::getFloat, Row::getFloat, -9.223372E18f)
                .returns(Tuple::getDouble, Row::getDouble, -9.223372036854776E18d)
                .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(Long.MIN_VALUE))
                .returns(Tuple::getNumeric, Row::getNumeric, Numeric.create(Long.MIN_VALUE))
                .forRow(row);
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
            ColumnChecker.checkColumn(0, "Float")
              .returns(Tuple::getValue, Row::getValue, Float.MAX_VALUE)
              .returns(Tuple::getInteger, Row::getInteger, 2147483647)
              .returns(Tuple::getLong, Row::getLong, 9223372036854775807L)
              .returns(Tuple::getFloat, Row::getFloat, Float.MAX_VALUE)
              .returns(Tuple::getDouble, Row::getDouble, 3.4028234663852886E38d)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("" + Float.MAX_VALUE))
              .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("" + Float.MAX_VALUE))
              .forRow(row);
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
              ColumnChecker.checkColumn(0, "Float")
                .returns(Tuple::getValue, Row::getValue, Float.MIN_VALUE)
                .returns(Tuple::getInteger, Row::getInteger, 0)
                .returns(Tuple::getLong, Row::getLong, 0L)
                .returns(Tuple::getFloat, Row::getFloat, Float.MIN_VALUE)
                .returns(Tuple::getDouble, Row::getDouble, 1.401298464324817E-45d)
                .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("" + Float.MIN_VALUE))
                .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("" + Float.MIN_VALUE))
                .forRow(row);
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
            ColumnChecker.checkColumn(0, "Double")
              .returns(Tuple::getValue, Row::getValue, Double.MAX_VALUE)
              .returns(Tuple::getInteger, Row::getInteger, 2147483647)
              .returns(Tuple::getLong, Row::getLong, 9223372036854775807L)
              .returns(Tuple::getFloat, Row::getFloat, Float.POSITIVE_INFINITY)
              .returns(Tuple::getDouble, Row::getDouble, Double.MAX_VALUE)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("" + Double.MAX_VALUE))
              .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("" + Double.MAX_VALUE))
              .forRow(row);
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
              ColumnChecker.checkColumn(0, "Double")
                .returns(Tuple::getValue, Row::getValue, Double.MIN_VALUE)
                .returns(Tuple::getInteger, Row::getInteger, 0)
                .returns(Tuple::getLong, Row::getLong, 0L)
                .returns(Tuple::getFloat, Row::getFloat, 0f)
                .returns(Tuple::getDouble, Row::getDouble, Double.MIN_VALUE)
                .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("" + Double.MIN_VALUE))
                .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("" + Double.MIN_VALUE))
                .forRow(row);
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Date\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            LocalDate ld = LocalDate.parse("2017-05-30");
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Time\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            LocalTime lt = LocalTime.parse("17:55:04.905120");
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"TimeTz\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            OffsetTime ot = OffsetTime.parse("17:55:04.905120+03:07");
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(3), ctx.asyncAssertSuccess(result -> {
            LocalDateTime ldt = LocalDateTime.parse("1800-01-01T23:57:53.237666");
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
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
            ColumnChecker.checkColumn(0, "uuid")
              .returns(Tuple::getValue, Row::getValue, uuid)
              .returns(Tuple::getUUID, Row::getUUID, uuid)
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "uuid")
              .returns(Tuple::getValue, Row::getValue, uuid)
              .returns(Tuple::getUUID, Row::getUUID, uuid)
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "Name")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "Name")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "SingleChar")
              .returns(Tuple::getValue, Row::getValue, singleChar)
              .returns(Tuple::getString, Row::getString, singleChar)
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "SingleChar")
              .returns(Tuple::getValue, Row::getValue, singleChar)
              .returns(Tuple::getString, Row::getString, singleChar)
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "FixedChar")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "FixedChar")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "Text")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "Text")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "VarCharacter")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
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
            ColumnChecker.checkColumn(0, "VarCharacter")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeLargeVarchar(TestContext ctx) {
    int len = 2048;
    StringBuilder builder = new StringBuilder();
    for (int i = 0;i < len;i++) {
      builder.append((char)('A' + (i % 26)));
    }
    String value = builder.toString();
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1::VARCHAR(" + len + ")",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.of(value), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(value, result.iterator().next().getString(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testBytea(TestContext ctx) {
    Random r = new Random();
    int len = 2048;
    byte[] bytes = new byte[len];
    r.nextBytes(bytes);
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1::BYTEA \"Bytea\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.of(Buffer.buffer(bytes)), ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Bytea")
              .returns(Tuple::getValue, Row::getValue, Buffer.buffer(bytes))
              .returns(Tuple::getBuffer, Row::getBuffer, Buffer.buffer(bytes))
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testJsonObject(TestContext ctx) {
    Async async = ctx.async();
    String json = "{\"str\":\"blah\", \"int\" : 1, \"float\":3.5, \"object\": {}, \"array\" : []}";
    JsonObject jsonObject = new JsonObject(json);
    Tuple tuple = Tuple.of(jsonObject);
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT ($1::JSON) \"JsonObject\"", tuple, ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(jsonObject, row.getValue(0));
        ctx.assertEquals(jsonObject, row.getJsonObject(0));
        ctx.assertEquals(jsonObject, row.getValue("JsonObject"));
        ctx.assertEquals(jsonObject, row.getJsonObject("JsonObject"));
        ctx.assertNull(row.getBoolean(0));
        ctx.assertNull(row.getBoolean("JsonObject"));
        ctx.assertNull(row.getLong(0));
        ctx.assertNull(row.getLong("JsonObject"));
        ctx.assertNull(row.getInteger(0));
        ctx.assertNull(row.getInteger("JsonObject"));
        ctx.assertNull(row.getFloat(0));
        ctx.assertNull(row.getFloat("JsonObject"));
        ctx.assertNull(row.getDouble(0));
        ctx.assertNull(row.getDouble("JsonObject"));
        ctx.assertNull(row.getCharacter(0));
        ctx.assertNull(row.getCharacter("JsonObject"));
        ctx.assertNull(row.getString(0));
        ctx.assertNull(row.getString("JsonObject"));
        ctx.assertNull(row.getJsonArray(0));
        ctx.assertNull(row.getJsonArray("JsonObject"));
        ctx.assertNull(row.getBuffer(0));
        ctx.assertNull(row.getBuffer("JsonObject"));
        ctx.assertNull(row.getTemporal(0));
        ctx.assertNull(row.getTemporal("JsonObject"));
        ctx.assertNull(row.getLocalDate(0));
        ctx.assertNull(row.getLocalDate("JsonObject"));
        ctx.assertNull(row.getLocalTime(0));
        ctx.assertNull(row.getLocalTime("JsonObject"));
        ctx.assertNull(row.getOffsetTime(0));
        ctx.assertNull(row.getOffsetTime("JsonObject"));
        ctx.assertNull(row.getLocalDateTime(0));
        ctx.assertNull(row.getLocalDateTime("JsonObject"));
        ctx.assertNull(row.getOffsetDateTime(0));
        ctx.assertNull(row.getOffsetDateTime("JsonObject"));
        ctx.assertNull(row.getUUID(0));
        ctx.assertNull(row.getUUID("JsonObject"));
        async.complete();
      }));
    }));
  }

  @Test
  public void testJsonArray(TestContext ctx) {
    Async async = ctx.async();
    String json = "[{\"x\":1},{\"x\":2},{\"x\":3},{\"x\":4},{\"x\":5}]";
    JsonArray jsonArray = new JsonArray(json);
    Tuple tuple = Tuple.of(jsonArray);
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT ($1::JSON) \"Array\"", tuple, ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(jsonArray, row.getValue(0));
        ctx.assertEquals(jsonArray, row.getJsonArray(0));
        ctx.assertEquals(jsonArray, row.getValue("Array"));
        ctx.assertEquals(jsonArray, row.getJsonArray("Array"));
        ctx.assertNull(row.getBoolean(0));
        ctx.assertNull(row.getBoolean(0));
        ctx.assertNull(row.getBoolean("Array"));
        ctx.assertNull(row.getLong(0));
        ctx.assertNull(row.getLong("Array"));
        ctx.assertNull(row.getInteger(0));
        ctx.assertNull(row.getInteger("Array"));
        ctx.assertNull(row.getFloat(0));
        ctx.assertNull(row.getFloat("Array"));
        ctx.assertNull(row.getDouble(0));
        ctx.assertNull(row.getDouble("Array"));
        ctx.assertNull(row.getCharacter(0));
        ctx.assertNull(row.getCharacter("Array"));
        ctx.assertNull(row.getString(0));
        ctx.assertNull(row.getString("Array"));
        ctx.assertNull(row.getJsonObject(0));
        ctx.assertNull(row.getJsonObject("Array"));
        ctx.assertNull(row.getBuffer(0));
        ctx.assertNull(row.getBuffer("Array"));
        ctx.assertNull(row.getTemporal(0));
        ctx.assertNull(row.getTemporal("Array"));
        ctx.assertNull(row.getLocalDate(0));
        ctx.assertNull(row.getLocalDate("Array"));
        ctx.assertNull(row.getLocalTime(0));
        ctx.assertNull(row.getLocalTime("Array"));
        ctx.assertNull(row.getOffsetTime(0));
        ctx.assertNull(row.getOffsetTime("Array"));
        ctx.assertNull(row.getLocalDateTime(0));
        ctx.assertNull(row.getLocalDateTime("Array"));
        ctx.assertNull(row.getOffsetDateTime(0));
        ctx.assertNull(row.getOffsetDateTime("Array"));
        ctx.assertNull(row.getUUID(0));
        ctx.assertNull(row.getUUID("Array"));
        async.complete();
      }));
    }));
  }

//  TODO: Need help here figuring out bind parameters for JSON scalars
//  @Test
//  public void testJsonScalar(TestContext ctx) {
//    Async async = ctx.async();
//    Tuple tuple = Tuple.of("true" , "false", "null", "7.502", "8", "Really Awesome!");
//    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
//      conn.preparedQuery("SELECT ($1::json) \"TrueValue\", ($2::json) \"FalseValue\", ($3::json) \"NullValue\", ($4::json) \"Number1\", ($5::json) \"Number2\", ($6::json) \"Text\"", tuple, ctx.asyncAssertSuccess(result -> {
//        ctx.assertEquals(1, result.size());
//        Row row = result.iterator().next();
//        ctx.assertEquals(true, row.getBoolean(0));
//        ctx.assertEquals(true, row.getValue(0));
//        ctx.assertEquals(true, row.getBoolean("TrueValue"));
//        ctx.assertEquals(true, row.getValue("TrueValue"));
//        ctx.assertNull(row.getLong(0));
//        ctx.assertNull(row.getLong("TrueValue"));
//        ctx.assertNull(row.getInteger(0));
//        ctx.assertNull(row.getInteger("TrueValue"));
//        ctx.assertNull(row.getFloat(0));
//        ctx.assertNull(row.getFloat("TrueValue"));
//        ctx.assertNull(row.getDouble(0));
//        ctx.assertNull(row.getDouble("TrueValue"));
//        ctx.assertNull(row.getCharacter(0));
//        ctx.assertNull(row.getCharacter("TrueValue"));
//        ctx.assertNull(row.getString(0));
//        ctx.assertNull(row.getString("TrueValue"));
//        ctx.assertNull(row.getJsonObject(0));
//        ctx.assertNull(row.getJsonObject("TrueValue"));
//        ctx.assertNull(row.getJsonArray(0));
//        ctx.assertNull(row.getJsonArray("TrueValue"));
//        ctx.assertNull(row.getBuffer(0));
//        ctx.assertNull(row.getBuffer("TrueValue"));
//        ctx.assertNull(row.getTemporal(0));
//        ctx.assertNull(row.getTemporal("TrueValue"));
//        ctx.assertNull(row.getLocalDate(0));
//        ctx.assertNull(row.getLocalDate("TrueValue"));
//        ctx.assertNull(row.getLocalTime(0));
//        ctx.assertNull(row.getLocalTime("TrueValue"));
//        ctx.assertNull(row.getOffsetTime(0));
//        ctx.assertNull(row.getOffsetTime("TrueValue"));
//        ctx.assertNull(row.getLocalDateTime(0));
//        ctx.assertNull(row.getLocalDateTime("TrueValue"));
//        ctx.assertNull(row.getOffsetDateTime(0));
//        ctx.assertNull(row.getOffsetDateTime("TrueValue"));
//        ctx.assertNull(row.getUUID(0));
//        ctx.assertNull(row.getUUID("TrueValue"));
//
//        ctx.assertEquals(false, row.getBoolean(1));
//        ctx.assertEquals(false, row.getValue(1));
//        ctx.assertEquals(false, row.getBoolean("FalseValue"));
//        ctx.assertEquals(false, row.getValue("FalseValue"));
//        ctx.assertNull(row.getLong(1));
//        ctx.assertNull(row.getLong("FalseValue"));
//        ctx.assertNull(row.getInteger(1));
//        ctx.assertNull(row.getInteger("FalseValue"));
//        ctx.assertNull(row.getFloat(1));
//        ctx.assertNull(row.getFloat("FalseValue"));
//        ctx.assertNull(row.getDouble(1));
//        ctx.assertNull(row.getDouble("FalseValue"));
//        ctx.assertNull(row.getCharacter(1));
//        ctx.assertNull(row.getCharacter("FalseValue"));
//        ctx.assertNull(row.getString(1));
//        ctx.assertNull(row.getString("FalseValue"));
//        ctx.assertNull(row.getJsonObject(1));
//        ctx.assertNull(row.getJsonObject("FalseValue"));
//        ctx.assertNull(row.getJsonArray(1));
//        ctx.assertNull(row.getJsonArray("FalseValue"));
//        ctx.assertNull(row.getBuffer(1));
//        ctx.assertNull(row.getBuffer("FalseValue"));
//        ctx.assertNull(row.getTemporal(1));
//        ctx.assertNull(row.getTemporal("FalseValue"));
//        ctx.assertNull(row.getLocalDate(1));
//        ctx.assertNull(row.getLocalDate("FalseValue"));
//        ctx.assertNull(row.getLocalTime(1));
//        ctx.assertNull(row.getLocalTime("FalseValue"));
//        ctx.assertNull(row.getOffsetTime(1));
//        ctx.assertNull(row.getOffsetTime("FalseValue"));
//        ctx.assertNull(row.getLocalDateTime(1));
//        ctx.assertNull(row.getLocalDateTime("FalseValue"));
//        ctx.assertNull(row.getOffsetDateTime(1));
//        ctx.assertNull(row.getOffsetDateTime("FalseValue"));
//        ctx.assertNull(row.getUUID(1));
//        ctx.assertNull(row.getUUID("FalseValue"));
//
//        ctx.assertNull(row.getValue(2));
//        ctx.assertNull(row.getValue("NullValue"));
//        ctx.assertNull(row.getBoolean(2));
//        ctx.assertNull(row.getBoolean("NullValue"));
//        ctx.assertNull(row.getLong(2));
//        ctx.assertNull(row.getLong("NullValue"));
//        ctx.assertNull(row.getInteger(2));
//        ctx.assertNull(row.getInteger("NullValue"));
//        ctx.assertNull(row.getFloat(2));
//        ctx.assertNull(row.getFloat("NullValue"));
//        ctx.assertNull(row.getDouble(2));
//        ctx.assertNull(row.getDouble("NullValue"));
//        ctx.assertNull(row.getCharacter(2));
//        ctx.assertNull(row.getCharacter("NullValue"));
//        ctx.assertNull(row.getString(2));
//        ctx.assertNull(row.getString("NullValue"));
//        ctx.assertNull(row.getJsonObject(2));
//        ctx.assertNull(row.getJsonObject("NullValue"));
//        ctx.assertNull(row.getJsonArray(2));
//        ctx.assertNull(row.getJsonArray("NullValue"));
//        ctx.assertNull(row.getBuffer(2));
//        ctx.assertNull(row.getBuffer("NullValue"));
//        ctx.assertNull(row.getTemporal(2));
//        ctx.assertNull(row.getTemporal("NullValue"));
//        ctx.assertNull(row.getLocalDate(2));
//        ctx.assertNull(row.getLocalDate("NullValue"));
//        ctx.assertNull(row.getLocalTime(2));
//        ctx.assertNull(row.getLocalTime("NullValue"));
//        ctx.assertNull(row.getOffsetTime(2));
//        ctx.assertNull(row.getOffsetTime("NullValue"));
//        ctx.assertNull(row.getLocalDateTime(2));
//        ctx.assertNull(row.getLocalDateTime("NullValue"));
//        ctx.assertNull(row.getOffsetDateTime(2));
//        ctx.assertNull(row.getOffsetDateTime("NullValue"));
//        ctx.assertNull(row.getUUID(2));
//        ctx.assertNull(row.getUUID("NullValue"));
//
//        // ctx.assertEquals(7.502f, row.getFloat(3));
//        ctx.assertEquals(7.502d, row.getDouble(3));
//        ctx.assertEquals(7.502d, row.getValue(3));
//        ctx.assertEquals(7.502d, row.getDouble("Number1"));
//        ctx.assertEquals(7.502d, row.getValue("Number1"));
//        ctx.assertNull(row.getBoolean(3));
//        ctx.assertNull(row.getBoolean("Number1"));
//        ctx.assertEquals(7L, row.getLong(3));
//        ctx.assertEquals(7L, row.getLong("Number1"));
//        ctx.assertEquals(7, row.getInteger(3));
//        ctx.assertEquals(7, row.getInteger("Number1"));
//        ctx.assertEquals(7.502f, row.getFloat(3));
//        ctx.assertEquals(7.502f, row.getFloat("Number1"));
//        ctx.assertNull(row.getCharacter(3));
//        ctx.assertNull(row.getCharacter("Number1"));
//        ctx.assertNull(row.getString(3));
//        ctx.assertNull(row.getString("Number1"));
//        ctx.assertNull(row.getJsonObject(3));
//        ctx.assertNull(row.getJsonObject("Number1"));
//        ctx.assertNull(row.getJsonArray(3));
//        ctx.assertNull(row.getJsonArray("Number1"));
//        ctx.assertNull(row.getBuffer(3));
//        ctx.assertNull(row.getBuffer("Number1"));
//        ctx.assertNull(row.getTemporal(3));
//        ctx.assertNull(row.getTemporal("Number1"));
//        ctx.assertNull(row.getLocalDate(3));
//        ctx.assertNull(row.getLocalDate("Number1"));
//        ctx.assertNull(row.getLocalTime(3));
//        ctx.assertNull(row.getLocalTime("Number1"));
//        ctx.assertNull(row.getOffsetTime(3));
//        ctx.assertNull(row.getOffsetTime("Number1"));
//        ctx.assertNull(row.getLocalDateTime(3));
//        ctx.assertNull(row.getLocalDateTime("Number1"));
//        ctx.assertNull(row.getOffsetDateTime(3));
//        ctx.assertNull(row.getOffsetDateTime("Number1"));
//        ctx.assertNull(row.getUUID(3));
//        ctx.assertNull(row.getUUID("Number1"));
//
//        ctx.assertEquals(8, row.getInteger(4));
//        ctx.assertEquals(8, row.getValue(4));
//        ctx.assertEquals(8, row.getInteger("Number2"));
//        ctx.assertEquals(8, row.getValue("Number2"));
//        ctx.assertNull(row.getBoolean(4));
//        ctx.assertNull(row.getBoolean("Number2"));
//        ctx.assertEquals(8L, row.getLong(4));
//        ctx.assertEquals(8L, row.getLong("Number2"));
//        ctx.assertEquals(8f, row.getFloat(4));
//        ctx.assertEquals(8f, row.getFloat("Number2"));
//        ctx.assertEquals(8d, row.getDouble(4));
//        ctx.assertEquals(8d, row.getDouble("Number2"));
//        ctx.assertNull(row.getCharacter(4));
//        ctx.assertNull(row.getCharacter("Number2"));
//        ctx.assertNull(row.getString(4));
//        ctx.assertNull(row.getString("Number2"));
//        ctx.assertNull(row.getJsonObject(4));
//        ctx.assertNull(row.getJsonObject("Number2"));
//        ctx.assertNull(row.getJsonArray(4));
//        ctx.assertNull(row.getJsonArray("Number2"));
//        ctx.assertNull(row.getBuffer(4));
//        ctx.assertNull(row.getBuffer("Number2"));
//        ctx.assertNull(row.getTemporal(4));
//        ctx.assertNull(row.getTemporal("Number2"));
//        ctx.assertNull(row.getLocalDate(4));
//        ctx.assertNull(row.getLocalDate("Number2"));
//        ctx.assertNull(row.getLocalTime(4));
//        ctx.assertNull(row.getLocalTime("Number2"));
//        ctx.assertNull(row.getOffsetTime(4));
//        ctx.assertNull(row.getOffsetTime("Number2"));
//        ctx.assertNull(row.getLocalDateTime(4));
//        ctx.assertNull(row.getLocalDateTime("Number2"));
//        ctx.assertNull(row.getOffsetDateTime(4));
//        ctx.assertNull(row.getOffsetDateTime("Number2"));
//        ctx.assertNull(row.getUUID(4));
//        ctx.assertNull(row.getUUID("Number2"));
//
//        // ctx.assertEquals(8L, row.getLong(4));
//        ctx.assertEquals(" Really Awesome! ", row.getString(5));
//        ctx.assertEquals(" Really Awesome! ", row.getValue(5));
//        ctx.assertEquals(" Really Awesome! ", row.getString("Text"));
//        ctx.assertEquals(" Really Awesome! ", row.getValue("Text"));
//        ctx.assertNull(row.getBoolean(5));
//        ctx.assertNull(row.getBoolean("Text"));
//        ctx.assertNull(row.getLong(5));
//        ctx.assertNull(row.getLong("Text"));
//        ctx.assertNull(row.getInteger(5));
//        ctx.assertNull(row.getInteger("Text"));
//        ctx.assertNull(row.getFloat(5));
//        ctx.assertNull(row.getFloat("Text"));
//        ctx.assertNull(row.getDouble(5));
//        ctx.assertNull(row.getDouble("Text"));
//        ctx.assertNull(row.getCharacter(5));
//        ctx.assertNull(row.getCharacter("Text"));
//        ctx.assertNull(row.getJsonObject(5));
//        ctx.assertNull(row.getJsonObject("Text"));
//        ctx.assertNull(row.getJsonArray(5));
//        ctx.assertNull(row.getJsonArray("Text"));
//        ctx.assertNull(row.getBuffer(5));
//        ctx.assertNull(row.getBuffer("Text"));
//        ctx.assertNull(row.getTemporal(5));
//        ctx.assertNull(row.getTemporal("Text"));
//        ctx.assertNull(row.getLocalDate(5));
//        ctx.assertNull(row.getLocalDate("Text"));
//        ctx.assertNull(row.getLocalTime(5));
//        ctx.assertNull(row.getLocalTime("Text"));
//        ctx.assertNull(row.getOffsetTime(5));
//        ctx.assertNull(row.getOffsetTime("Text"));
//        ctx.assertNull(row.getLocalDateTime(5));
//        ctx.assertNull(row.getLocalDateTime("Text"));
//        ctx.assertNull(row.getOffsetDateTime(5));
//        ctx.assertNull(row.getOffsetDateTime("Text"));
//        ctx.assertNull(row.getUUID(5));
//        ctx.assertNull(row.getUUID("Text"));
//        async.complete();
//      }));
//    }));
//  }
}

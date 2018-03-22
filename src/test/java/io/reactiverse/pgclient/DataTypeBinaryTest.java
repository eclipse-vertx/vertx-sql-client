package io.reactiverse.pgclient;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
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
              .returns(Tuple::getValue, Row::getValue, (short) 32767)
              .returns(Tuple::getShort, Row::getShort, Short.MAX_VALUE)
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
              .returns(Tuple::getValue, Row::getValue, (short) -32768)
              .returns(Tuple::getShort, Row::getShort, Short.MIN_VALUE)
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
              .returns(Tuple::getShort, Row::getShort, (short) -1)
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
                .returns(Tuple::getShort, Row::getShort, (short) 0)
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
              .returns(Tuple::getShort, Row::getShort, (short) -1)
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
                .returns(Tuple::getShort, Row::getShort, (short) 0)
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
              .returns(Tuple::getShort, Row::getShort, (short) -1)
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
                .returns(Tuple::getShort, Row::getShort, (short) 0)
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
              .returns(Tuple::getShort, Row::getShort, (short) -1)
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
                .returns(Tuple::getShort, Row::getShort, (short) 0)
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
  public void testDecodeJson(TestContext ctx) {
    testDecodeJson(ctx, "JsonDataType");
  }

  @Test
  public void testDecodeJsonb(TestContext ctx) {
    testDecodeJson(ctx, "JsonbDataType");
  }

  private void testDecodeJson(TestContext ctx, String tableName) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"JsonObject\", \"JsonArray\", \"Number\", \"String\", \"BooleanTrue\", \"BooleanFalse\", \"Null\" FROM \"" + tableName + "\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []}");
            JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
            ColumnChecker.checkColumn(0, "JsonObject")
              .returns(Tuple::getValue, Row::getValue, Json.create(object))
              .returns(Tuple::getJsonObject, Row::getJsonObject, object)
              .forRow(row);
            ColumnChecker.checkColumn(1, "JsonArray")
              .returns(Tuple::getValue, Row::getValue, Json.create(array))
              .returns(Tuple::getJsonArray, Row::getJsonArray, array)
              .forRow(row);
            ColumnChecker.checkColumn(2, "Number")
              .returns(Tuple::getValue, Row::getValue, Json.create(4))
              .returns(Tuple::getShort, Row::getShort, (short) 4)
              .returns(Tuple::getInteger, Row::getInteger, 4)
              .returns(Tuple::getLong, Row::getLong, 4L)
              .returns(Tuple::getFloat, Row::getFloat, 4f)
              .returns(Tuple::getDouble, Row::getDouble, 4d)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("4"))
              .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("4"))
              .forRow(row);
            ColumnChecker.checkColumn(3, "String")
              .returns(Tuple::getValue, Row::getValue, Json.create("Hello World"))
              .returns(Tuple::getString, Row::getString, "Hello World")
              .forRow(row);
            ColumnChecker.checkColumn(4, "BooleanTrue")
              .returns(Tuple::getValue, Row::getValue, Json.create(true))
              .returns(Tuple::getBoolean, Row::getBoolean, true)
              .forRow(row);
            ColumnChecker.checkColumn(5, "BooleanFalse")
              .returns(Tuple::getValue, Row::getValue, Json.create(false))
              .returns(Tuple::getBoolean, Row::getBoolean, false)
              .forRow(row);
            ColumnChecker.checkColumn(6, "Null")
              .returns(Tuple::getValue, Row::getValue, Json.create(null))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeJson(TestContext ctx) {
    testEncodeJson(ctx, "JsonDataType");
  }

  @Test
  public void testEncodeJsonb(TestContext ctx) {
    testEncodeJson(ctx, "JsonbDataType");
  }

  private void testEncodeJson(TestContext ctx, String tableName) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"" + tableName + "\" SET " +
          "\"JsonObject\" = $1, " +
          "\"JsonArray\" = $2, " +
          "\"Number\" = $3, " +
          "\"String\" = $4, " +
          "\"BooleanTrue\" = $5, " +
          "\"BooleanFalse\" = $6, " +
          "\"Null\" = $7 " +
          "WHERE \"id\" = $8 RETURNING \"JsonObject\", \"JsonArray\", \"Number\", \"String\", \"BooleanTrue\", \"BooleanFalse\", \"Null\"",
        ctx.asyncAssertSuccess(p -> {
          JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []}");
          JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
          p.execute(Tuple.tuple()
            .addJsonObject(object)
            .addJsonArray(array)
            .addInteger(4)
            .addString("Hello World")
            .addBoolean(true)
            .addBoolean(false)
            .addJson(Json.create(null))
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "JsonObject")
              .returns(Tuple::getValue, Row::getValue, Json.create(object))
              .returns(Tuple::getJsonObject, Row::getJsonObject, object)
              .forRow(row);
            ColumnChecker.checkColumn(1, "JsonArray")
              .returns(Tuple::getValue, Row::getValue, Json.create(array))
              .returns(Tuple::getJsonArray, Row::getJsonArray, array)
              .forRow(row);
            ColumnChecker.checkColumn(2, "Number")
              .returns(Tuple::getValue, Row::getValue, Json.create(4))
              .returns(Tuple::getShort, Row::getShort, (short) 4)
              .returns(Tuple::getInteger, Row::getInteger, 4)
              .returns(Tuple::getLong, Row::getLong, 4L)
              .returns(Tuple::getFloat, Row::getFloat, 4f)
              .returns(Tuple::getDouble, Row::getDouble, 4d)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("4"))
              .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("4"))
              .forRow(row);
            ColumnChecker.checkColumn(3, "String")
              .returns(Tuple::getValue, Row::getValue, Json.create("Hello World"))
              .returns(Tuple::getString, Row::getString, "Hello World")
              .forRow(row);
            ColumnChecker.checkColumn(4, "BooleanTrue")
              .returns(Tuple::getValue, Row::getValue, Json.create(true))
              .returns(Tuple::getBoolean, Row::getBoolean, true)
              .forRow(row);
            ColumnChecker.checkColumn(5, "BooleanFalse")
              .returns(Tuple::getValue, Row::getValue, Json.create(false))
              .returns(Tuple::getBoolean, Row::getBoolean, false)
              .forRow(row);
            ColumnChecker.checkColumn(6, "Null")
              .returns(Tuple::getValue, Row::getValue, Json.create(null))
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
//Had to use the old test method cause vertx TestContext doesnt seem to support to compare arrays
  @Test
  public void testDecodeBooleanArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Boolean\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Boolean"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Boolean"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Boolean"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Boolean"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Boolean"));
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
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("Boolean"));
            ctx.assertEquals(Boolean.TRUE, row.getBooleanArray(0)[0]);
            ctx.assertEquals(Boolean.TRUE, row.getBooleanArray("Boolean")[0]);
            ctx.assertEquals(Boolean.TRUE, ((Boolean[]) row.getValue(0))[0]);
            ctx.assertEquals(Boolean.TRUE, ((Boolean[]) row.getValue("Boolean"))[0]);
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("Boolean"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("Boolean"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("Boolean"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("Boolean"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("Boolean"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("Boolean"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("Boolean"));
            ctx.assertNull(row.getOffsetTimeArray(0));
            ctx.assertNull(row.getOffsetTimeArray("Boolean"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("Boolean"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("Boolean"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("Boolean"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("Boolean"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeBooleanArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Boolean\" = $1  WHERE \"id\" = $2 RETURNING \"Boolean\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addBooleanArray(new Boolean[]{Boolean.FALSE})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("Boolean"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("Boolean"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("Boolean"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("Boolean"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("Boolean"));
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
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("Boolean"));
              ctx.assertEquals(Boolean.FALSE, row.getBooleanArray(0)[0]);
              ctx.assertEquals(Boolean.FALSE, row.getBooleanArray("Boolean")[0]);
              ctx.assertEquals(Boolean.FALSE, ((Boolean[]) row.getValue(0))[0]);
              ctx.assertEquals(Boolean.FALSE, ((Boolean[]) row.getValue("Boolean"))[0]);
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("Boolean"));
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("Boolean"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("Boolean"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("Boolean"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("Boolean"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("Boolean"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("Boolean"));
              ctx.assertNull(row.getOffsetTimeArray(0));
              ctx.assertNull(row.getOffsetTimeArray("Boolean"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("Boolean"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("Boolean"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("Boolean"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("Boolean"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeShortArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Short\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Short"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Short"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Short"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Short"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Short"));
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
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("Short"));
            ctx.assertEquals((short) 1, row.getShortArray(0)[0]);
            ctx.assertEquals((short) 1, row.getShortArray("Short")[0]);
            ctx.assertEquals((short) 1, ((Short[]) row.getValue(0))[0]);
            ctx.assertEquals((short) 1, ((Short[]) row.getValue("Short"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("Short"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("Short"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("Short"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("Short"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("Short"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("Short"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("Short"));
            ctx.assertNull(row.getOffsetTimeArray(0));
            ctx.assertNull(row.getOffsetTimeArray("Short"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("Short"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("Short"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("Short"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("Short"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeShortArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Short\" = $1  WHERE \"id\" = $2 RETURNING \"Short\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addShortArray(new Short[]{2})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("Short"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("Short"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("Short"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("Short"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("Short"));
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
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("Short"));
              ctx.assertEquals((short) 2, row.getShortArray(0)[0]);
              ctx.assertEquals((short) 2, row.getShortArray("Short")[0]);
              ctx.assertEquals((short) 2, ((Short[]) row.getValue(0))[0]);
              ctx.assertEquals((short) 2, ((Short[]) row.getValue("Short"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("Short"));
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("Short"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("Short"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("Short"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("Short"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("Short"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("Short"));
              ctx.assertNull(row.getOffsetTimeArray(0));
              ctx.assertNull(row.getOffsetTimeArray("Short"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("Short"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("Short"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("Short"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("Short"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeIntArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Integer\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Integer"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Integer"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Integer"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Integer"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Integer"));
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
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("Integer"));
            ctx.assertEquals(2, row.getIntArray(0)[0]);
            ctx.assertEquals(2, row.getIntArray("Integer")[0]);
            ctx.assertEquals(2, ((Integer[]) row.getValue(0))[0]);
            ctx.assertEquals(2, ((Integer[]) row.getValue("Integer"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("Integer"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("Integer"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("Integer"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("Integer"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("Integer"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("Integer"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("Integer"));
            ctx.assertNull(row.getOffsetTimeArray(0));
            ctx.assertNull(row.getOffsetTimeArray("Integer"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("Integer"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("Integer"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("Integer"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("Integer"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeIntArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Integer\" = $1  WHERE \"id\" = $2 RETURNING \"Integer\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addIntArray(new Integer[]{3})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("Integer"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("Integer"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("Integer"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("Integer"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("Integer"));
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
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("Integer"));
              ctx.assertEquals(3, row.getIntArray(0)[0]);
              ctx.assertEquals(3, row.getIntArray("Integer")[0]);
              ctx.assertEquals(3, ((Integer[]) row.getValue(0))[0]);
              ctx.assertEquals(3, ((Integer[]) row.getValue("Integer"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("Integer"));
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("Integer"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("Integer"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("Integer"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("Integer"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("Integer"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("Integer"));
              ctx.assertNull(row.getOffsetTimeArray(0));
              ctx.assertNull(row.getOffsetTimeArray("Integer"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("Integer"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("Integer"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("Integer"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("Integer"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeLongArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Long\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Long"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Long"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Long"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Long"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Long"));
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
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("Long"));
            ctx.assertEquals((long) 3, row.getLongArray(0)[0]);
            ctx.assertEquals((long) 3, row.getLongArray("Long")[0]);
            ctx.assertEquals((long) 3, ((Long[]) row.getValue(0))[0]);
            ctx.assertEquals((long) 3, ((Long[]) row.getValue("Long"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("Long"));
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("Long"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("Long"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("Long"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("Long"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("Long"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("Long"));
            ctx.assertNull(row.getOffsetTimeArray(0));
            ctx.assertNull(row.getOffsetTimeArray("Long"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("Long"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("Long"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("Long"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("Long"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeLongArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Long\" = $1  WHERE \"id\" = $2 RETURNING \"Long\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addLongArray(new Long[]{(long) 4})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("Long"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("Long"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("Long"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("Long"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("Long"));
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
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("Long"));
              ctx.assertEquals((long) 4, row.getLongArray(0)[0]);
              ctx.assertEquals((long) 4, row.getLongArray("Long")[0]);
              ctx.assertEquals((long) 4, ((Long[]) row.getValue(0))[0]);
              ctx.assertEquals((long) 4, ((Long[]) row.getValue("Long"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("Long"));
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("Long"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("Long"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("Long"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("Long"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("Long"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("Long"));
              ctx.assertNull(row.getOffsetTimeArray(0));
              ctx.assertNull(row.getOffsetTimeArray("Long"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("Long"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("Long"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("Long"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("Long"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeFloatArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Float\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Float"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Float"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Float"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Float"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Float"));
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
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("Float"));
            ctx.assertEquals((float) 4.1, row.getFloatArray(0)[0]);
            ctx.assertEquals((float) 4.1, row.getFloatArray("Float")[0]);
            ctx.assertEquals((float) 4.1, ((Float[]) row.getValue(0))[0]);
            ctx.assertEquals((float) 4.1, ((Float[]) row.getValue("Float"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("Float"));
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("Float"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("Float"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("Float"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("Float"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("Float"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("Float"));
            ctx.assertNull(row.getOffsetTimeArray(0));
            ctx.assertNull(row.getOffsetTimeArray("Float"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("Float"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("Float"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("Float"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("Float"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeFloatArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Float\" = $1  WHERE \"id\" = $2 RETURNING \"Float\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addFloatArray(new Float[]{(float) 5.2})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("Float"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("Float"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("Float"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("Float"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("Float"));
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
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("Float"));
              ctx.assertEquals((float) 5.2, row.getFloatArray(0)[0]);
              ctx.assertEquals((float) 5.2, row.getFloatArray("Float")[0]);
              ctx.assertEquals((float) 5.2, ((Float[]) row.getValue(0))[0]);
              ctx.assertEquals((float) 5.2, ((Float[]) row.getValue("Float"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("Float"));
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("Float"));
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("Float"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("Float"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("Float"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("Float"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("Float"));
              ctx.assertNull(row.getOffsetTimeArray(0));
              ctx.assertNull(row.getOffsetTimeArray("Float"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("Float"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("Float"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("Float"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("Float"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeDoubleArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Double\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Double"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Double"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Double"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Double"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Double"));
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
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("Double"));
            ctx.assertEquals(5.2, row.getDoubleArray(0)[0]);
            ctx.assertEquals(5.2, row.getDoubleArray("Double")[0]);
            ctx.assertEquals(5.2, ((Double[]) row.getValue(0))[0]);
            ctx.assertEquals(5.2, ((Double[]) row.getValue("Double"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("Double"));
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("Double"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("Double"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("Double"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("Double"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("Double"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("Double"));
            ctx.assertNull(row.getOffsetTimeArray(0));
            ctx.assertNull(row.getOffsetTimeArray("Double"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("Double"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("Double"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("Double"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("Double"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeDoubleArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Double\" = $1  WHERE \"id\" = $2 RETURNING \"Double\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addDoubleArray(new Double[]{6.3})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("Double"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("Double"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("Double"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("Double"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("Double"));
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
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("Double"));
              ctx.assertEquals(6.3, row.getDoubleArray(0)[0]);
              ctx.assertEquals(6.3, row.getDoubleArray("Double")[0]);
              ctx.assertEquals(6.3, ((Double[]) row.getValue(0))[0]);
              ctx.assertEquals(6.3, ((Double[]) row.getValue("Double"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("Double"));
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("Double"));
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("Double"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("Double"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("Double"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("Double"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("Double"));
              ctx.assertNull(row.getOffsetTimeArray(0));
              ctx.assertNull(row.getOffsetTimeArray("Double"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("Double"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("Double"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("Double"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("Double"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeStringArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Text\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("Text"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("Text"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("Text"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("Text"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("Text"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("Text"));
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
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("Text"));
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("Text"));
            ctx.assertEquals("Knock, knock.Who’s there?very long pause….Java.", row.getStringArray(0)[0]);
            ctx.assertEquals("Knock, knock.Who’s there?very long pause….Java.", row.getStringArray("Text")[0]);
            ctx.assertEquals("Knock, knock.Who’s there?very long pause….Java.", ((String[]) row.getValue(0))[0]);
            ctx.assertEquals("Knock, knock.Who’s there?very long pause….Java.", ((String[]) row.getValue("Text"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("Text"));
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("Text"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("Text"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("Text"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("Text"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("Text"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("Text"));
            ctx.assertNull(row.getOffsetTimeArray(0));
            ctx.assertNull(row.getOffsetTimeArray("Text"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("Text"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("Text"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("Text"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("Text"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeStringArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Text\" = $1  WHERE \"id\" = $2 RETURNING \"Text\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addStringArray(new String[]{"Knock, knock.Who’s there?"})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("Text"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("Text"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("Text"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("Text"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("Text"));
              ctx.assertNull(row.getString(0));
              ctx.assertNull(row.getString("Text"));
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
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("Text"));
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("Text"));
              ctx.assertEquals("Knock, knock.Who’s there?", row.getStringArray(0)[0]);
              ctx.assertEquals("Knock, knock.Who’s there?", row.getStringArray("Text")[0]);
              ctx.assertEquals("Knock, knock.Who’s there?", ((String[]) row.getValue(0))[0]);
              ctx.assertEquals("Knock, knock.Who’s there?", ((String[]) row.getValue("Text"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("Text"));
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("Text"));
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("Text"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("Text"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("Text"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("Text"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("Text"));
              ctx.assertNull(row.getOffsetTimeArray(0));
              ctx.assertNull(row.getOffsetTimeArray("Text"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("Text"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("Text"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("Text"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("Text"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeLocalDateArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"LocalDate\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("LocalDate"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("LocalDate"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("LocalDate"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("LocalDate"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("LocalDate"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("LocalDate"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("LocalDate"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("LocalDate"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("LocalDate"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("LocalDate"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("LocalDate"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("LocalDate"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("LocalDate"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("LocalDate"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("LocalDate"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("LocalDate"));
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("LocalDate"));
            final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            final LocalDate dt = LocalDate.parse("1998-05-11", dtf);
            ctx.assertEquals(dt, row.getLocalDateArray(0)[0]);
            ctx.assertEquals(dt, row.getLocalDateArray("LocalDate")[0]);
            ctx.assertEquals(dt, ((LocalDate[]) row.getValue(0))[0]);
            ctx.assertEquals(dt, ((LocalDate[]) row.getValue("LocalDate"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("LocalDate"));
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("LocalDate"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("LocalDate"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("LocalDate"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("LocalDate"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("LocalDate"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("LocalDate"));
            ctx.assertNull(row.getOffsetTimeArray(0));
            ctx.assertNull(row.getOffsetTimeArray("LocalDate"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("LocalDate"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("LocalDate"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("LocalDate"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("LocalDate"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeLocalDateArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"LocalDate\" = $1  WHERE \"id\" = $2 RETURNING \"LocalDate\"",
        ctx.asyncAssertSuccess(p -> {
          final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
          final LocalDate dt = LocalDate.parse("1998-05-12", dtf);
          p.execute(Tuple.tuple()
              .addLocalDateArray(new LocalDate[]{dt})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("LocalDate"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("LocalDate"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("LocalDate"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("LocalDate"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("LocalDate"));
              ctx.assertNull(row.getString(0));
              ctx.assertNull(row.getString("LocalDate"));
              ctx.assertNull(row.getJsonObject(0));
              ctx.assertNull(row.getJsonObject("LocalDate"));
              ctx.assertNull(row.getJsonArray(0));
              ctx.assertNull(row.getJsonArray("LocalDate"));
              ctx.assertNull(row.getBuffer(0));
              ctx.assertNull(row.getBuffer("LocalDate"));
              ctx.assertNull(row.getTemporal(0));
              ctx.assertNull(row.getTemporal("LocalDate"));
              ctx.assertNull(row.getLocalDate(0));
              ctx.assertNull(row.getLocalDate("LocalDate"));
              ctx.assertNull(row.getLocalTime(0));
              ctx.assertNull(row.getLocalTime("LocalDate"));
              ctx.assertNull(row.getOffsetTime(0));
              ctx.assertNull(row.getOffsetTime("LocalDate"));
              ctx.assertNull(row.getLocalDateTime(0));
              ctx.assertNull(row.getLocalDateTime("LocalDate"));
              ctx.assertNull(row.getOffsetDateTime(0));
              ctx.assertNull(row.getOffsetDateTime("LocalDate"));
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("LocalDate"));
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("LocalDate"));
              ctx.assertEquals(dt, row.getLocalDateArray(0)[0]);
              ctx.assertEquals(dt, row.getLocalDateArray("LocalDate")[0]);
              ctx.assertEquals(dt, ((LocalDate[]) row.getValue(0))[0]);
              ctx.assertEquals(dt, ((LocalDate[]) row.getValue("LocalDate"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("LocalDate"));
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("LocalDate"));
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("LocalDate"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("LocalDate"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("LocalDate"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("LocalDate"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("LocalDate"));
              ctx.assertNull(row.getOffsetTimeArray(0));
              ctx.assertNull(row.getOffsetTimeArray("LocalDate"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("LocalDate"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("LocalDate"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("LocalDate"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("LocalDate"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeLocalTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"LocalTime\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("LocalTime"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("LocalTime"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("LocalTime"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("LocalTime"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("LocalTime"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("LocalTime"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("LocalTime"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("LocalTime"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("LocalTime"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("LocalTime"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("LocalTime"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("LocalTime"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("LocalTime"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("LocalTime"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("LocalTime"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("LocalTime"));
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("LocalTime"));
            final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSS");
            final LocalTime dt = LocalTime.parse("17:55:04.90512", dtf);
            ctx.assertEquals(dt, row.getLocalTimeArray(0)[0]);
            ctx.assertEquals(dt, row.getLocalTimeArray("LocalTime")[0]);
            ctx.assertEquals(dt, ((LocalTime[]) row.getValue(0))[0]);
            ctx.assertEquals(dt, ((LocalTime[]) row.getValue("LocalTime"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("LocalTime"));
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("LocalTime"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("LocalTime"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("LocalTime"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("LocalTime"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("LocalTime"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("LocalTime"));
            ctx.assertNull(row.getOffsetTimeArray(0));
            ctx.assertNull(row.getOffsetTimeArray("LocalTime"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("LocalTime"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("LocalTime"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("LocalTime"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("LocalTime"));
            async.complete();
          }));
        }));
    }));
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
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("LocalTime"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("LocalTime"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("LocalTime"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("LocalTime"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("LocalTime"));
              ctx.assertNull(row.getString(0));
              ctx.assertNull(row.getString("LocalTime"));
              ctx.assertNull(row.getJsonObject(0));
              ctx.assertNull(row.getJsonObject("LocalTime"));
              ctx.assertNull(row.getJsonArray(0));
              ctx.assertNull(row.getJsonArray("LocalTime"));
              ctx.assertNull(row.getBuffer(0));
              ctx.assertNull(row.getBuffer("LocalTime"));
              ctx.assertNull(row.getTemporal(0));
              ctx.assertNull(row.getTemporal("LocalTime"));
              ctx.assertNull(row.getLocalDate(0));
              ctx.assertNull(row.getLocalDate("LocalTime"));
              ctx.assertNull(row.getLocalTime(0));
              ctx.assertNull(row.getLocalTime("LocalTime"));
              ctx.assertNull(row.getOffsetTime(0));
              ctx.assertNull(row.getOffsetTime("LocalTime"));
              ctx.assertNull(row.getLocalDateTime(0));
              ctx.assertNull(row.getLocalDateTime("LocalTime"));
              ctx.assertNull(row.getOffsetDateTime(0));
              ctx.assertNull(row.getOffsetDateTime("LocalTime"));
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("LocalTime"));
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("LocalTime"));
              ctx.assertEquals(dt, row.getLocalTimeArray(0)[0]);
              ctx.assertEquals(dt, row.getLocalTimeArray("LocalTime")[0]);
              ctx.assertEquals(dt, ((LocalTime[]) row.getValue(0))[0]);
              ctx.assertEquals(dt, ((LocalTime[]) row.getValue("LocalTime"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("LocalTime"));
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("LocalTime"));
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("LocalTime"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("LocalTime"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("LocalTime"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("LocalTime"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("LocalTime"));
              ctx.assertNull(row.getOffsetTimeArray(0));
              ctx.assertNull(row.getOffsetTimeArray("LocalTime"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("LocalTime"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("LocalTime"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("LocalTime"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("LocalTime"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeOffsetTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"OffsetTime\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("OffsetTime"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("OffsetTime"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("OffsetTime"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("OffsetTime"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("OffsetTime"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("OffsetTime"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("OffsetTime"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("OffsetTime"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("OffsetTime"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("OffsetTime"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("OffsetTime"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("OffsetTime"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("OffsetTime"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("OffsetTime"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("OffsetTime"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("OffsetTime"));
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("OffsetTime"));
            final OffsetTime dt = OffsetTime.parse("17:55:04.90512+03:00");
            ctx.assertEquals(dt, row.getOffsetTimeArray(0)[0]);
            ctx.assertEquals(dt, row.getOffsetTimeArray("OffsetTime")[0]);
            ctx.assertEquals(dt, ((OffsetTime[]) row.getValue(0))[0]);
            ctx.assertEquals(dt, ((OffsetTime[]) row.getValue("OffsetTime"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("OffsetTime"));
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("OffsetTime"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("OffsetTime"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("OffsetTime"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("OffsetTime"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("OffsetTime"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("OffsetTime"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("OffsetTime"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("OffsetTime"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("OffsetTime"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("OffsetTime"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("OffsetTime"));
            async.complete();
          }));
        }));
    }));
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
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("OffsetTime"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("OffsetTime"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("OffsetTime"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("OffsetTime"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("OffsetTime"));
              ctx.assertNull(row.getString(0));
              ctx.assertNull(row.getString("OffsetTime"));
              ctx.assertNull(row.getJsonObject(0));
              ctx.assertNull(row.getJsonObject("OffsetTime"));
              ctx.assertNull(row.getJsonArray(0));
              ctx.assertNull(row.getJsonArray("OffsetTime"));
              ctx.assertNull(row.getBuffer(0));
              ctx.assertNull(row.getBuffer("OffsetTime"));
              ctx.assertNull(row.getTemporal(0));
              ctx.assertNull(row.getTemporal("OffsetTime"));
              ctx.assertNull(row.getLocalDate(0));
              ctx.assertNull(row.getLocalDate("OffsetTime"));
              ctx.assertNull(row.getLocalTime(0));
              ctx.assertNull(row.getLocalTime("OffsetTime"));
              ctx.assertNull(row.getOffsetTime(0));
              ctx.assertNull(row.getOffsetTime("OffsetTime"));
              ctx.assertNull(row.getLocalDateTime(0));
              ctx.assertNull(row.getLocalDateTime("OffsetTime"));
              ctx.assertNull(row.getOffsetDateTime(0));
              ctx.assertNull(row.getOffsetDateTime("OffsetTime"));
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("OffsetTime"));
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("OffsetTime"));
              ctx.assertEquals(dt, row.getOffsetTimeArray(0)[0]);
              ctx.assertEquals(dt, row.getOffsetTimeArray("OffsetTime")[0]);
              ctx.assertEquals(dt, ((OffsetTime[]) row.getValue(0))[0]);
              ctx.assertEquals(dt, ((OffsetTime[]) row.getValue("OffsetTime"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("OffsetTime"));
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("OffsetTime"));
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("OffsetTime"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("OffsetTime"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("OffsetTime"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("OffsetTime"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("OffsetTime"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("OffsetTime"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("OffsetTime"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("OffsetTime"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("OffsetTime"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("OffsetTime"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeLocalDateTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"LocalDateTime\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("LocalDateTime"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("LocalDateTime"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("LocalDateTime"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("LocalDateTime"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("LocalDateTime"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("LocalDateTime"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("LocalDateTime"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("LocalDateTime"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("LocalDateTime"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("LocalDateTime"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("LocalDateTime"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("LocalDateTime"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("LocalDateTime"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("LocalDateTime"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("LocalDateTime"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("LocalDateTime"));
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("LocalDateTime"));
            final LocalDateTime dt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
            ctx.assertEquals(dt, row.getLocalDateTimeArray(0)[0]);
            ctx.assertEquals(dt, row.getLocalDateTimeArray("LocalDateTime")[0]);
            ctx.assertEquals(dt, ((LocalDateTime[]) row.getValue(0))[0]);
            ctx.assertEquals(dt, ((LocalDateTime[]) row.getValue("LocalDateTime"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("LocalDateTime"));
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("LocalDateTime"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("LocalDateTime"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("LocalDateTime"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("LocalDateTime"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("LocalDateTime"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("LocalDateTime"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("LocalDateTime"));
            ctx.assertNull(row.getOffsetTimeArray(0));
            ctx.assertNull(row.getOffsetTimeArray("LocalDateTime"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("LocalDateTime"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("LocalDateTime"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("LocalDateTime"));
            async.complete();
          }));
        }));
    }));
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
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("LocalDateTime"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("LocalDateTime"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("LocalDateTime"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("LocalDateTime"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("LocalDateTime"));
              ctx.assertNull(row.getString(0));
              ctx.assertNull(row.getString("LocalDateTime"));
              ctx.assertNull(row.getJsonObject(0));
              ctx.assertNull(row.getJsonObject("LocalDateTime"));
              ctx.assertNull(row.getJsonArray(0));
              ctx.assertNull(row.getJsonArray("LocalDateTime"));
              ctx.assertNull(row.getBuffer(0));
              ctx.assertNull(row.getBuffer("LocalDateTime"));
              ctx.assertNull(row.getTemporal(0));
              ctx.assertNull(row.getTemporal("LocalDateTime"));
              ctx.assertNull(row.getLocalDate(0));
              ctx.assertNull(row.getLocalDate("LocalDateTime"));
              ctx.assertNull(row.getLocalTime(0));
              ctx.assertNull(row.getLocalTime("LocalDateTime"));
              ctx.assertNull(row.getOffsetTime(0));
              ctx.assertNull(row.getOffsetTime("LocalDateTime"));
              ctx.assertNull(row.getLocalDateTime(0));
              ctx.assertNull(row.getLocalDateTime("LocalDateTime"));
              ctx.assertNull(row.getOffsetDateTime(0));
              ctx.assertNull(row.getOffsetDateTime("LocalDateTime"));
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("LocalDateTime"));
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("LocalDateTime"));
              ctx.assertEquals(dt, row.getLocalDateTimeArray(0)[0]);
              ctx.assertEquals(dt, row.getLocalDateTimeArray("LocalDateTime")[0]);
              ctx.assertEquals(dt, ((LocalDateTime[]) row.getValue(0))[0]);
              ctx.assertEquals(dt, ((LocalDateTime[]) row.getValue("LocalDateTime"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("LocalDateTime"));
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("LocalDateTime"));
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("LocalDateTime"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("LocalDateTime"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("LocalDateTime"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("LocalDateTime"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("LocalDateTime"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("LocalDateTime"));
              ctx.assertNull(row.getOffsetTimeArray(0));
              ctx.assertNull(row.getOffsetTimeArray("LocalDateTime"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("LocalDateTime"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("LocalDateTime"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("LocalDateTime"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeOffsetDateTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"OffsetDateTime\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("UUID"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("OffsetDateTime"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("OffsetDateTime"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("OffsetDateTime"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("OffsetDateTime"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("OffsetDateTime"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("OffsetDateTime"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("OffsetDateTime"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("OffsetDateTime"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("OffsetDateTime"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("OffsetDateTime"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("OffsetDateTime"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("OffsetDateTime"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("OffsetDateTime"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("OffsetDateTime"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("OffsetDateTime"));
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("OffsetDateTime"));
            final OffsetDateTime dt = OffsetDateTime.parse("2017-05-15T02:59:59.237666Z");
            ctx.assertEquals(dt, row.getOffsetDateTimeArray(0)[0]);
            ctx.assertEquals(dt, row.getOffsetDateTimeArray("OffsetDateTime")[0]);
            ctx.assertEquals(dt, ((OffsetDateTime[]) row.getValue(0))[0]);
            ctx.assertEquals(dt, ((OffsetDateTime[]) row.getValue("OffsetDateTime"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("OffsetDateTime"));
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("OffsetDateTime"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("OffsetDateTime"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("OffsetDateTime"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("OffsetDateTime"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("OffsetDateTime"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("OffsetDateTime"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("OffsetDateTime"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("OffsetDateTime"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("OffsetDateTime"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("OffsetDateTime"));
            ctx.assertNull(row.getUUIDArray(0));
            ctx.assertNull(row.getUUIDArray("OffsetDateTime"));
            async.complete();
          }));
        }));
    }));
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
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("OffsetDateTime"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("OffsetDateTime"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("OffsetDateTime"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("OffsetDateTime"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("OffsetDateTime"));
              ctx.assertNull(row.getString(0));
              ctx.assertNull(row.getString("OffsetDateTime"));
              ctx.assertNull(row.getJsonObject(0));
              ctx.assertNull(row.getJsonObject("OffsetDateTime"));
              ctx.assertNull(row.getJsonArray(0));
              ctx.assertNull(row.getJsonArray("OffsetDateTime"));
              ctx.assertNull(row.getBuffer(0));
              ctx.assertNull(row.getBuffer("OffsetDateTime"));
              ctx.assertNull(row.getTemporal(0));
              ctx.assertNull(row.getTemporal("OffsetDateTime"));
              ctx.assertNull(row.getLocalDate(0));
              ctx.assertNull(row.getLocalDate("OffsetDateTime"));
              ctx.assertNull(row.getLocalTime(0));
              ctx.assertNull(row.getLocalTime("OffsetDateTime"));
              ctx.assertNull(row.getOffsetTime(0));
              ctx.assertNull(row.getOffsetTime("OffsetDateTime"));
              ctx.assertNull(row.getLocalDateTime(0));
              ctx.assertNull(row.getLocalDateTime("OffsetDateTime"));
              ctx.assertNull(row.getOffsetDateTime(0));
              ctx.assertNull(row.getOffsetDateTime("OffsetDateTime"));
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("OffsetDateTime"));
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("OffsetDateTime"));
              ctx.assertEquals(dt, row.getOffsetDateTimeArray(0)[0]);
              ctx.assertEquals(dt, row.getOffsetDateTimeArray("OffsetDateTime")[0]);
              ctx.assertEquals(dt, ((OffsetDateTime[]) row.getValue(0))[0]);
              ctx.assertEquals(dt, ((OffsetDateTime[]) row.getValue("OffsetDateTime"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("OffsetDateTime"));
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("OffsetDateTime"));
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("OffsetDateTime"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("OffsetDateTime"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("OffsetDateTime"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("OffsetDateTime"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("OffsetDateTime"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("OffsetDateTime"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("OffsetDateTime"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("OffsetDateTime"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("OffsetDateTime"));
              ctx.assertNull(row.getUUIDArray(0));
              ctx.assertNull(row.getUUIDArray("OffsetDateTime"));
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeUUIDArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"UUID\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.updatedCount());
            Row row = result.iterator().next();
            ctx.assertNull(row.getBoolean(0));
            ctx.assertNull(row.getBoolean("UUID"));
            ctx.assertNull(row.getLong(0));
            ctx.assertNull(row.getLong("UUID"));
            ctx.assertNull(row.getInteger(0));
            ctx.assertNull(row.getInteger("UUID"));
            ctx.assertNull(row.getFloat(0));
            ctx.assertNull(row.getFloat("UUID"));
            ctx.assertNull(row.getDouble(0));
            ctx.assertNull(row.getDouble("UUID"));
            ctx.assertNull(row.getString(0));
            ctx.assertNull(row.getString("UUID"));
            ctx.assertNull(row.getJsonObject(0));
            ctx.assertNull(row.getJsonObject("UUID"));
            ctx.assertNull(row.getJsonArray(0));
            ctx.assertNull(row.getJsonArray("UUID"));
            ctx.assertNull(row.getBuffer(0));
            ctx.assertNull(row.getBuffer("UUID"));
            ctx.assertNull(row.getTemporal(0));
            ctx.assertNull(row.getTemporal("UUID"));
            ctx.assertNull(row.getLocalDate(0));
            ctx.assertNull(row.getLocalDate("UUID"));
            ctx.assertNull(row.getLocalTime(0));
            ctx.assertNull(row.getLocalTime("UUID"));
            ctx.assertNull(row.getOffsetTime(0));
            ctx.assertNull(row.getOffsetTime("UUID"));
            ctx.assertNull(row.getLocalDateTime(0));
            ctx.assertNull(row.getLocalDateTime("UUID"));
            ctx.assertNull(row.getOffsetDateTime(0));
            ctx.assertNull(row.getOffsetDateTime("UUID"));
            ctx.assertNull(row.getUUID(0));
            ctx.assertNull(row.getUUID("UUID"));
            ctx.assertNull(row.getShortArray(0));
            ctx.assertNull(row.getShortArray("UUID"));
            final UUID uuid = UUID.fromString("6f790482-b5bd-438b-a8b7-4a0bed747011");
            ctx.assertEquals(uuid, row.getUUIDArray(0)[0]);
            ctx.assertEquals(uuid, row.getUUIDArray("UUID")[0]);
            ctx.assertEquals(uuid, ((UUID[]) row.getValue(0))[0]);
            ctx.assertEquals(uuid, ((UUID[]) row.getValue("UUID"))[0]);
            ctx.assertNull(row.getBooleanArray(0));
            ctx.assertNull(row.getBooleanArray("UUID"));
            ctx.assertNull(row.getIntArray(0));
            ctx.assertNull(row.getIntArray("UUID"));
            ctx.assertNull(row.getLongArray(0));
            ctx.assertNull(row.getLongArray("UUID"));
            ctx.assertNull(row.getFloatArray(0));
            ctx.assertNull(row.getFloatArray("UUID"));
            ctx.assertNull(row.getDoubleArray(0));
            ctx.assertNull(row.getDoubleArray("UUID"));
            ctx.assertNull(row.getStringArray(0));
            ctx.assertNull(row.getStringArray("UUID"));
            ctx.assertNull(row.getLocalDateArray(0));
            ctx.assertNull(row.getLocalDateArray("UUID"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("UUID"));
            ctx.assertNull(row.getLocalTimeArray(0));
            ctx.assertNull(row.getLocalTimeArray("UUID"));
            ctx.assertNull(row.getLocalDateTimeArray(0));
            ctx.assertNull(row.getLocalDateTimeArray("UUID"));
            ctx.assertNull(row.getBufferArray(0));
            ctx.assertNull(row.getBufferArray("UUID"));
            ctx.assertNull(row.getOffsetDateTimeArray(0));
            ctx.assertNull(row.getOffsetDateTimeArray("UUID"));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeUUIDArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"UUID\" = $1  WHERE \"id\" = $2 RETURNING \"UUID\"",
        ctx.asyncAssertSuccess(p -> {
          final UUID uuid = UUID.fromString("6f790482-b5bd-438b-a8b7-4a0bed747011");
          p.execute(Tuple.tuple()
              .addUUIDArray(new UUID[]{uuid})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.updatedCount());
              Row row = result.iterator().next();
              ctx.assertNull(row.getBoolean(0));
              ctx.assertNull(row.getBoolean("UUID"));
              ctx.assertNull(row.getLong(0));
              ctx.assertNull(row.getLong("UUID"));
              ctx.assertNull(row.getInteger(0));
              ctx.assertNull(row.getInteger("UUID"));
              ctx.assertNull(row.getFloat(0));
              ctx.assertNull(row.getFloat("UUID"));
              ctx.assertNull(row.getDouble(0));
              ctx.assertNull(row.getDouble("UUID"));
              ctx.assertNull(row.getString(0));
              ctx.assertNull(row.getString("UUID"));
              ctx.assertNull(row.getJsonObject(0));
              ctx.assertNull(row.getJsonObject("UUID"));
              ctx.assertNull(row.getJsonArray(0));
              ctx.assertNull(row.getJsonArray("UUID"));
              ctx.assertNull(row.getBuffer(0));
              ctx.assertNull(row.getBuffer("UUID"));
              ctx.assertNull(row.getTemporal(0));
              ctx.assertNull(row.getTemporal("UUID"));
              ctx.assertNull(row.getLocalDate(0));
              ctx.assertNull(row.getLocalDate("UUID"));
              ctx.assertNull(row.getLocalTime(0));
              ctx.assertNull(row.getLocalTime("UUID"));
              ctx.assertNull(row.getOffsetTime(0));
              ctx.assertNull(row.getOffsetTime("UUID"));
              ctx.assertNull(row.getLocalDateTime(0));
              ctx.assertNull(row.getLocalDateTime("UUID"));
              ctx.assertNull(row.getOffsetDateTime(0));
              ctx.assertNull(row.getOffsetDateTime("UUID"));
              ctx.assertNull(row.getUUID(0));
              ctx.assertNull(row.getUUID("UUID"));
              ctx.assertNull(row.getIntArray(0));
              ctx.assertNull(row.getIntArray("UUID"));
              ctx.assertEquals(uuid, row.getUUIDArray(0)[0]);
              ctx.assertEquals(uuid, row.getUUIDArray("UUID")[0]);
              ctx.assertEquals(uuid, ((UUID[]) row.getValue(0))[0]);
              ctx.assertEquals(uuid, ((UUID[]) row.getValue("UUID"))[0]);
              ctx.assertNull(row.getBooleanArray(0));
              ctx.assertNull(row.getBooleanArray("UUID"));
              ctx.assertNull(row.getShortArray(0));
              ctx.assertNull(row.getShortArray("UUID"));
              ctx.assertNull(row.getLongArray(0));
              ctx.assertNull(row.getLongArray("UUID"));
              ctx.assertNull(row.getFloatArray(0));
              ctx.assertNull(row.getFloatArray("UUID"));
              ctx.assertNull(row.getDoubleArray(0));
              ctx.assertNull(row.getDoubleArray("UUID"));
              ctx.assertNull(row.getStringArray(0));
              ctx.assertNull(row.getStringArray("UUID"));
              ctx.assertNull(row.getLocalDateArray(0));
              ctx.assertNull(row.getLocalDateArray("UUID"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("UUID"));
              ctx.assertNull(row.getLocalTimeArray(0));
              ctx.assertNull(row.getLocalTimeArray("UUID"));
              ctx.assertNull(row.getLocalDateTimeArray(0));
              ctx.assertNull(row.getLocalDateTimeArray("UUID"));
              ctx.assertNull(row.getBufferArray(0));
              ctx.assertNull(row.getBufferArray("UUID"));
              ctx.assertNull(row.getOffsetDateTimeArray(0));
              ctx.assertNull(row.getOffsetDateTimeArray("UUID"));
              async.complete();
            }));
        }));
    }));
  }
}

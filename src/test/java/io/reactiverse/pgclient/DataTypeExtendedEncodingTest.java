package io.reactiverse.pgclient;

import io.reactiverse.pgclient.data.Interval;
import io.reactiverse.pgclient.data.Json;
import io.reactiverse.pgclient.data.Numeric;
import io.reactiverse.pgclient.data.Point;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class DataTypeExtendedEncodingTest extends DataTypeTestBase {

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
            ctx.assertEquals(1, result.rowCount());
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
              ctx.assertEquals(1, result.rowCount());
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
  public void testDecodeEnum(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"currentMood\" FROM \"EnumDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "currentMood")
              .returns(Tuple::getValue, Row::getValue, "ok")
              .returns(Tuple::getString, Row::getString, "ok")
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeEnum(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"EnumDataType\" SET \"currentMood\" = $1  WHERE \"id\" = $2 RETURNING \"currentMood\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addString("happy")
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.rowCount());
              Row row = result.iterator().next();
              ColumnChecker.checkColumn(0, "currentMood")
                .returns(Tuple::getValue, Row::getValue, "happy")
                .returns(Tuple::getString, Row::getString, "happy")
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
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
              ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
              ctx.assertEquals(1, result.rowCount());
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
  public void testEncodeCustomType(TestContext ctx) {
    Async async = ctx.async();
    String actual = "('Othercity',\" 'Second Ave'\",f)";
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CustomDataType\" SET \"address\" = $1  WHERE \"id\" = $2 RETURNING \"address\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addString("('Othercity', 'Second Ave', false)")
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.rowCount());
              Row row = result.iterator().next();
              ColumnChecker.checkColumn(0, "address")
                .returns(Tuple::getValue, Row::getValue, actual)
                .returns(Tuple::getString, Row::getString, actual)
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
            ctx.assertEquals(1, result.rowCount());
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
              ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
              ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Date\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Time\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            LocalTime lt = LocalTime.parse("17:55:04.905120");
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"TimeTz\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            OffsetTime ot = OffsetTime.parse("17:55:04.905120+03:07");
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(3), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            LocalDateTime ldt = LocalDateTime.parse("1800-01-01T23:57:53.237666");
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

  static final LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");

  @Test
  public void testDecodeTimestampAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
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
              ctx.assertEquals(1, result.rowCount());
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'", ctx.asyncAssertSuccess(v -> {
        conn.prepare("SELECT \"TimestampTz\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
          ctx.asyncAssertSuccess(p -> {
            p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.rowCount());
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

  static final UUID uuid = UUID.fromString("6f790482-b5bd-438b-a8b7-4a0bed747011");

  @Test
  public void testDecodeUUID(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"uuid\" FROM \"CharacterDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
  public void testDecodeInterval(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Interval\" FROM \"TemporalDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          // 10 years 3 months 332 days 20 hours 20 minutes 20.999999 seconds
          Interval expected = Interval.of()
            .years(10)
            .months(3)
            .days(332)
            .hours(20)
            .minutes(20)
            .seconds(20)
            .microseconds(999999);
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
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
  public void testNumeric(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: NUMERIC)) AS t (c)",
      new Numeric[]{
        Numeric.create(10),
        Numeric.create(200030004),
        Numeric.create(-500),
        Numeric.NaN
      }, Tuple::getNumeric);
  }

  @Test
  public void testNumericArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: NUMERIC[])) AS t (c)",
      new Numeric[][]{new Numeric[]{Numeric.create(10), Numeric.create(200030004), null, Numeric.create(-500), Numeric.NaN, null}},
      Tuple::getNumericArray);
  }

  @Test
  public void testJSON(TestContext ctx) {
    testJson(ctx, "JSON");
  }

  @Test
  public void testJSONB(TestContext ctx) {
    testJson(ctx, "JSONB");
  }

  private void testJson(TestContext ctx, String jsonType) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: " + jsonType + ")) AS t (c)",
      new Json[]{
        Json.create(10),
        Json.create(true),
        Json.create("hello"),
        Json.create(new JsonObject().put("foo", "bar")),
        Json.create(new JsonArray().add(0).add(1).add(2))
      }, Tuple::getJson);
  }

  @Test
  public void testJSONArray(TestContext ctx) {
    testJsonArray(ctx, "JSON");
  }

  @Test
  public void testJSONBArray(TestContext ctx) {
    testJsonArray(ctx, "JSONB");
  }

  private void testJsonArray(TestContext ctx, String jsonType) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: " + jsonType + "[])) AS t (c)",
      new Json[][]{
        new Json[]{Json.create(10),
          Json.create(true),
          Json.create("hello"),
          Json.create(new JsonObject().put("foo", "bar")),
          Json.create(new JsonArray().add(0).add(1).add(2))}
      }, Tuple::getJsonArray);
  }

  @Test
  public void testBooleanArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: BOOL[])) AS t (c)",
      new Boolean[][]{new Boolean[]{true, null, false}}, Tuple::getBooleanArray);
  }

  @Test
  public void testShortArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: INT2[])) AS t (c)",
      new Short[][]{new Short[]{0, -10, null, Short.MAX_VALUE}}, Tuple::getShortArray);
  }

  @Test
  public void testIntegerArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: INT4[])) AS t (c)",
      new Integer[][]{new Integer[]{0, -10, null, Integer.MAX_VALUE}}, Tuple::getIntegerArray);
  }

  @Test
  public void testLongArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: INT8[])) AS t (c)",
      new Long[][]{new Long[]{0L, -10L, null, Long.MAX_VALUE}}, Tuple::getLongArray);
  }

  @Test
  public void testFloatArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: FLOAT4[])) AS t (c)",
      new Float[][]{new Float[]{0f, -10f, Float.MAX_VALUE}}, Tuple::getFloatArray);
  }

  @Test
  public void testPoint(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: POINT)) AS t (c)",
      new Point[]{new Point(0, 0), new Point(10.45, 20.178)}, Tuple::getPoint);
  }

  @Test
  public void testPointArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: POINT[])) AS t (c)",
      new Point[][]{new Point[]{new Point(4, 5), null, new Point(3.4, -4.5), null}},
      Tuple::getPointArray);
  }

  private static <T> void compare(TestContext ctx, T expected, T actual) {
    if (expected != null && expected.getClass().isArray()) {
      ctx.assertNotNull(actual);
      ctx.assertTrue(actual.getClass().isArray());
      List expectedList = Arrays.asList((Object[]) expected);
      List actualList = Arrays.asList((Object[]) actual);
      ctx.assertEquals(expectedList, actualList);
    } else {
      ctx.assertEquals(expected, actual);
    }
  }

  private <T> void testGeneric(TestContext ctx, String sql, T[] expected, BiFunction<Row, Integer, T> getter) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      List<Tuple> batch = Stream.of(expected).map(Tuple::of).collect(Collectors.toList());
      conn.preparedBatch(sql, batch,
        ctx.asyncAssertSuccess(result -> {
          for (T n : expected) {
            ctx.assertEquals(result.size(), 1);
            Iterator<Row> it = result.iterator();
            Row row = it.next();
            compare(ctx, n, getter.apply(row, 0));
            compare(ctx, n, row.getValue(0));
            result = result.next();
          }
          ctx.assertNull(result);
          async.complete();
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
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []}");
            JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
            ColumnChecker.checkColumn(0, "JsonObject")
              .returns(Tuple::getValue, Row::getValue, Json.create(object))
              .returns(Tuple::getJson, Row::getJson, Json.create(object))
              .forRow(row);
            ColumnChecker.checkColumn(1, "JsonArray")
              .returns(Tuple::getValue, Row::getValue, Json.create(array))
              .returns(Tuple::getJson, Row::getJson, Json.create(array))
              .forRow(row);
            ColumnChecker.checkColumn(2, "Number")
              .returns(Tuple::getValue, Row::getValue, Json.create(4))
              .returns(Tuple::getJson, Row::getJson, Json.create(4))
              .forRow(row);
            ColumnChecker.checkColumn(3, "String")
              .returns(Tuple::getValue, Row::getValue, Json.create("Hello World"))
              .returns(Tuple::getJson, Row::getJson, Json.create("Hello World"))
              .forRow(row);
            ColumnChecker.checkColumn(4, "BooleanTrue")
              .returns(Tuple::getValue, Row::getValue, Json.create(true))
              .returns(Tuple::getJson, Row::getJson, Json.create(true))
              .forRow(row);
            ColumnChecker.checkColumn(5, "BooleanFalse")
              .returns(Tuple::getValue, Row::getValue, Json.create(false))
              .returns(Tuple::getJson, Row::getJson, Json.create(false))
              .forRow(row);
            ColumnChecker.checkColumn(6, "Null")
              .returns(Tuple::getValue, Row::getValue, Json.create(null))
              .returns(Tuple::getJson, Row::getJson, Json.create(null))
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
            .addJson(Json.create(object))
            .addJson(Json.create(array))
            .addJson(Json.create(4))
            .addJson(Json.create("Hello World"))
            .addJson(Json.create(true))
            .addJson(Json.create(false))
            .addJson(Json.create(null))
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "JsonObject")
              .returns(Tuple::getValue, Row::getValue, Json.create(object))
              .returns(Tuple::getJson, Row::getJson, Json.create(object))
              .forRow(row);
            ColumnChecker.checkColumn(1, "JsonArray")
              .returns(Tuple::getValue, Row::getValue, Json.create(array))
              .returns(Tuple::getJson, Row::getJson, Json.create(array))
              .forRow(row);
            ColumnChecker.checkColumn(2, "Number")
              .returns(Tuple::getValue, Row::getValue, Json.create(4))
              .returns(Tuple::getJson, Row::getJson, Json.create(4))
              .forRow(row);
            ColumnChecker.checkColumn(3, "String")
              .returns(Tuple::getValue, Row::getValue, Json.create("Hello World"))
              .returns(Tuple::getJson, Row::getJson, Json.create("Hello World"))
              .forRow(row);
            ColumnChecker.checkColumn(4, "BooleanTrue")
              .returns(Tuple::getValue, Row::getValue, Json.create(true))
              .returns(Tuple::getJson, Row::getJson, Json.create(true))
              .forRow(row);
            ColumnChecker.checkColumn(5, "BooleanFalse")
              .returns(Tuple::getValue, Row::getValue, Json.create(false))
              .returns(Tuple::getJson, Row::getJson, Json.create(false))
              .forRow(row);
            ColumnChecker.checkColumn(6, "Null")
              .returns(Tuple::getValue, Row::getValue, Json.create(null))
              .returns(Tuple::getJson, Row::getJson, Json.create(null))
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
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
            ctx.assertEquals(1, result.rowCount());
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
    for (int i = 0; i < len; i++) {
      builder.append((char) ('A' + (i % 26)));
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
  public void testDecodeBooleanArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Boolean\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Boolean")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new boolean[]{Boolean.TRUE}))
              .returns(Tuple::getBooleanArray, Row::getBooleanArray, ColumnChecker.toObjectArray(new boolean[]{Boolean.TRUE}))
              .forRow(result.iterator().next());
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
              .addBooleanArray(new Boolean[]{Boolean.FALSE, Boolean.TRUE})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Boolean")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new boolean[]{Boolean.FALSE, Boolean.TRUE}))
                .returns(Tuple::getBooleanArray, Row::getBooleanArray, ColumnChecker.toObjectArray(new boolean[]{Boolean.FALSE, Boolean.TRUE}))
                .forRow(result.iterator().next());
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
            ColumnChecker.checkColumn(0, "Short")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new short[]{1}))
              .returns(Tuple::getShortArray, Row::getShortArray, ColumnChecker.toObjectArray(new short[]{1}))
              .forRow(result.iterator().next());
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
              .addShortArray(new Short[]{2, 3, 4})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Short")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new short[]{2, 3, 4}))
                .returns(Tuple::getShortArray, Row::getShortArray, ColumnChecker.toObjectArray(new short[]{2, 3, 4}))
                .forRow(result.iterator().next());
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
            ColumnChecker.checkColumn(0, "Integer")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new int[]{2}))
              .returns(Tuple::getIntegerArray, Row::getIntegerArray, ColumnChecker.toObjectArray(new int[]{2}))
              .forRow(result.iterator().next());
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
              .addIntegerArray(new Integer[]{3, 4, 5, 6})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Integer")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new int[]{3, 4, 5, 6}))
                .returns(Tuple::getIntegerArray, Row::getIntegerArray, ColumnChecker.toObjectArray(new int[]{3, 4, 5, 6}))
                .forRow(result.iterator().next());
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
            ColumnChecker.checkColumn(0, "Long")
              .returns(Tuple::getValue, Row::getValue, new Long[]{3L})
              .returns(Tuple::getLongArray, Row::getLongArray, new Long[]{3L})
              .forRow(result.iterator().next());
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
              .addLongArray(new Long[]{4L, 5L, 6L, 7L, 8L})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Long")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new long[]{4, 5, 6, 7, 8}))
                .returns(Tuple::getLongArray, Row::getLongArray, ColumnChecker.toObjectArray(new long[]{4, 5, 6, 7, 8}))
                .forRow(result.iterator().next());
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
            ColumnChecker.checkColumn(0, "Float")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new float[]{4.1f}))
              .returns(Tuple::getFloatArray, Row::getFloatArray, ColumnChecker.toObjectArray(new float[]{4.1f}))
              .forRow(result.iterator().next());
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
              .addFloatArray(new Float[]{5.2f, 5.3f, 5.4f})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Float")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new float[]{5.2f, 5.3f, 5.4f}))
                .returns(Tuple::getFloatArray, Row::getFloatArray, ColumnChecker.toObjectArray(new float[]{5.2f, 5.3f, 5.4f}))
                .forRow(result.iterator().next());
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
            ColumnChecker.checkColumn(0, "Double")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new double[]{5.2}))
              .returns(Tuple::getDoubleArray, Row::getDoubleArray, ColumnChecker.toObjectArray(new double[]{5.2}))
              .forRow(result.iterator().next());
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
              ColumnChecker.checkColumn(0, "Double")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new double[]{6.3}))
                .returns(Tuple::getDoubleArray, Row::getDoubleArray, ColumnChecker.toObjectArray(new double[]{6.3}))
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testEncodeEmptyArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Double\" = $1  WHERE \"id\" = $2 RETURNING \"Double\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addDoubleArray(new Double[]{})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Double")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new double[]{}))
                .returns(Tuple::getDoubleArray, Row::getDoubleArray, ColumnChecker.toObjectArray(new double[]{}))
                .forRow(result.iterator().next());
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
            ColumnChecker.checkColumn(0, "Text")
              .returns(Tuple::getValue, Row::getValue, new String[]{"Knock, knock.Who’s there?very long pause….Java."})
              .returns(Tuple::getStringArray, Row::getStringArray, new String[]{"Knock, knock.Who’s there?very long pause….Java."})
              .forRow(result.iterator().next());
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
              ColumnChecker.checkColumn(0, "Text")
                .returns(Tuple::getValue, Row::getValue, new String[]{"Knock, knock.Who’s there?"})
                .returns(Tuple::getStringArray, Row::getStringArray, new String[]{"Knock, knock.Who’s there?"})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeEnumArray(TestContext ctx) {
    final String[] expected = new String[]{"ok", "unhappy", "happy"};
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Enum\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Enum")
              .returns(Tuple::getValue, Row::getValue, expected)
              .returns(Tuple::getStringArray, Row::getStringArray, expected)
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeEnumArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Enum\" = $1 WHERE \"id\" = $2 RETURNING \"Enum\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addStringArray(new String[]{"unhappy"})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Enum")
                .returns(Tuple::getValue, Row::getValue, new String[]{"unhappy"})
                .returns(Tuple::getStringArray, Row::getStringArray, new String[]{"unhappy"})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testEncodeEnumArrayMultipleValues(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Enum\" = $1 WHERE \"id\" = $2 RETURNING \"Enum\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addStringArray(new String[]{"unhappy", "ok"})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Enum")
                .returns(Tuple::getValue, Row::getValue, new String[]{"unhappy", "ok"})
                .returns(Tuple::getStringArray, Row::getStringArray, new String[]{"unhappy", "ok"})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testEncodeEnumArrayEmptyValues(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Enum\" = $1 WHERE \"id\" = $2 RETURNING \"Enum\", \"Boolean\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addStringArray(new String[]{})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Enum")
                .returns(Tuple::getValue, Row::getValue, new String[]{})
                .returns(Tuple::getStringArray, Row::getStringArray, new String[]{})
                .forRow(result.iterator().next());
              ColumnChecker.checkColumn(1, "Boolean")
                .returns(Tuple::getValue, Row::getValue, new Boolean[]{true})
                .returns(Tuple::getBooleanArray, Row::getBooleanArray, new Boolean[]{true})
                .forRow(result.iterator().next());
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
            final LocalDate dt = LocalDate.parse("1998-05-11");
            ColumnChecker.checkColumn(0, "LocalDate")
              .returns(Tuple::getValue, Row::getValue, new Object[]{dt, dt})
              .returns(Tuple::getLocalDateArray, Row::getLocalDateArray, new Object[]{dt, dt})
              .forRow(result.iterator().next());
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

  static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSS");
  static final LocalTime lt = LocalTime.parse("17:55:04.90512", dtf);

  @Test
  public void testDecodeLocalTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"LocalTime\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "LocalTime")
              .returns(Tuple::getValue, Row::getValue, new LocalTime[]{lt})
              .returns(Tuple::getLocalTimeArray, Row::getLocalTimeArray, new LocalTime[]{lt})
              .forRow(result.iterator().next());
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
              ColumnChecker.checkColumn(0, "LocalTime")
                .returns(Tuple::getValue, Row::getValue, new LocalTime[]{dt})
                .returns(Tuple::getLocalTimeArray, Row::getLocalTimeArray, new LocalTime[]{dt})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  static final OffsetTime dt = OffsetTime.parse("17:55:04.90512+03:00");

  @Test
  public void testDecodeOffsetTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"OffsetTime\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
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
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"LocalDateTime\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            final LocalDateTime dt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
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

  static final OffsetDateTime odt = OffsetDateTime.parse("2017-05-15T02:59:59.237666Z");

  @Test
  public void testDecodeOffsetDateTimeArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"OffsetDateTime\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "OffsetDateTime")
              .returns(Tuple::getValue, Row::getValue, new OffsetDateTime[]{odt})
              .returns(Tuple::getOffsetDateTimeArray, Row::getOffsetDateTimeArray, new OffsetDateTime[]{odt})
              .forRow(result.iterator().next());
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
  public void testDecodeUUIDArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"UUID\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            final UUID uuid = UUID.fromString("6f790482-b5bd-438b-a8b7-4a0bed747011");
            ColumnChecker.checkColumn(0, "UUID")
              .returns(Tuple::getValue, Row::getValue, new UUID[]{uuid})
              .returns(Tuple::getUUIDArray, Row::getUUIDArray, new UUID[]{uuid})
              .forRow(result.iterator().next());
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
              ColumnChecker.checkColumn(0, "UUID")
                .returns(Tuple::getValue, Row::getValue, new UUID[]{uuid})
                .returns(Tuple::getUUIDArray, Row::getUUIDArray, new UUID[]{uuid})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeNumericArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Numeric\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            Numeric[] expected = {
              Numeric.create(0),
              Numeric.create(1),
              Numeric.create(2),
              Numeric.create(3)
            };
            ColumnChecker.checkColumn(0, "Numeric")
              .returns(Tuple::getValue, Row::getValue, expected)
              .returns(Tuple::getNumericArray, Row::getNumericArray, expected)
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeNumericArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Numeric\" = $1  WHERE \"id\" = $2 RETURNING \"Numeric\"",
        ctx.asyncAssertSuccess(p -> {
          Numeric[] expected = {
            Numeric.create(0),
            Numeric.create(10000),
          };
          p.execute(Tuple.tuple()
              .addNumericArray(expected)
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Numeric")
                .returns(Tuple::getValue, Row::getValue, expected)
                .returns(Tuple::getNumericArray, Row::getNumericArray, expected)
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testBufferArray(TestContext ctx) {
    Random r = new Random();
    int len = 2048;
    byte[] bytes = new byte[len];
    r.nextBytes(bytes);
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT ARRAY[$1::BYTEA] \"Bytea\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.of(Buffer.buffer(bytes)), ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Bytea")
              .returns(Tuple::getValue, Row::getValue, new Buffer[]{Buffer.buffer(bytes)})
              .returns(Tuple::getBufferArray, Row::getBufferArray, new Buffer[]{Buffer.buffer(bytes)})
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }


  static final Interval[] intervals = new Interval[] {
    Interval.of().years(10).months(3).days(332).hours(20).minutes(20).seconds(20).microseconds(999991),
    Interval.of().minutes(20).seconds(20).microseconds(123456),
    Interval.of().years(-2).months(-6)
  };

  @Test
  public void testDecodeIntervalArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Interval\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Interval")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(intervals))
              .returns(Tuple::getIntervalArray, Row::getIntervalArray, ColumnChecker.toObjectArray(intervals))
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeIntervalArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Interval\" = $1  WHERE \"id\" = $2 RETURNING \"Interval\"",
        ctx.asyncAssertSuccess(p -> {
          Interval[] intervals = new Interval[] {
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

}

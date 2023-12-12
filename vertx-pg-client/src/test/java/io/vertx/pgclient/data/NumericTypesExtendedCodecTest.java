package io.vertx.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;

public class NumericTypesExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {
  @Test
  public void testDecodeInt2(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1 :: INT2 \"Short\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple().addShort((short) 32767))
            .onComplete(ctx.asyncAssertSuccess(result -> {
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
              .returns(Numeric.class, Numeric.create(32767))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeInt2(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Short\" = $1 WHERE \"id\" = $2 RETURNING \"Short\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.of(Short.MIN_VALUE, 2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
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
              .returns(Numeric.class, Numeric.create(-32768))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeInt4(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1 :: INT4 \"Integer\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple().addInteger(Integer.MAX_VALUE))
            .onComplete(ctx.asyncAssertSuccess(result -> {
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
              .returns(Numeric.class, Numeric.create(2147483647))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeInt4(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Integer\" = $1 WHERE \"id\" = $2 RETURNING \"Integer\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addInteger(Integer.MIN_VALUE)
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
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
                .returns(Numeric.class, Numeric.create(-2147483648))
                .forRow(row);
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeInt8(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1 :: INT8 \"Long\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple().addLong(Long.MAX_VALUE))
            .onComplete(ctx.asyncAssertSuccess(result -> {
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
              .returns(Numeric.class, Numeric.create(Long.MAX_VALUE))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeInt8(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Long\" = $1 WHERE \"id\" = $2 RETURNING \"Long\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addLong(Long.MIN_VALUE)
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
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
                .returns(Numeric.class, Numeric.create(Long.MIN_VALUE))
                .forRow(row);
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeFloat4(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1 :: FLOAT4\"Float\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple().addFloat(Float.MAX_VALUE))
            .onComplete(ctx.asyncAssertSuccess(result -> {
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
              .returns(Numeric.class, Numeric.parse("" + Float.MAX_VALUE))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeFloat4(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Float\" = $1 WHERE \"id\" = $2 RETURNING \"Float\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addFloat(Float.MIN_VALUE)
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
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
                .returns(Numeric.class, Numeric.parse("" + Float.MIN_VALUE))
                .forRow(row);
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeFloat8(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1 :: FLOAT8\"Double\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple().addDouble(Double.MAX_VALUE))
            .onComplete(ctx.asyncAssertSuccess(result -> {
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
              .returns(Numeric.class, Numeric.parse("" + Double.MAX_VALUE))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeFloat8(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Double\" = $1 WHERE \"id\" = $2 RETURNING \"Double\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addDouble(Double.MIN_VALUE)
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
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
                .returns(Numeric.class, Numeric.parse("" + Double.MIN_VALUE))
                .forRow(row);
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeSerial2(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"SmallSerial\" FROM \"NumericDataType\" WHERE \"id\" = $1").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.of(1))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "SmallSerial")
              .returns(Tuple::getValue, Row::getValue, (short) 1)
              .returns(Tuple::getShort, Row::getShort, (short) 1)
              .returns(Tuple::getInteger, Row::getInteger, 1)
              .returns(Tuple::getLong, Row::getLong, 1L)
              .returns(Tuple::getFloat, Row::getFloat, 1f)
              .returns(Tuple::getDouble, Row::getDouble, 1d)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(1))
              .returns(Numeric.class, Numeric.create(1))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeSerial2(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"SmallSerial\" = $1 WHERE \"id\" = $2 RETURNING \"SmallSerial\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.of(Short.MIN_VALUE, 2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "SmallSerial")
              .returns(Tuple::getValue, Row::getValue, (short) -32768)
              .returns(Tuple::getShort, Row::getShort, Short.MIN_VALUE)
              .returns(Tuple::getInteger, Row::getInteger, -32768)
              .returns(Tuple::getLong, Row::getLong, -32768L)
              .returns(Tuple::getFloat, Row::getFloat, -32768f)
              .returns(Tuple::getDouble, Row::getDouble, -32768d)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(-32768))
              .returns(Numeric.class, Numeric.create(-32768))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeSerial4(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Serial\" FROM \"NumericDataType\" WHERE \"id\" = $1").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple().addInteger(1))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Serial")
              .returns(Tuple::getValue, Row::getValue, 1)
              .returns(Tuple::getShort, Row::getShort, (short) 1)
              .returns(Tuple::getInteger, Row::getInteger, 1)
              .returns(Tuple::getLong, Row::getLong, 1L)
              .returns(Tuple::getFloat, Row::getFloat, 1f)
              .returns(Tuple::getDouble, Row::getDouble, 1d)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(1))
              .returns(Numeric.class, Numeric.create(1))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeSerial4(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Serial\" = $1 WHERE \"id\" = $2 RETURNING \"Serial\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addInteger(Integer.MIN_VALUE)
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.rowCount());
              Row row = result.iterator().next();
              ColumnChecker.checkColumn(0, "Serial")
                .returns(Tuple::getValue, Row::getValue, Integer.MIN_VALUE)
                .returns(Tuple::getShort, Row::getShort, (short) 0)
                .returns(Tuple::getInteger, Row::getInteger, Integer.MIN_VALUE)
                .returns(Tuple::getLong, Row::getLong, -2147483648L)
                .returns(Tuple::getFloat, Row::getFloat, -2147483648f)
                .returns(Tuple::getDouble, Row::getDouble, -2147483648d)
                .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(-2147483648))
                .returns(Numeric.class, Numeric.create(-2147483648))
                .forRow(row);
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeSerial8(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"BigSerial\" FROM \"NumericDataType\" WHERE \"id\" = $1").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple().addInteger(1))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "BigSerial")
              .returns(Tuple::getValue, Row::getValue, 1L)
              .returns(Tuple::getShort, Row::getShort, (short) 1)
              .returns(Tuple::getInteger, Row::getInteger, 1)
              .returns(Tuple::getLong, Row::getLong, 1L)
              .returns(Tuple::getFloat, Row::getFloat, 1f)
              .returns(Tuple::getDouble, Row::getDouble, 1d)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(1))
              .returns(Numeric.class, Numeric.create(1))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeSerial8(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"BigSerial\" = $1 WHERE \"id\" = $2 RETURNING \"BigSerial\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addLong(Long.MIN_VALUE)
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.rowCount());
              Row row = result.iterator().next();
              ColumnChecker.checkColumn(0, "BigSerial")
                .returns(Tuple::getValue, Row::getValue, Long.MIN_VALUE)
                .returns(Tuple::getShort, Row::getShort, (short) 0)
                .returns(Tuple::getInteger, Row::getInteger, 0)
                .returns(Tuple::getLong, Row::getLong, Long.MIN_VALUE)
                .returns(Tuple::getFloat, Row::getFloat, -9.223372E18f)
                .returns(Tuple::getDouble, Row::getDouble, -9.223372036854776E18d)
                .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(Long.MIN_VALUE))
                .returns(Numeric.class, Numeric.create(Long.MIN_VALUE))
                .forRow(row);
              async.complete();
            }));
        }));
    }));
  }

/*
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
*/

/*
  @Test
  public void testNumericArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: NUMERIC[])) AS t (c)",
      new Numeric[][]{new Numeric[]{Numeric.create(10), Numeric.create(200030004), null, Numeric.create(-500), Numeric.NaN, null}},
      Tuple::getArrayOfNumerics);
  }
*/

  @Test
  public void testShortArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: INT2[])) AS t (c)",
      new Short[][]{new Short[]{0, -10, null, Short.MAX_VALUE}}, Tuple::getArrayOfShorts);
  }

  @Test
  public void testPrimitiveShortArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: INT2[])) AS t (c)",
      Collections.singletonList(Tuple.tuple().addValue(new short[]{0, -10, Short.MAX_VALUE})),
      new Short[][]{new Short[]{0, -10, Short.MAX_VALUE}}, Tuple::getArrayOfShorts);
  }

  @Test
  public void testIntegerArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: INT4[])) AS t (c)",
      new Integer[][]{new Integer[]{0, -10, null, Integer.MAX_VALUE}}, Tuple::getArrayOfIntegers);
  }

  @Test
  public void testPrimitiveIntegerArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: INT4[])) AS t (c)",
      Collections.singletonList(Tuple.tuple().addValue(new int[]{0, -10, Integer.MAX_VALUE})),
      new Integer[][]{new Integer[]{0, -10, Integer.MAX_VALUE}}, Tuple::getArrayOfIntegers);
  }

  @Test
  public void testLongArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: INT8[])) AS t (c)",
      new Long[][]{new Long[]{0L, -10L, null, Long.MAX_VALUE}}, Tuple::getArrayOfLongs);
  }

  @Test
  public void testPrimitiveLongArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: INT8[])) AS t (c)",
      Collections.singletonList(Tuple.tuple().addValue(new long[]{0L, -10L, Long.MAX_VALUE})),
      new Long[][]{new Long[]{0L, -10L, Long.MAX_VALUE}}, Tuple::getArrayOfLongs);
  }

  @Test
  public void testFloatArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: FLOAT4[])) AS t (c)",
      new Float[][]{new Float[]{0f, -10f, Float.MAX_VALUE}}, Tuple::getArrayOfFloats);
  }

  @Test
  public void testPrimitiveFloatArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: FLOAT4[])) AS t (c)",
      Collections.singletonList(Tuple.tuple().addValue(new float[]{0f, -10f, Float.MAX_VALUE})),
      new Float[][]{new Float[]{0f, -10f, Float.MAX_VALUE}}, Tuple::getArrayOfFloats);
  }

  @Test
  public void testDoubleArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: FLOAT8[])) AS t (c)",
      new Double[][]{new Double[]{0d, -10d, Double.MAX_VALUE}}, Tuple::getArrayOfDoubles);
  }

  @Test
  public void testPrimitiveDoubleArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: FLOAT8[])) AS t (c)",
      Collections.singletonList(Tuple.tuple().addValue(new double[]{0d, -10d, Double.MAX_VALUE})),
      new Double[][]{new Double[]{0d, -10d, Double.MAX_VALUE}}, Tuple::getArrayOfDoubles);
  }

  @Test
  public void testDecodeShortArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Short\" FROM \"ArrayDataType\" WHERE \"id\" = $1").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
            .addInteger(1))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Short")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new short[]{1}))
              .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, ColumnChecker.toObjectArray(new short[]{1}))
              .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, ColumnChecker.toObjectArray(new int[]{1}))
              .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, ColumnChecker.toObjectArray(new long[]{1}))
              .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, ColumnChecker.toObjectArray(new float[]{1}))
              .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, ColumnChecker.toObjectArray(new double[]{1}))
              .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, ColumnChecker.toObjectArray(new Numeric[]{Numeric.create(1)}))
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeShortArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Short\" = $1  WHERE \"id\" = $2 RETURNING \"Short\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addArrayOfShort(new Short[]{2, 3, 4})
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Short")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new short[]{2, 3, 4}))
                .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, ColumnChecker.toObjectArray(new short[]{2, 3, 4}))
                .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, ColumnChecker.toObjectArray(new int[]{2, 3, 4}))
                .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, ColumnChecker.toObjectArray(new long[]{2, 3, 4}))
                .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, ColumnChecker.toObjectArray(new float[]{2, 3, 4}))
                .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, ColumnChecker.toObjectArray(new double[]{2, 3, 4}))
                .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, ColumnChecker.toObjectArray(new Numeric[]{Numeric.create(2), Numeric.create(3), Numeric.create(4)}))
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeIntArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Integer\" FROM \"ArrayDataType\" WHERE \"id\" = $1").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
            .addInteger(1))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Integer")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new int[]{2}))
              .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, ColumnChecker.toObjectArray(new short[]{2}))
              .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, ColumnChecker.toObjectArray(new int[]{2}))
              .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, ColumnChecker.toObjectArray(new long[]{2}))
              .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, ColumnChecker.toObjectArray(new float[]{2}))
              .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, ColumnChecker.toObjectArray(new double[]{2}))
              .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, ColumnChecker.toObjectArray(new Numeric[]{Numeric.create(2)}))
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeIntArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Integer\" = $1  WHERE \"id\" = $2 RETURNING \"Integer\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addArrayOfInteger(new Integer[]{3, 4, 5, 6})
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Integer")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new int[]{3, 4, 5, 6}))
                .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, ColumnChecker.toObjectArray(new short[]{3, 4, 5, 6}))
                .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, ColumnChecker.toObjectArray(new int[]{3, 4, 5, 6}))
                .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, ColumnChecker.toObjectArray(new long[]{3, 4, 5, 6}))
                .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, ColumnChecker.toObjectArray(new float[]{3, 4, 5, 6}))
                .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, ColumnChecker.toObjectArray(new double[]{3, 4, 5, 6}))
                .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, ColumnChecker.toObjectArray(new Numeric[]{Numeric.create(3), Numeric.create(4), Numeric.create(5), Numeric.create(6)}))
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeLongArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Long\" FROM \"ArrayDataType\" WHERE \"id\" = $1").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
            .addInteger(1))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Long")
              .returns(Tuple::getValue, Row::getValue, new Long[]{3L})
              .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, new Short[]{(short)3})
              .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, new Integer[]{3})
              .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, new Long[]{3L})
              .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, new Float[]{3F})
              .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, new Double[]{3D})
              .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, ColumnChecker.toObjectArray(new Numeric[]{Numeric.create(3)}))
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeLongArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Long\" = $1  WHERE \"id\" = $2 RETURNING \"Long\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addArrayOfLong(new Long[]{4L, 5L, 6L, 7L, 8L})
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Long")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new long[]{4, 5, 6, 7, 8}))
                .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, ColumnChecker.toObjectArray(new short[]{4, 5, 6, 7, 8}))
                .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, ColumnChecker.toObjectArray(new int[]{4, 5, 6, 7, 8}))
                .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, ColumnChecker.toObjectArray(new long[]{4, 5, 6, 7, 8}))
                .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, ColumnChecker.toObjectArray(new float[]{4, 5, 6, 7, 8}))
                .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, ColumnChecker.toObjectArray(new double[]{4, 5, 6, 7, 8}))
                .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, ColumnChecker.toObjectArray(new Numeric[]{Numeric.create(4), Numeric.create(5), Numeric.create(6), Numeric.create(7), Numeric.create(8)}))
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeFloatArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Float\" FROM \"ArrayDataType\" WHERE \"id\" = $1").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
            .addInteger(1))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Float")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new float[]{4.1f}))
              .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, ColumnChecker.toObjectArray(new short[]{(short)4.1f}))
              .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, ColumnChecker.toObjectArray(new int[]{(int)4.1f}))
              .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, ColumnChecker.toObjectArray(new long[]{(long)4.1f}))
              .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, ColumnChecker.toObjectArray(new float[]{4.1f}))
              .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, ColumnChecker.toObjectArray(new double[]{(double)4.1f}))
              .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, ColumnChecker.toObjectArray(new Numeric[]{Numeric.create(4.1f)}))
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeFloatArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Float\" = $1  WHERE \"id\" = $2 RETURNING \"Float\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addArrayOfFloat(new Float[]{5.2f, 5.3f, 5.4f})
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Float")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new float[]{5.2f, 5.3f, 5.4f}))
                .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, ColumnChecker.toObjectArray(new short[]{(short)5.2f, (short)5.3f, (short)5.4f}))
                .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, ColumnChecker.toObjectArray(new int[]{(int)5.2f, (int)5.3f, (int)5.4f}))
                .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, ColumnChecker.toObjectArray(new long[]{(long)5.2f, (long)5.3f, (long)5.4f}))
                .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, ColumnChecker.toObjectArray(new float[]{5.2f, 5.3f, 5.4f}))
                .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, ColumnChecker.toObjectArray(new double[]{(double)5.2f, (double)5.3f, (double)5.4f}))
                .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, ColumnChecker.toObjectArray(new Numeric[]{Numeric.create(5.2f), Numeric.create(5.3f), Numeric.create(5.4f)}))
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeDoubleArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Double\" FROM \"ArrayDataType\" WHERE \"id\" = $1").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
            .addInteger(1))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Double")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new double[]{5.2}))
              .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, ColumnChecker.toObjectArray(new short[]{(short)5}))
              .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, ColumnChecker.toObjectArray(new int[]{5}))
              .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, ColumnChecker.toObjectArray(new long[]{5L}))
              .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, ColumnChecker.toObjectArray(new float[]{5.2F}))
              .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, ColumnChecker.toObjectArray(new double[]{5.2D}))
              .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, ColumnChecker.toObjectArray(new Numeric[]{Numeric.create(5.2D)}))
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeDoubleArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Double\" = $1  WHERE \"id\" = $2 RETURNING \"Double\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addArrayOfDouble(new Double[]{6.3})
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Double")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new double[]{6.3}))
                .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, ColumnChecker.toObjectArray(new short[]{(short)6.3}))
                .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, ColumnChecker.toObjectArray(new int[]{(int)6.3}))
                .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, ColumnChecker.toObjectArray(new long[]{(long)6.3}))
                .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, ColumnChecker.toObjectArray(new float[]{(float)6.3}))
                .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, ColumnChecker.toObjectArray(new double[]{6.3}))
                .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, ColumnChecker.toObjectArray(new Numeric[]{Numeric.create(6.3)}))
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testEncodeEmptyArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Double\" = $1  WHERE \"id\" = $2 RETURNING \"Double\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
              .addArrayOfDouble(new Double[]{})
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Double")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new double[]{}))
                .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, ColumnChecker.toObjectArray(new short[]{}))
                .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, ColumnChecker.toObjectArray(new int[]{}))
                .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, ColumnChecker.toObjectArray(new long[]{}))
                .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, ColumnChecker.toObjectArray(new float[]{}))
                .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, ColumnChecker.toObjectArray(new double[]{}))
                .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, ColumnChecker.toObjectArray(new Numeric[]{}))
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeNumericArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Numeric\" FROM \"ArrayDataType\" WHERE \"id\" = $1").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
            .addInteger(1))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            Numeric[] expected = {
              Numeric.create(0),
              Numeric.create(1),
              Numeric.create(2),
              Numeric.create(3)
            };
            ColumnChecker.checkColumn(0, "Numeric")
              .returns(Tuple::getValue, Row::getValue, expected)
              .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, new Short[]{0,1,2,3})
              .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, new Integer[]{0,1,2,3})
              .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, new Long[]{0L,1L,2L,3L})
              .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, new Float[]{0f,1f,2f,3f})
              .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, new Double[]{0D,1D,2D,3D})
              .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, new Numeric[]{Numeric.create(0),Numeric.create(1),Numeric.create(2),Numeric.create(3)})
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeNumericArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Numeric\" = $1  WHERE \"id\" = $2 RETURNING \"Numeric\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          Numeric[] expected = {
            Numeric.create(0),
            Numeric.create(10000),
          };
          p
            .query()
            .execute(Tuple.tuple()
              .addValue(expected)
              .addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Numeric")
                .returns(Tuple::getValue, Row::getValue, expected)
                .returns(Tuple::getArrayOfShorts, Row::getArrayOfShorts, new Short[]{expected[0].shortValue(), expected[1].shortValue()})
                .returns(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers, new Integer[]{expected[0].intValue(), expected[1].intValue()})
                .returns(Tuple::getArrayOfLongs, Row::getArrayOfLongs, new Long[]{expected[0].longValue(), expected[1].longValue()})
                .returns(Tuple::getArrayOfFloats, Row::getArrayOfFloats, new Float[]{expected[0].floatValue(), expected[1].floatValue()})
                .returns(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles, new Double[]{expected[0].doubleValue(), expected[1].doubleValue()})
                .returns(Tuple::getArrayOfNumerics, Row::getArrayOfNumerics, new Numeric[]{Numeric.create(expected[0]), Numeric.create(expected[1])})
                .returns(Numeric.class, expected)
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }
}

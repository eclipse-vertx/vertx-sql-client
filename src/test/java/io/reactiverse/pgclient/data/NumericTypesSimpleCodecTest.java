package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.math.BigDecimal;

public class NumericTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testInt2(TestContext ctx) {
    testNumber(ctx, new Number[]{(short) 32767, (short) -1}, "INT2");
  }

  @Test
  public void testInt4(TestContext ctx) {
    testNumber(ctx, new Number[]{2147483647, -1}, "INT4");
  }

  @Test
  public void testInt8(TestContext ctx) {
    testNumber(ctx, new Number[]{9223372036854775807L, -1L}, "INT8");
  }

  @Test
  public void testFloat4(TestContext ctx) {
    testNumber(ctx, new Number[]{3.4028235E38f, -1f}, "FLOAT4");
  }

  @Test
  public void testFloat8(TestContext ctx) {
    testNumber(ctx, new Number[]{1.7976931348623157E308D, -1D}, "FLOAT8");
  }

  @Test
  public void testSerial2(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT \"SmallSerial\" FROM \"NumericDataType\" WHERE \"id\" = 1", ctx.asyncAssertSuccess(result -> {
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
            .returns(Tuple::getNumeric, Row::getNumeric, Numeric.create(1))
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testSerial4(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT \"Serial\" FROM \"NumericDataType\" WHERE \"id\" = 1", ctx.asyncAssertSuccess(result -> {
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
            .returns(Tuple::getNumeric, Row::getNumeric, Numeric.create(1))
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testSerial8(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT \"BigSerial\" FROM \"NumericDataType\" WHERE \"id\" = 1", ctx.asyncAssertSuccess(result -> {
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
            .returns(Tuple::getNumeric, Row::getNumeric, Numeric.create(1))
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testNumeric(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 919.999999999999999999999999999999999999::NUMERIC \"Numeric\", 'NaN'::NUMERIC \"NaN\"", ctx.asyncAssertSuccess(result -> {
          Numeric numeric = Numeric.parse("919.999999999999999999999999999999999999");
          Numeric nan = Numeric.parse("NaN");
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Numeric")
            .returns(Tuple::getValue, Row::getValue, numeric)
            .returns(Tuple::getShort, Row::getShort, (short) 919)
            .returns(Tuple::getInteger, Row::getInteger, 919)
            .returns(Tuple::getLong, Row::getLong, 919L)
            .returns(Tuple::getFloat, Row::getFloat, 920f)
            .returns(Tuple::getDouble, Row::getDouble, 920.0)
            .returns(Tuple::getBigDecimal, Row::getBigDecimal, numeric.bigDecimalValue())
            .returns(Tuple::getNumeric, Row::getNumeric, numeric)
            .forRow(row);
          ColumnChecker.checkColumn(1, "NaN")
            .returns(Tuple::getValue, Row::getValue, nan)
            .returns(Tuple::getShort, Row::getShort, (short) 0)
            .returns(Tuple::getInteger, Row::getInteger, 0)
            .returns(Tuple::getLong, Row::getLong, 0L)
            .returns(Tuple::getFloat, Row::getFloat, Float.NaN)
            .returns(Tuple::getDouble, Row::getDouble, Double.NaN)
            .fails(Tuple::getBigDecimal, Row::getBigDecimal)
            .returns(Tuple::getNumeric, Row::getNumeric, nan)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  private void testNumber(TestContext ctx, Number[] values, String type) {
    Async async = ctx.async(values.length);
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      for (Number value : values) {
        conn
          .query("SELECT " + value + "::" + type + " \"col\"", ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "col")
              .returns(Tuple::getValue, Row::getValue, value)
              .returns(Tuple::getShort, Row::getShort, value.shortValue())
              .returns(Tuple::getInteger, Row::getInteger, value.intValue())
              .returns(Tuple::getLong, Row::getLong, value.longValue())
              .returns(Tuple::getFloat, Row::getFloat, value.floatValue())
              .returns(Tuple::getDouble, Row::getDouble, value.doubleValue())
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal("" + value))
              .returns(Tuple::getNumeric, Row::getNumeric, Numeric.parse("" + value))
              .forRow(row);
            async.countDown();
          }));
      }
    }));
  }

  @Test
  public void testDecodeINT2Array(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY [1 :: INT2]", "Short", Tuple::getShortArray, Row::getShortArray, (short) 1);
  }

  @Test
  public void testDecodeINT4Array(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY [2 :: INT4]", "Integer", Tuple::getIntegerArray, Row::getIntegerArray, 2);
  }

  @Test
  public void testDecodeINT8Array(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY [3 :: INT8]", "Long", Tuple::getLongArray, Row::getLongArray, 3L);
  }

  @Test
  public void testDecodeFLOAT4Array(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY [4.1 :: FLOAT4]", "Float", Tuple::getFloatArray, Row::getFloatArray, 4.1f);
  }

  @Test
  public void testDecodeFLOAT8Array(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY [5.2 :: FLOAT8]", "Double", Tuple::getDoubleArray, Row::getDoubleArray, 5.2d);
  }

  @Test
  public void testDecodeEmptyArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      // The extra column makes sure that reading the array remains confined in the value since we are doing
      // parsing of the array value
      conn.query("SELECT '{}'::bigint[] \"array\", 1 \"Extra\"",
        ctx.asyncAssertSuccess(result -> {
          ColumnChecker.checkColumn(0, "array")
            .returns(Tuple::getValue, Row::getValue, (Object[]) new Long[0])
            .returns(Tuple::getLongArray, Row::getLongArray, (Object[]) new Long[0])
            .forRow(result.iterator().next());
          async.complete();
        }));
    }));
  }
}

package io.vertx.clickhousenativeclient.tck;

import io.vertx.clickhousenativeclient.ClickhouseResource;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.tck.BinaryDataTypeEncodeTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.BiFunction;

@RunWith(VertxUnitRunner.class)
public class ClickhouseNativeBinaryDataTypeEncodeTest extends BinaryDataTypeEncodeTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseNativeBinaryDataTypeEncodeTest.class);

  //updates may be async even for non-replicated tables;
  public static final int SLEEP_TIME = 100;

  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("$").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }

  @Ignore
  @Test
  public void testTime(TestContext ctx) {
    //time is not supported
  }

  @Test
  public void testBoolean(TestContext ctx) {
    testEncodeGeneric(ctx, "test_boolean", Byte.class, null, (byte)0);
  }

  @Test
  public void testDouble(TestContext ctx) {
    //Double.MIN_VALUE is too small here
    testEncodeGeneric(ctx, "test_float_8", Double.class, Row::getDouble, (double) 4.9e-322);
  }

  @Test
  public void testNullValues(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(statement("ALTER TABLE basicdatatype UPDATE" +
            " test_int_2 = ",
          ", test_int_4 = ",
          ", test_int_8 = ",
          ", test_float_4 = ",
          ", test_float_8 = ",
          ", test_numeric = ",
          ", test_decimal = ",
          ", test_boolean = ",
          ", test_char = ",
          ", test_varchar = ",
          ", test_date = ",
          " WHERE id = 2"))
        .execute(Tuple.tuple()
            .addValue(null)
            .addValue(null)
            .addValue(null)
            .addValue(null)
            .addValue(null)
            .addValue(null)
            .addValue(null)
            .addValue(null)
            .addValue(null)
            .addValue(null)
            .addValue(null),
          ctx.asyncAssertSuccess(updateResult -> {
            try {
              Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
              LOG.error(e);
            }
            conn
              .preparedQuery("SELECT * FROM basicdatatype WHERE id = 2")
              .execute(ctx.asyncAssertSuccess(result -> {
                ctx.assertEquals(1, result.size());
                Row row = result.iterator().next();
                ctx.assertEquals(12, row.size());
                ctx.assertEquals(2, row.getInteger(0));
                for (int i = 1; i < 12; i++) {
                  ctx.assertNull(row.getValue(i));
                }
                conn.close();
              }));
          }));
    }));
  }

  @Override
  protected <T> void testEncodeGeneric(TestContext ctx,
                                       String columnName,
                                       Class<T> clazz,
                                       BiFunction<Row,String,T> getter,
                                       T expected) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(statement("ALTER TABLE basicdatatype UPDATE " + columnName + " = ", " WHERE id = 2"))
        .execute(Tuple.tuple().addValue(expected), ctx.asyncAssertSuccess(updateResult -> {
          try {
            Thread.sleep(SLEEP_TIME);
          } catch (InterruptedException e) {
            LOG.error(e);
          }
          conn
            .preparedQuery("SELECT " + columnName + " FROM basicdatatype WHERE id = 2")
            .execute(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              Row row = result.iterator().next();
              ctx.assertEquals(1, row.size());
              ctx.assertEquals(expected, row.getValue(0));
              ctx.assertEquals(expected, row.getValue(columnName));
              if (getter != null) {
                ctx.assertEquals(expected, getter.apply(row, columnName));
              }
//        ctx.assertEquals(expected, row.get(clazz, 0));
//        ColumnChecker.checkColumn(0, columnName)
//          .returns(Tuple::getValue, Row::getValue, expected)
//          .returns(byIndexGetter, byNameGetter, expected)
//          .forRow(row);
              conn.close();
            }));
        }));
    }));
  }
}

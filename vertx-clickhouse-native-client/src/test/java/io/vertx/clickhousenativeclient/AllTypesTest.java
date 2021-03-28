package io.vertx.clickhousenativeclient;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnection;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class AllTypesTest {
  private static final Logger LOG = LoggerFactory.getLogger(AllTypesTest.class);

  public static final String TABLE_PREFIX = "vertx_test_";

  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

  private ClickhouseNativeConnectOptions options;
  private Vertx vertx;

  @Before
  public void setup(TestContext ctx) {
    options = rule.options();
    vertx = Vertx.vertx();
  }

  @After
  public void teardDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testUInt8(TestContext ctx) {
    doTest(ctx, "uint8", Short.class, true, (short) 0, Arrays.asList((short)0, null, (short)255, (short)0));
  }

  @Test
  public void testInt8(TestContext ctx) {
    doTest(ctx, "int8", Byte.class, true, (byte) 0, Arrays.asList((byte)-128, (byte)0, null, (byte)127, (byte)0));
  }

  @Test
  public void testString(TestContext ctx) {
    doTest(ctx, "string", String.class, true, "", Arrays.asList("val1", null, "val2", "0"));
  }

  private void doTest(TestContext ctx, String tableSuffix, Class<?> desiredType, boolean hasLowCardinality,
                      Object nullValue,
                      List<Object> regularValues) {
    String tableName = TABLE_PREFIX + tableSuffix;
    ClickhouseNativeConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("TRUNCATE TABLE " + tableName).execute(
        ctx.asyncAssertSuccess(res1 -> {
          Sleep.sleepOrThrow();
          List<String> columnsList = columnsList(hasLowCardinality);
          String columnsStr = String.join(", ", columnsList);
          String query = "INSERT INTO " + tableName + " (" + columnsStr + ") VALUES";
          List<Tuple> batch = buildBatch(columnsList, nullValue, regularValues);
          conn.preparedQuery(query)
            .executeBatch(batch, ctx.asyncAssertSuccess(
              res2 -> {
                Sleep.sleepOrThrow();
                conn.query("SELECT " + columnsStr + " FROM " + tableName + " ORDER BY id").execute(ctx.asyncAssertSuccess(
                  res3 -> {
                    ctx.assertEquals(res3.size(), regularValues.size(), "row count mismatch");
                    RowIterator<Row> rows = res3.iterator();
                    int rowNo = 0;
                    while (rows.hasNext()) {
                      Row row = rows.next();
                      for (String columnName : columnsList) {
                        Object expectedValue = regularValues.get(rowNo);
                        expectedValue = buildColumnValue(rowNo, columnName, nullValue, expectedValue);
                        if (columnName.equalsIgnoreCase("id")) {
                          compare(ctx, row, rowNo, columnName, Byte.class, ((Number) expectedValue).byteValue());
                        } else {
                          compare(ctx, row, rowNo, columnName, desiredType, expectedValue);
                        }
                      }
                      ++rowNo;
                    }
                    conn.close();
                  }
                ));
              }
            ));
        }));
    }));
  }

  private List<String> columnsList(boolean hasLowCardinality) {
    List<String> columns = new ArrayList<>(Arrays.asList("id", "simple_t", "nullable_t"));
    if (hasLowCardinality) {
      columns.addAll(Arrays.asList("simple_lc_t", "nullable_lc_t"));
    }
    return columns;
  }

  private List<Tuple> buildBatch(List<String> columnsList, Object nullValue, List<Object> regularValues) {
    List<Tuple> batch = new ArrayList<>(regularValues.size());
    for (int rowNo = 0; rowNo < regularValues.size(); ++rowNo) {
      Object regularValue = regularValues.get(rowNo);
      List<Object> vals = new ArrayList<>(columnsList.size());
      for (String columnName : columnsList) {
        Object val = buildColumnValue(rowNo, columnName, nullValue, regularValue);
        vals.add(val);
      }
      batch.add(Tuple.tuple(vals));
    }
    return batch;
  }

  private Object buildColumnValue(int rowNo, String columnName, Object nullValue, Object regularValue) {
    Object val;
    if (columnName.equalsIgnoreCase("id")) {
      val = rowNo;
    } else {
      if (regularValue == null) {
        if (columnName.startsWith("nullable_")) {
          if (columnName.startsWith("nullable_array")) {
            throw new IllegalArgumentException("not implemented");
          } else {
            val = null;
          }
        } else {
          val = nullValue;
        }
      } else {
        val = regularValue;
      }
    }
    return val;
  }

  private void compare(TestContext ctx, Row row, int rowNo, String colName, Class<?> desiredType, Object expected) {
    Object val = row.get(desiredType, colName);
    ctx.assertEquals(val, expected, "row " + colName + " mismatch; rowNo: " + rowNo);
  }
}

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
    doTest(ctx, "uint8", Short.class, true, (short) 0, Arrays.asList((short)0, null, (short)255, (short)0),
      Arrays.asList(new Short[]{}, new Short[]{0, 2, null, 3, 255}, new Short[]{255, 0, null}, new Short[]{0, null, 0}));
  }

  @Test
  public void testInt8(TestContext ctx) {
    doTest(ctx, "int8", Byte.class, true, (byte) 0, Arrays.asList((byte)-128, (byte)0, null, (byte)127, (byte)0),
      Arrays.asList(new Byte[]{}, new Byte[]{0, 2, null, 3, Byte.MAX_VALUE}, new Byte[]{Byte.MIN_VALUE, 0, null}, new Byte[]{0, null, 0}, new Byte[]{Byte.MAX_VALUE, Byte.MIN_VALUE}));
  }

  @Test
  public void testString(TestContext ctx) {
    doTest(ctx, "string", String.class, true, "", Arrays.asList("val1", null, "val2", "0"),
      Arrays.asList(new String[]{}, new String[]{"val1", "", null, "val2"}, new String[]{null, "", null}, new String[]{null}));
  }

  private void doTest(TestContext ctx, String tableSuffix, Class<?> desiredType, boolean hasLowCardinality,
                      Object nullValue,
                      List<Object> regularValues,
                      List<Object> nullableArrayValues) {
    ctx.assertEquals(regularValues.size(), nullableArrayValues.size());
    String tableName = TABLE_PREFIX + tableSuffix;
    ClickhouseNativeConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("TRUNCATE TABLE " + tableName).execute(
        ctx.asyncAssertSuccess(res1 -> {
          Sleep.sleepOrThrow();
          List<String> columnsList = columnsList(hasLowCardinality);
          String columnsStr = String.join(", ", columnsList);
          String query = "INSERT INTO " + tableName + " (" + columnsStr + ") VALUES";
          List<Tuple> batch = buildBatch(columnsList, nullValue, regularValues, nullableArrayValues);
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
                        Object expectedNullArrayValue = nullableArrayValues.get(rowNo);
                        expectedValue = buildColumnValue(rowNo, columnName, nullValue, expectedValue, expectedNullArrayValue);
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
    List<String> columns = new ArrayList<>(Arrays.asList("id", "simple_t", "nullable_t", "nullable_array_t"));
    if (hasLowCardinality) {
      columns.addAll(Arrays.asList("simple_lc_t", "nullable_lc_t", "nullable_array_lc_t"));
    }
    return columns;
  }

  private List<Tuple> buildBatch(List<String> columnsList, Object nullValue, List<Object> regularValues, List<Object> nullableArrayValues) {
    List<Tuple> batch = new ArrayList<>(regularValues.size());
    for (int rowNo = 0; rowNo < regularValues.size(); ++rowNo) {
      Object regularValue = regularValues.get(rowNo);
      Object nullableArrayValue = nullableArrayValues.get(rowNo);
      List<Object> vals = new ArrayList<>(columnsList.size());
      for (String columnName : columnsList) {
        Object val = buildColumnValue(rowNo, columnName, nullValue, regularValue, nullableArrayValue);
        vals.add(val);
      }
      batch.add(Tuple.tuple(vals));
    }
    return batch;
  }

  private Object buildColumnValue(int rowNo, String columnName, Object nullValue, Object regularValue, Object nullableArrayValue) {
    Object val;
    if (columnName.equalsIgnoreCase("id")) {
      val = rowNo;
    } else if (columnName.startsWith("nullable_array_")) {
      val = nullableArrayValue;
    } else if (columnName.equals("simple_t") || columnName.equals("simple_lc_t")) {
      if (regularValue == null) {
        val = nullValue;
      } else {
        val = regularValue;
      }
    } else if (columnName.equals("nullable_t") || columnName.equals("nullable_lc_t")) {
      val = regularValue;
    } else {
      throw new IllegalStateException("not implemented for " + columnName);
    }
    return val;
  }

  private void compare(TestContext ctx, Row row, int rowNo, String colName, Class<?> desiredType, Object expected) {
    boolean isArray = expected != null && expected.getClass().isArray();
    if (isArray) {
      desiredType = expected.getClass();
    }
    Object val = row.get(desiredType, colName);
    if (isArray) {
      boolean equals = Arrays.deepEquals((Object[]) val, (Object[]) expected);
      ctx.assertTrue(equals, colName + " row " + " mismatch; rowNo: " + rowNo);
    } else {
      ctx.assertEquals(val, expected, colName + "row " + " mismatch; rowNo: " + rowNo);
    }
  }
}

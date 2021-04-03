package io.vertx.clickhousenativeclient.alltypes;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnection;
import io.vertx.clickhousenativeclient.ClickhouseResource;
import io.vertx.clickhousenativeclient.Sleep;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public abstract class AllTypesBase<T> {
  private static final Logger LOG = LoggerFactory.getLogger(AllTypesBase.class);

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

  protected abstract String tableSuffix();
  protected abstract Class<T> elementType();

  private List<String> columnsList(boolean hasLowCardinality) {
    List<String> columns = new ArrayList<>(Arrays.asList("id", "simple_t", "nullable_t", "array_t", "array3_t", "nullable_array_t", "nullable_array3_t"));
    if (hasLowCardinality) {
      columns.addAll(Arrays.asList("simple_lc_t", "nullable_lc_t", "array_lc_t", "array3_lc_t", "nullable_array_lc_t", "nullable_array3_lc_t"));
    }
    return columns;
  }

  protected  <R> void doTest(TestContext ctx, String tableSuffix, boolean hasLowCardinality,
                      MyColumnChecker<R> columnChecker, List<Tuple> batch) {
    String tableName = TABLE_PREFIX + tableSuffix;
    ClickhouseNativeConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("TRUNCATE TABLE " + tableName).execute(
        ctx.asyncAssertSuccess(res1 -> {
          Sleep.sleepOrThrow();
          List<String> columnsList = columnsList(hasLowCardinality);
          String columnsStr = String.join(", ", columnsList);
          String query = "INSERT INTO " + tableName + " (" + columnsStr + ") VALUES";
          conn.preparedQuery(query)
            .executeBatch(batch, ctx.asyncAssertSuccess(
              res2 -> {
                Sleep.sleepOrThrow();
                conn.query("SELECT " + columnsStr + " FROM " + tableName + " ORDER BY id").execute(ctx.asyncAssertSuccess(
                  res3 -> {
                    ctx.assertEquals(res3.size(), batch.size(), "row count mismatch");
                    int batchIdx = 0;
                    for (Row row : res3) {
                      Number id = row.get(Number.class, "id");
                      Tuple expectedRow = batch.get(batchIdx);
                      LOG.info("checking row " + tableSuffix + ":" + id);
                      for (int colIdx = 0; colIdx < expectedRow.size(); ++colIdx) {
                        String colName = columnsList.get(colIdx);
                        Object expectedColumnValue = expectedRow.getValue(colIdx);
                        columnChecker.checkColumn(row, colIdx, colName, (R) expectedColumnValue);
                      }
                      ++batchIdx;
                    }
                  }));
              }));
        }));
    }));
  }
}

class MyColumnChecker<R> {
  private final Class<R> componentType;
  private final ColumnChecker.SerializableBiFunction<Tuple, Integer, R> byIndexGetter;
  private final ColumnChecker.SerializableBiFunction<Row, String, R> byNameGetter;
  private final ColumnChecker.SerializableBiFunction<Tuple, Integer, Object> arrayByIndexGetter;
  private final ColumnChecker.SerializableBiFunction<Row, String, Object> arrayByNameGetter;

  public MyColumnChecker(Class<R> componentType,
                         ColumnChecker.SerializableBiFunction<Tuple, Integer, R> byIndexGetter,
                         ColumnChecker.SerializableBiFunction<Row, String, R> byNameGetter,
                         ColumnChecker.SerializableBiFunction<Tuple, Integer, Object> arrayByIndexGetter,
                         ColumnChecker.SerializableBiFunction<Row, String, Object> arrayByNameGetter) {
    this.componentType = componentType;
    this.byIndexGetter = byIndexGetter;
    this.byNameGetter = byNameGetter;
    this.arrayByNameGetter = arrayByNameGetter;
    this.arrayByIndexGetter = arrayByIndexGetter;
  }

  public void checkColumn(Row row, int index, String name, R expected) {
    ColumnChecker checker = ColumnChecker.checkColumn(index, name);
    if ("id".equals(name)) {
      checker.returns((Class<R>)expected.getClass(), expected)
        .forRow(row);
      return;
    }
    if (componentType == byte[].class && (expected == null || expected.getClass() == byte[].class)) {
      //ask driver to turn off String encoding
      checker = checker
        .returns((tuple, idx) -> tuple.get(byte[].class, idx),
                 (ColumnChecker.SerializableBiFunction<Row, String, byte[]>) (r, colName) -> r.get(byte[].class, colName),
                 (Consumer<byte[]>) actual -> Assert.assertArrayEquals((byte[])actual, (byte[])expected));
    } else {
      //arrays are non-nullable
      if (expected != null && expected.getClass().isArray()) {
        boolean multidimensional = expected.getClass().getComponentType().isArray() && expected.getClass().getComponentType() != byte[].class;
        if (componentType == byte[].class) {
          //ask driver to turn off String encoding
          checker = checker.returns((tuple, idx) -> tuple.get(expected.getClass(), idx), (r, colName) -> r.get(expected.getClass(), colName), (Object[]) expected);
        } else {
          checker = checker.returns(Tuple::getValue, Row::getValue, (Object[]) expected);
        }
        if (!multidimensional && arrayByIndexGetter != null) {
          //API does not provide dedicated methods to get multi-dimensional arrays
          checker = checker.returns(arrayByIndexGetter, arrayByNameGetter, (Object[]) expected);
        }
      } else {
        //regular non-array elements
        checker = checker.returns(Tuple::getValue, Row::getValue, expected);
        if (byIndexGetter != null) {
          checker = checker.returns(byIndexGetter, byNameGetter, expected);
        }
      }
    }
    checker.forRow(row);
  }
}

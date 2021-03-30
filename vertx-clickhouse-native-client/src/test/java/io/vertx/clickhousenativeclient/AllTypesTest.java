package io.vertx.clickhousenativeclient;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnection;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.DateColumnReader;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.DateTimeColumnReader;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.time.*;
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

  private List<String> columnsList(boolean hasLowCardinality) {
    List<String> columns = new ArrayList<>(Arrays.asList("id", "simple_t", "nullable_t", "array_t", "nullable_array_t"));
    if (hasLowCardinality) {
      columns.addAll(Arrays.asList("simple_lc_t", "nullable_lc_t", "array_lc_t", "nullable_array_lc_t"));
    }
    return columns;
  }

  @Test
  public void testUInt8(TestContext ctx) {
    List<Tuple> batch = Arrays.asList(
      Tuple.of((byte)1, (short)0,       (short)0,   new Short[]{},                new Short[]{},                   (short)0,   (short)0,   new Short[]{},                new Short[]{}   ),
      Tuple.of((byte)2, (short)0,  null,      new Short[]{0, 2, 0, 3, 255}, new Short[]{0, 2, null, 3, 255},  (short)0,   null,      new Short[]{0, 2, 0, 3, 255}, new Short[]{0, 2, null, 3, 255}      ),
      Tuple.of((byte)3, (short)255,     (short)255, new Short[]{255, 0, 0},       new Short[]{255, 0, null},       (short)255, (short)255, new Short[]{255, 0, 0},       new Short[]{255, 0, null} ),
      Tuple.of((byte)4, (short)0,       (short)0,   new Short[]{0, 0, 0},         new Short[]{0, null, 0},         (short)0  , (short)0,   new Short[]{0, 0, 0},         new Short[]{0, null, 0}   )
    );
    doTest(ctx, "uint8", Short.class, true, batch);
  }

  @Test
  public void testInt8(TestContext ctx) {
    List<Tuple> batch = Arrays.asList(
      Tuple.of((byte)1, (byte)-128,   (byte)-128, new Byte[]{},                   new Byte[]{},                      (byte)0,   (byte)0,   new Byte[]{},                new Byte[]{}   ),
      Tuple.of((byte)2, (byte)0, null,      new Byte[]{-128, 2, 0, 3, 127}, new Byte[]{-128, 2, null, 3, 127}, (byte)0,   null,      new Byte[]{0, 2, 0, 3, 127}, new Byte[]{0, 2, null, 3, -128}      ),
      Tuple.of((byte)3, (byte)127,    (byte)127,  new Byte[]{127, 0, 0},          new Byte[]{127, 0, null},          (byte)255, (byte)255, new Byte[]{-128, 0, 0},      new Byte[]{127, 0, null} ),
      Tuple.of((byte)4, (byte)0,      (byte)0,    new Byte[]{0, 0, 0},            new Byte[]{0, null, 0},            (byte)0,   (byte)0,   new Byte[]{0, 0, 0},         new Byte[]{0, null, 0}   )
    );
    doTest(ctx, "int8", Byte.class, true, batch);
  }

  @Test
  public void testString(TestContext ctx) {
    String v2 = "val2";
    String v_1 = "val_1";
    String v4 = "val_4";
    String v5 = "val5";
    String v3 = "val3";
    String v1 = "val1";
    List<Tuple> batch = Arrays.asList(
      Tuple.of((byte)1, v2,  v3,   new String[]{},                    new String[]{},                          "",   "",   new String[]{},                   new String[]{} ),
      Tuple.of((byte)2, "",  null, new String[]{v3, v1, "", "z", v5}, new String[]{v3, v1, null, "", "z", v3}, "", null,   new String[]{"", v1, "", v2, v2}, new String[]{"", v2, null, v3, v2} ),
      Tuple.of((byte)3, v_1, v4,   new String[]{v5, "", ""},          new String[]{v3, "", null},              v5,   v5,   new String[]{v1, "", ""},         new String[]{v2, "", null} ),
      Tuple.of((byte)4, "",  "",   new String[]{"", "", ""},          new String[]{"", null, v5},              "",   "",   new String[]{"", "", ""},         new String[]{"", null, ""} ),
      Tuple.of((byte)5, v_1, v4,   new String[]{v5, "", ""},          new String[]{v3, "", null},              v5,   v5,   new String[]{v1, "", ""},         new String[]{v2, "", null} )
    );
    doTest(ctx, "string", String.class, true, batch);
  }

  @Test
  public void testBlob(TestContext ctx) {
    byte[] v2 = b("val2");
    byte[] em = b("");
    byte[] v3 = b("val3");
    byte[] v_4 = b("val_4");
    byte[] v1 = b("val1");
    byte[] v4 = b("val4");
    byte[] v_1 = b("val_1");
    byte[] z = b("z");
    List<Tuple> batch = Arrays.asList(
      Tuple.of((byte)1, v2,   v3, new byte[][]{},                  new byte[][]{},                        em, em,   new byte[][]{},                   new byte[][]{} ),
      Tuple.of((byte)2, em, null, new byte[][]{v3, v1, em, z, v4}, new byte[][]{v3, v1, null, em, z, v3}, em, null, new byte[][]{em, v1, em, v2, v2}, new byte[][]{em, v2, null, v3, v2} ),
      Tuple.of((byte)3, v_1, v_4, new byte[][]{v4, em, em},        new byte[][]{v3, em, null},            v4, v4,   new byte[][]{v1, em, em},         new byte[][]{v2, em, null} ),
      Tuple.of((byte)4, em,   em, new byte[][]{em, em, em},        new byte[][]{em, null, v4},            em, em,   new byte[][]{em, em, em},         new byte[][]{em, null, em} ),
      Tuple.of((byte)5,       v_1, v_4, new byte[][]{v4, em, em},        new byte[][]{v3, em, null},            v4, v4,   new byte[][]{v1, em, em},         new byte[][]{v2, em, null} )
    );
    doTest(ctx, "string", byte[].class, true, batch);
  }

  @Test
  public void testDate(TestContext ctx) {
    LocalDate dt = LocalDate.of(2020, 3, 29);
    LocalDate d2 = dt.plusDays(2);
    LocalDate d3 = dt.plusDays(3);
    LocalDate mn = DateColumnReader.MIN_VALUE;
    LocalDate mx = DateColumnReader.MAX_VALUE;
    List<Tuple> batch = Arrays.asList(
      Tuple.of((byte)1,       d2,   d2, new LocalDate[]{},                   new LocalDate[]{},                         mn, mn,   new LocalDate[]{},                   new LocalDate[]{} ),
      Tuple.of((byte)2, mn, null, new LocalDate[]{d2, mn, mn, mx, d3}, new LocalDate[]{d2, d3, null, mn, mn, d3}, mn, null, new LocalDate[]{mn, d2, mn, d3, d3}, new LocalDate[]{mn, d2, null, d3, d2} ),
      Tuple.of((byte)3,       dt,   dt, new LocalDate[]{d2, mn, mn},         new LocalDate[]{d3, mn, null},             d2, d3,   new LocalDate[]{d2, mn, mn},         new LocalDate[]{d2, mn, null} ),
      Tuple.of((byte)4,       dt,   dt, new LocalDate[]{mn, mn, mn},         new LocalDate[]{mn, null, d3},             mn, mn,   new LocalDate[]{mn, mn, mn},         new LocalDate[]{mn, null, mn} ),
      Tuple.of((byte)5,       mn,   mn, new LocalDate[]{d2, mn, mn},         new LocalDate[]{d3, mn, null},             d2, d3,   new LocalDate[]{d2, mn, mn},         new LocalDate[]{d2, mn, null} ),
      Tuple.of((byte)6,       mx,   mx, new LocalDate[]{d2, mn, mn},         new LocalDate[]{d3, mn, null},             d2, d3,   new LocalDate[]{d2, mn, mn},         new LocalDate[]{d2, mn, null} ),
      Tuple.of((byte)6,       mx,   mx, new LocalDate[]{d2, mn, mn},         new LocalDate[]{d3, mn, null},             d2, d3,   new LocalDate[]{d2, mn, mn},         new LocalDate[]{d2, mn, null} )
    );
    doTest(ctx, "date", LocalDate.class, true, batch);
  }

  @Test
  public void testDateTime(TestContext ctx) {
    ZoneId zoneId = ZoneId.of("Europe/Oslo");
    OffsetDateTime dt = Instant.ofEpochSecond(1617120094L).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime d2 = Instant.ofEpochSecond(1617120094L).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime d3 = Instant.ofEpochSecond(1617120094L).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime mn = Instant.ofEpochSecond(0).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime mx = Instant.ofEpochSecond(DateTimeColumnReader.MAX_EPOCH_SECOND).atZone(zoneId).toOffsetDateTime();

    List<Tuple> batch = Arrays.asList(
      Tuple.of((byte)1,       d2,   d2, new OffsetDateTime[]{},                   new OffsetDateTime[]{},                         mn, mn,   new OffsetDateTime[]{},                   new OffsetDateTime[]{} ),
      Tuple.of((byte)2, mn, null, new OffsetDateTime[]{d2, mn, mn, mx, d3}, new OffsetDateTime[]{d2, d3, null, mn, mn, d3}, mn, null, new OffsetDateTime[]{mn, d2, mn, d3, d3}, new OffsetDateTime[]{mn, d2, null, d3, d2} ),
      Tuple.of((byte)3,       dt,   dt, new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d3, mn, null},             d2, d3,   new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d2, mn, null} ),
      Tuple.of((byte)4,       dt,   dt, new OffsetDateTime[]{mn, mn, mn},         new OffsetDateTime[]{mn, null, d3},             mn, mn,   new OffsetDateTime[]{mn, mn, mn},         new OffsetDateTime[]{mn, null, mn} ),
      Tuple.of((byte)5,       mn,   mn, new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d3, mn, null},             d2, d3,   new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d2, mn, null} ),
      Tuple.of((byte)6,       mx,   mx, new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d3, mn, null},             d2, d3,   new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d2, mn, null} ),
      Tuple.of((byte)6,       mx,   mx, new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d3, mn, null},             d2, d3,   new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d2, mn, null} )
    );
    doTest(ctx, "datetime", OffsetDateTime.class, true, batch);
  }

  private static byte[] b(String s) {
    return s.getBytes(StandardCharsets.UTF_8);
  }

  private void doTest(TestContext ctx, String tableSuffix, Class<?> desiredType, boolean hasLowCardinality,
                      List<Tuple> batch) {
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
                      Tuple batchRow = batch.get(batchIdx);
                      Object id = row.getValue("id");
                      for (int colIdx = 0; colIdx < batchRow.size(); ++colIdx) {
                        String colName = columnsList.get(colIdx);
                        Object expectedValue = batchRow.getValue(colIdx);
                        Class<?> colType = expectedValue == null ? desiredType : expectedValue.getClass();
                        Object actualValue;
                        if ("id".equals(colName)) {
                          actualValue = row.getValue(colName);
                        } else {
                          actualValue = row.get(colType, colName);
                        }
                        compareValues(ctx, id, colName, colType, expectedValue, actualValue);
                      }
                      ++batchIdx;
                    }
                  }));
              }));
        }));
    }));
  }

  private void compareValues(TestContext ctx, Object id, String colName, Class<?> colType, Object expectedValue, Object actualValue) {
    if (colType.isArray()) {
      boolean equals;
      if (colType == byte[].class) {
        equals = Arrays.equals((byte[]) expectedValue, (byte[]) actualValue);
      } else {
        equals = Arrays.deepEquals((Object[]) expectedValue, (Object[]) actualValue);
      }
      ctx.assertTrue(equals, colName + " byte row mismatch; id = " + id);
    } else {
      ctx.assertEquals(expectedValue, actualValue, colName + " row mismatch; id = " + id);
    }
  }
}

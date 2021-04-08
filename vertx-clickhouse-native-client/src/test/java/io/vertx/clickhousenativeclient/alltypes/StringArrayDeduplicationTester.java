package io.vertx.clickhousenativeclient.alltypes;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnection;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;

import java.util.Arrays;
import java.util.List;

public class StringArrayDeduplicationTester {
  private final String tableName;
  private final ClickhouseNativeConnectOptions options;
  private final Vertx vertx;

  public StringArrayDeduplicationTester(String tableName, Vertx vertx, ClickhouseNativeConnectOptions options) {
    this.tableName = tableName;
    this.options = options;
    this.vertx = vertx;
  }

  public void test(TestContext ctx) {
    List<Tuple> batch = Arrays.asList(
      Tuple.of(1, (Object) new Object[][][]{ {{"str1_1", "str1_2", null, "dedup3", "dedup1"}, {null}}, {{"str1_3", "str1_4", null}, {null, "dedup2"}} }),
      Tuple.of(2, (Object) new Object[][][]{ {{"str2_1", "str2_2", null, "dedup2"}, {null, "dedup1"}} }),
      Tuple.of(3, (Object) new Object[][][]{ {{"str3_1", "str3_2", null}, {null}, {"dedup3"}} })
    );
    ClickhouseNativeConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery(String.format("INSERT INTO %s (id, nullable_array3_lc_t) VALUES", tableName)).executeBatch(batch, ctx.asyncAssertSuccess(result -> {
        conn.query("SELECT nullable_array3_lc_t FROM " + tableName + " ORDER BY id").execute(ctx.asyncAssertSuccess(
          res3 -> {
            ctx.assertEquals(res3.size(), batch.size(), "row count mismatch");
            RowIterator<Row> rows = res3.iterator();
            String[][][] r1 = rows.next().get(String[][][].class, 0);
            String[][][] r2 = rows.next().get(String[][][].class, 0);
            String[][][] r3 = rows.next().get(String[][][].class, 0);
            ctx.assertTrue(r1[0][0][3] == r3[0][2][0]);//dedup3
            ctx.assertTrue(r1[0][0][4] == r2[0][1][1]);//dedup1
            ctx.assertTrue(r1[1][1][1] == r2[0][0][3]);//dedup2
          }));
      }));
    }));
  }
}

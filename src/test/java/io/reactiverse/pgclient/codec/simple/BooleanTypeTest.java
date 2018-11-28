package io.reactiverse.pgclient.codec.simple;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.codec.ColumnChecker;
import io.reactiverse.pgclient.codec.SimpleQueryDataTypeCodecTestBase;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class BooleanTypeTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testBoolean(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT true \"TrueValue\", false \"FalseValue\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "TrueValue")
            .returns(Tuple::getValue, Row::getValue, true)
            .returns(Tuple::getBoolean, Row::getBoolean, true)
            .forRow(row);
          ColumnChecker.checkColumn(1, "FalseValue")
            .returns(Tuple::getBoolean, Row::getBoolean, false)
            .returns(Tuple::getValue, Row::getValue, false)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testDecodeBOOLArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Boolean", "ArrayDataType", Tuple::getBooleanArray, Row::getBooleanArray, true);
  }
}

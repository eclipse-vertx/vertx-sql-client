package io.reactiverse.pgclient.codec.simple;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.codec.ColumnChecker;
import io.reactiverse.pgclient.codec.SimpleQueryDataTypeCodecTestBase;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class CustomTypesTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testCustomType(TestContext ctx) {
    Async async = ctx.async();
    String expected = "Anytown";
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT (address).city FROM \"CustomDataType\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(2, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "city")
            .returns(Tuple::getValue, Row::getValue, expected)
            .returns(Tuple::getString, Row::getString, expected)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testDecodeCustomTypeArray(TestContext ctx) {
    String [] addresses = new String [] {"(Anytown,\"Main St\",t)", "(Anytown,\"First St\",f)"};

    testDecodeXXXArray(ctx, "CustomType", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, addresses);
  }
}

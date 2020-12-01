package io.vertx.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class CustomTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testCustomType(TestContext ctx) {
    Async async = ctx.async();
    String expected = "Anytown";
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT (address).city FROM \"CustomDataType\"").execute(ctx.asyncAssertSuccess(result -> {
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

    testDecodeXXXArray(ctx, "CustomType", "ArrayDataType", Tuple::getArrayOfStrings, Row::getArrayOfStrings, addresses);
  }
}

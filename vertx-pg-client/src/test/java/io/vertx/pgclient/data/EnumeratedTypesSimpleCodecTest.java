package io.vertx.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class EnumeratedTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testEnum(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT \"currentMood\" FROM \"EnumDataType\" WHERE \"id\" = 5").execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "currentMood")
            .returns(Tuple::getValue, Row::getValue, "ok")
            .returns(Tuple::getString, Row::getString, "ok")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testDecodeENUMArray(TestContext ctx) {
    String [] moods = new String [] {"ok", "unhappy", "happy"};
    testDecodeXXXArray(ctx, "Enum", "ArrayDataType", Tuple::getArrayOfStrings, Row::getArrayOfStrings, moods);
  }
}

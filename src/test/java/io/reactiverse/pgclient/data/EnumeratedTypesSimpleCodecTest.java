package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class EnumeratedTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testEnum(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT \"currentMood\" FROM \"EnumDataType\" WHERE \"id\" = 5", ctx.asyncAssertSuccess(result -> {
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
    testDecodeXXXArray(ctx, "Enum", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, moods);
  }
}

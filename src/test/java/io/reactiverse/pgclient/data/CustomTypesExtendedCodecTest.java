package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class CustomTypesExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {
  @Test
  public void testEncodeCustomType(TestContext ctx) {
    Async async = ctx.async();
    String actual = "('Othercity',\" 'Second Ave'\",f)";
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CustomDataType\" SET \"address\" = $1  WHERE \"id\" = $2 RETURNING \"address\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addString("('Othercity', 'Second Ave', false)")
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.rowCount());
              Row row = result.iterator().next();
              ColumnChecker.checkColumn(0, "address")
                .returns(Tuple::getValue, Row::getValue, actual)
                .returns(Tuple::getString, Row::getString, actual)
                .forRow(row);
              async.complete();
            }));
        }));
    }));
  }
}

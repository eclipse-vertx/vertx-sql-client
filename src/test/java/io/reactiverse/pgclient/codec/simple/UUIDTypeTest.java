package io.reactiverse.pgclient.codec.simple;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.codec.ColumnChecker;
import io.reactiverse.pgclient.codec.SimpleQueryDataTypeCodecTestBase;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.UUID;

public class UUIDTypeTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testUUID(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '50867d3d-0098-4f61-bd31-9309ebf53475'::UUID \"uuid\"", ctx.asyncAssertSuccess(result -> {
          UUID uuid = UUID.fromString("50867d3d-0098-4f61-bd31-9309ebf53475");
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "uuid")
            .returns(Tuple::getValue, Row::getValue, uuid)
            .returns(Tuple::getUUID, Row::getUUID, uuid)
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testDecodeUUIDArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "UUID", "ArrayDataType", Tuple::getUUIDArray, Row::getUUIDArray, uuid);
  }
}

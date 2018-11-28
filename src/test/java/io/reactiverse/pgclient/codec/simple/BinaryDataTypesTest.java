package io.reactiverse.pgclient.codec.simple;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.codec.ColumnChecker;
import io.reactiverse.pgclient.codec.SimpleQueryDataTypeCodecTestBase;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class BinaryDataTypesTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testBytea(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '12345678910'::BYTEA \"Buffer1\", '\u00DE\u00AD\u00BE\u00EF'::BYTEA \"Buffer2\"", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ColumnChecker.checkColumn(0, "Buffer1")
          .returns(Tuple::getValue, Row::getValue, Buffer.buffer("12345678910"))
          .returns(Tuple::getBuffer, Row::getBuffer, Buffer.buffer("12345678910"))
          .forRow(row);
        ColumnChecker.checkColumn(1, "Buffer2")
          .returns(Tuple::getValue, Row::getValue, Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"))
          .returns(Tuple::getBuffer, Row::getBuffer, Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"))
          .forRow(row);
        async.complete();
      }));
    }));
  }

  @Test
  public void testDecodeBYTEAArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Bytea", "ArrayDataType", Tuple::getBufferArray, Row::getBufferArray, Buffer.buffer("HELLO"));
  }
}

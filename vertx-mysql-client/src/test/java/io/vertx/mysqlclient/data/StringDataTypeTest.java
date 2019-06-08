package io.vertx.mysqlclient.data;

import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.sqlclient.Row;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class StringDataTypeTest extends MySQLDataTypeTestBase {
  @Test
  public void testBinaryDecodeAll(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT * FROM datatype WHERE id = 1", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(1, row.getValue(0));
        ctx.assertEquals(Buffer.buffer("HELLO"), row.getValue(1));
        ctx.assertEquals(Buffer.buffer("HELLO, WORLD"), row.getValue(2));
        ctx.assertEquals(Buffer.buffer("TINYBLOB"), row.getValue(3));
        ctx.assertEquals(Buffer.buffer("BLOB"), row.getValue(4));
        ctx.assertEquals(Buffer.buffer("MEDIUMBLOB"), row.getValue(5));
        ctx.assertEquals(Buffer.buffer("LONGBLOB"), row.getValue(6));
        ctx.assertEquals("TINYTEXT", row.getValue(7));
        ctx.assertEquals("TEXT", row.getValue(8));
        ctx.assertEquals("MEDIUMTEXT", row.getValue(9));
        ctx.assertEquals("LONGTEXT", row.getValue(10));
        conn.close();
      }));
    }));
  }

  @Test
  public void testTextDecodeBinary(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "Binary", Buffer.buffer("HELLO"));
  }

  @Test
  public void testBinaryDecodeBinary(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "Binary", Buffer.buffer("HELLO"));
  }

  @Test
  public void testBinaryEncodeBinary(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "Binary", Buffer.buffer("HELLO"));
  }

  @Test
  public void testBinaryEncodeVarBinary(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "VarBinary", Buffer.buffer("HELLO, WORLD"));
  }

  @Test
  public void testTextDecodeVarBinary(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "VarBinary", Buffer.buffer("HELLO, WORLD"));
  }

  @Test
  public void testBinaryDecodeVarBinary(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "VarBinary", Buffer.buffer("HELLO, WORLD"));
  }

  @Test
  public void testBinaryEncodeTinyBlob(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "TinyBlob", Buffer.buffer("TINYBLOB"));
  }

  @Test
  public void testTextDecodeTinyBlob(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "TinyBlob", Buffer.buffer("TINYBLOB"));
  }

  @Test
  public void testBinaryDecodeTinyBlob(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "TinyBlob", Buffer.buffer("TINYBLOB"));
  }

  @Test
  public void testBinaryEncodeBlob(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "Blob", Buffer.buffer("BLOB"));
  }

  @Test
  public void testTextDecodeBlob(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "Blob", Buffer.buffer("BLOB"));
  }

  @Test
  public void testBinaryDecodeBlob(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "Blob", Buffer.buffer("BLOB"));
  }

  @Test
  public void testBinaryEncodeMediumBlob(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "MediumBlob", Buffer.buffer("MEDIUMBLOB"));
  }

  @Test
  public void testTextDecodeMediumBlob(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "MediumBlob", Buffer.buffer("MEDIUMBLOB"));
  }

  @Test
  public void testBinaryDecodeMediumBlob(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "MediumBlob", Buffer.buffer("MEDIUMBLOB"));
  }

  @Test
  public void testBinaryEncodeLongBlob(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "LongBlob", Buffer.buffer("LONGBLOB"));
  }

  @Test
  public void testTextDecodeLongBlob(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "LongBlob", Buffer.buffer("LONGBLOB"));
  }

  @Test
  public void testBinaryDecodeLongBlob(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "LongBlob", Buffer.buffer("LONGBLOB"));
  }

  @Test
  public void testBinaryEncodeTinyText(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "TinyText", "TINYTEXT");
  }

  @Test
  public void testTextDecodeTinyText(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "TinyText", "TINYTEXT");
  }

  @Test
  public void testBinaryDecodeTinyText(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "TinyText", "TINYTEXT");
  }

  @Test
  public void testBinaryEncodeText(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "Text", "TEXT");
  }

  @Test
  public void testTextDecodeText(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "Text", "TEXT");
  }

  @Test
  public void testBinaryDecodeText(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "Text", "TEXT");
  }

  @Test
  public void testBinaryEncodeMediumText(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "MediumText", "MEDIUMTEXT");
  }

  @Test
  public void testTextDecodeMediumText(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "MediumText", "MEDIUMTEXT");
  }

  @Test
  public void testBinaryDecodeMediumText(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "MediumText", "MEDIUMTEXT");
  }

  @Test
  public void testBinaryEncodeLongText(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "LongText", "LONGTEXT");
  }

  @Test
  public void testTextDecodeLongText(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "LongText", "LONGTEXT");
  }

  @Test
  public void testBinaryDecodeLongText(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "LongText", "LONGTEXT");
  }
}

package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class BinaryDataTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
  private final static String hexFormat = "hex";
  private final static String escapeFormat = "escape";

  @Test
  public void testByteaHexFormat1(TestContext ctx) {
    testBytea(ctx, hexFormat, "12345678910", "Buffer1", Buffer.buffer("12345678910"));
  }

  @Test
  public void testByteaHexFormat2(TestContext ctx) {
    testBytea(ctx, hexFormat, "\u00DE\u00AD\u00BE\u00EF", "Buffer2", Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"));
  }

  @Test
  public void testByteaEscapeFormat1(TestContext ctx) {
    testBytea(ctx, escapeFormat, "\\\\001\\\\007", "Buffer3", Buffer.buffer(new byte[]{'\\', '\\', '0', '0', '1', '\\', '\\', '0', '0', '7'}));
  }

  @Test
  public void testByteaEscapeFormat2(TestContext ctx) {
    testBytea(ctx, escapeFormat, "\\001\\007", "Buffer4", Buffer.buffer(new byte[]{'\\', '0', '0', '1', '\\', '0', '0', '7'}));
  }

  @Test
  public void testByteaEscapeFormat3(TestContext ctx) {
    testBytea(ctx, escapeFormat, "abc \\153\\154\\155 \\052\\251\\124", "Buffer5", Buffer.buffer(new byte[]{'a', 'b', 'c', ' ', 'k', 'l', 'm', ' ', '*', '\\', '2', '5', '1', 'T'}));
  }

  private void testBytea(TestContext ctx, String byteaFormat, String binaryStr, String columnName, Buffer expected) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET bytea_output = '" + byteaFormat + "'", ctx.asyncAssertSuccess(v -> {
        conn.query("SELECT '" + binaryStr + "' :: BYTEA" + " \"" + columnName + "\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, columnName)
            .returns(Tuple::getValue, Row::getValue, expected)
            .returns(Tuple::getBuffer, Row::getBuffer, expected)
            .forRow(row);
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testDecodeHexByteaArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY [decode('48454c4c4f', 'hex') :: BYTEA]", "BufferArray", Tuple::getBufferArray, Row::getBufferArray, Buffer.buffer("HELLO"));
  }

  @Test
  public void testDecodeEscapeByteaArray(TestContext ctx) {
    testByteaArray(ctx, escapeFormat, "abc \\153\\154\\155 \\052\\251\\124", "BufferArray2", new Buffer[]{Buffer.buffer(new byte[]{'a', 'b', 'c', ' ', 'k', 'l', 'm', ' ', '*', '\\', '2', '5', '1', 'T'})});
  }

  private void testByteaArray(TestContext ctx, String byteaFormat, String binaryStr, String columnName, Buffer[] expected) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET bytea_output = '" + byteaFormat + "'", ctx.asyncAssertSuccess(v -> {
        conn.query("SELECT ARRAY ['" + binaryStr + "' :: BYTEA]" + " \"" + columnName + "\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, columnName)
            .returns(Tuple::getValue, Row::getValue, expected)
            .returns(Tuple::getBufferArray, Row::getBufferArray, expected)
            .forRow(row);
          async.complete();
        }));
      }));
    }));
  }
}

package io.vertx.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
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
  public void testByteaEscapeBackslash(TestContext ctx) {
    testBytea(ctx, escapeFormat, "\\\\\\134", "Buffer3", Buffer.buffer(new byte[]{0x5C, 0x5C}));
  }

  @Test
  public void testByteaEscapeNonPrintableOctets(TestContext ctx) {
    testBytea(ctx, escapeFormat, "\\001\\007", "Buffer4", Buffer.buffer(new byte[]{0x01, 0x07}));
  }

  @Test
  public void testByteaEscapePrintableOctets(TestContext ctx) {
    testBytea(ctx, escapeFormat, "123abc", "Buffer5", Buffer.buffer(new byte[]{'1', '2', '3', 'a', 'b', 'c'}));
  }

  @Test
  public void testByteaEscapeSingleQuote(TestContext ctx) {
    testBytea(ctx, escapeFormat, "\'\'", "Buffer6", Buffer.buffer(new byte[]{0x27}));
  }

  @Test
  public void testByteaEscapeZeroOctet(TestContext ctx) {
    testBytea(ctx, escapeFormat, "\\000", "Buffer7", Buffer.buffer(new byte[]{0x00}));
  }

  @Test
  public void testByteaEscapeFormat(TestContext ctx) {
    testBytea(ctx, escapeFormat, "abc \\153\\154\\155 \\052\\251\\124", "Buffer8", Buffer.buffer(new byte[]{'a', 'b', 'c', ' ', 'k', 'l', 'm', ' ', '*', (byte) 0xA9, 'T'}));
  }

  @Test
  public void testByteaEmptyString(TestContext ctx) {
    testDecodeGeneric(ctx, "", "BYTEA", "Buffer9", Tuple::getBuffer, Row::getBuffer, Buffer.buffer(""));
  }

  private void testBytea(TestContext ctx, String byteaFormat, String binaryStr, String columnName, Buffer expected) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
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
    testByteaArray(ctx, escapeFormat, "abc \\153\\154\\155 \\052\\251\\124", "BufferArray2", new Buffer[]{Buffer.buffer(new byte[]{'a', 'b', 'c', ' ', 'k', 'l', 'm', ' ', '*', (byte) 0xA9, 'T'})});
  }

  private void testByteaArray(TestContext ctx, String byteaFormat, String binaryStr, String columnName, Buffer[] expected) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
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

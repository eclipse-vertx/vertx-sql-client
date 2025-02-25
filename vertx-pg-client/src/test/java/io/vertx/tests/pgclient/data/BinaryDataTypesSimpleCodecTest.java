package io.vertx.tests.pgclient.data;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

public class BinaryDataTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {

  @Test
  public void testByteaHexFormat1(TestContext ctx) {
    testDecodeGeneric(ctx, "12345678910", "BYTEA", "Buffer1", Tuple::getBuffer, Row::getBuffer, Buffer.buffer("12345678910"));
  }

  @Test
  public void testByteaHexFormat2(TestContext ctx) {
    testDecodeGeneric(ctx, "\u00DE\u00AD\u00BE\u00EF", "BYTEA", "Buffer2", Tuple::getBuffer, Row::getBuffer, Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"));
  }

  @Test
  public void testByteaEscapeBackslash(TestContext ctx) {
    testDecodeGeneric(ctx, "\\\\\\134", "BYTEA", "Buffer3", Tuple::getBuffer, Row::getBuffer, Buffer.buffer(new byte[]{0x5C, 0x5C}));
  }

  @Test
  public void testByteaEscapeNonPrintableOctets(TestContext ctx) {
    testDecodeGeneric(ctx, "\\001\\007", "BYTEA", "Buffer4", Tuple::getBuffer, Row::getBuffer, Buffer.buffer(new byte[]{0x01, 0x07}));
  }

  @Test
  public void testByteaEscapePrintableOctets(TestContext ctx) {
    testDecodeGeneric(ctx, "123abc", "BYTEA", "Buffer5", Tuple::getBuffer, Row::getBuffer, Buffer.buffer(new byte[]{'1', '2', '3', 'a', 'b', 'c'}));
  }

  @Test
  public void testByteaEscapeSingleQuote(TestContext ctx) {
    testDecodeGeneric(ctx, "\'\'", "BYTEA", "Buffer6", Tuple::getBuffer, Row::getBuffer, Buffer.buffer(new byte[]{0x27}));
  }

  @Test
  public void testByteaEscapeZeroOctet(TestContext ctx) {
    testDecodeGeneric(ctx, "\\000", "BYTEA", "Buffer7", Tuple::getBuffer, Row::getBuffer, Buffer.buffer(new byte[]{0x00}));
  }

  @Test
  public void testByteaEscapeFormat(TestContext ctx) {
    testDecodeGeneric(ctx, "abc \\153\\154\\155 \\052\\251\\124", "BYTEA", "Buffer8", Tuple::getBuffer, Row::getBuffer, Buffer.buffer(new byte[]{'a', 'b', 'c', ' ', 'k', 'l', 'm', ' ', '*', (byte) 0xA9, 'T'}));
  }

  @Test
  public void testByteaEmptyString(TestContext ctx) {
    testDecodeGeneric(ctx, "", "BYTEA", "Buffer9", Tuple::getBuffer, Row::getBuffer, Buffer.buffer(""));
  }

  @Test
  public void testDecodeHexByteaArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY [decode('48454c4c4f', 'hex') :: BYTEA]", "BufferArray", Tuple::getArrayOfBuffers, Row::getArrayOfBuffers, Buffer.buffer("HELLO"));
  }

  @Test
  public void testDecodeEscapeByteaArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY [decode('abc \\153\\154\\155 \\052\\251\\124', 'escape') :: BYTEA]", "BufferArray2", Tuple::getArrayOfBuffers, Row::getArrayOfBuffers, Buffer.buffer(new byte[]{'a', 'b', 'c', ' ', 'k', 'l', 'm', ' ', '*', (byte) 0xA9, 'T'}));
  }
}

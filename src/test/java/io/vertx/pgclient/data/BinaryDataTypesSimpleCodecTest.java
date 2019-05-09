package io.vertx.pgclient.data;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class BinaryDataTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testBytea1(TestContext ctx) {
    testDecodeGeneric(ctx, "12345678910", "BYTEA", "Buffer1", Tuple::getBuffer, Row::getBuffer, Buffer.buffer("12345678910"));
  }

  @Test
  public void testBytea2(TestContext ctx) {
    testDecodeGeneric(ctx, "\u00DE\u00AD\u00BE\u00EF", "BYTEA", "Buffer2", Tuple::getBuffer, Row::getBuffer, Buffer.buffer("\u00DE\u00AD\u00BE\u00EF"));
  }

  @Test
  public void testDecodeBYTEAArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY [decode('48454c4c4f', 'hex') :: BYTEA]", "BufferArray", Tuple::getBufferArray, Row::getBufferArray, Buffer.buffer("HELLO"));
  }
}

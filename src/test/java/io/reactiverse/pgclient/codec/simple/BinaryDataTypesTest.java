package io.reactiverse.pgclient.codec.simple;

import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.codec.SimpleQueryDataTypeCodecTestBase;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class BinaryDataTypesTest extends SimpleQueryDataTypeCodecTestBase {
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

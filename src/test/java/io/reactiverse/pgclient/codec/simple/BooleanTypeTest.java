package io.reactiverse.pgclient.codec.simple;

import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.codec.SimpleQueryDataTypeCodecTestBase;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class BooleanTypeTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testBoolean1(TestContext ctx) {
    testDecodeGeneric(ctx, "true", "BOOLEAN", "TrueValue", Tuple::getBoolean, Row::getBoolean, true);
  }

  @Test
  public void testBoolean2(TestContext ctx) {
    testDecodeGeneric(ctx, "false", "BOOLEAN", "FalseValue", Tuple::getBoolean, Row::getBoolean, false);
  }

  @Test
  public void testDecodeBOOLArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Boolean", "ArrayDataType", Tuple::getBooleanArray, Row::getBooleanArray, true);
  }
}

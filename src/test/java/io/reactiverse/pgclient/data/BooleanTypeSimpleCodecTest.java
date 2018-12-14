package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class BooleanTypeSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
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
    testDecodeGenericArray(ctx, "ARRAY ['TRUE' :: BOOLEAN,'FALSE' :: BOOLEAN]", "BooleanArray", Tuple::getBooleanArray, Row::getBooleanArray, true, false);
  }
}

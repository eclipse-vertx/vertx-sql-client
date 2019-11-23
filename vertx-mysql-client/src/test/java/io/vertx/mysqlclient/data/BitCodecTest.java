package io.vertx.mysqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.sqlclient.Row;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ThreadLocalRandom;

@RunWith(VertxUnitRunner.class)
public class BitCodecTest extends MySQLDataTypeTestBase {
  @Test
  public void testTextDecodeBit(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "test_bit", 30L);
  }

  @Test
  public void testBinaryDecodeBit(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "test_bit", 30L);
  }

  @Test
  public void testBinaryEncodeBit(TestContext ctx) {
    StringBuilder binaryStringBuilder = new StringBuilder();
    for (int i = 0; i < 50; i++) {
      binaryStringBuilder.append(ThreadLocalRandom.current().nextInt(2));
    }
    Long expected = Long.parseLong(binaryStringBuilder.toString(), 2);
    testBinaryEncodeGeneric(ctx, "test_bit", expected);
  }

  @Test
  public void testBinaryEncodeZero(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_bit", 0L);
  }

  @Test
  public void testBinaryEncodeBitMax(TestContext ctx) {
    long maxBitLimit = -1L; // max value of uint_64
    StringBuilder binaryStringBuilder = new StringBuilder();
    for (int i = 0; i < 64; i++) {
      binaryStringBuilder.append(1);
    }
    ctx.assertEquals(binaryStringBuilder.toString(), Long.toBinaryString(maxBitLimit));
    testBinaryEncodeGeneric(ctx, "test_bit", maxBitLimit);
  }
}

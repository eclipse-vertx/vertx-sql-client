package io.vertx.tests.pgclient.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.pgclient.impl.codec.DataType;
import io.vertx.pgclient.impl.codec.DataTypeCodec;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DataTypeCodecTest {

  @Test
  public void testDecodeTextArray() {
    assertTextArray("{foo}", "foo");
    assertTextArray("{foo,bar}", "foo", "bar");
    assertTextArray("{\"foo\",bar}", "foo", "bar");
    assertTextArray("{foo,\"bar\"}", "foo", "bar");
    assertTextArray("{foo,\"ba\\\"r\"}", "foo", "ba\"r");
    assertTextArray("{foo,\"bar\\\"\"}", "foo", "bar\"");
    assertTextArray("{foo,\"bar\\\\\"}", "foo", "bar\\");
  }

  private void assertTextArray(String data, String... expected) {
    ByteBuf buff = Unpooled.copiedBuffer(data, StandardCharsets.UTF_8);
    List<String> res = Arrays.asList((String[]) DataTypeCodec.decodeText(DataType.TEXT_ARRAY, 0, buff.readableBytes(), buff));
    assertEquals(Arrays.asList(expected), res);
  }
}

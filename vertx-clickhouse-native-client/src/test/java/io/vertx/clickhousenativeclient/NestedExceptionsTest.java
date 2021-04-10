package io.vertx.clickhousenativeclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseServerException;
import io.vertx.clickhouse.clickhousenative.impl.codec.PacketReader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;

public class NestedExceptionsTest {
  @Test
  public void checkExceptions() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.findAndRegisterModules();
    ByteBuf buf;
    try (InputStream is = NestedExceptionsTest.class.getResourceAsStream("/forged_nested_exception.yaml")) {
      Map<String, byte[]> map = mapper.readValue(is, Map.class);
      List<byte[]> queryAnswers = PacketUtil.filterServerBlocks(map);
      byte[][] arrays = PacketUtil.asPrimitiveByteArray(queryAnswers);
      buf = Unpooled.wrappedBuffer(arrays);
    }

    PooledByteBufAllocator allocator = new PooledByteBufAllocator();
    PacketReader rdr = new PacketReader(null, "none", Collections.emptyMap(), null);
    ClickhouseServerException exception = (ClickhouseServerException)rdr.receivePacket(allocator, buf);
    Assert.assertEquals("DB::Exception", exception.getName());
    ClickhouseServerException nested = (ClickhouseServerException) exception.getCause();
    Assert.assertNotNull(nested);
    Assert.assertEquals("DB::Dxception", nested.getName());
    Assert.assertNull(nested.getCause());
  }
}

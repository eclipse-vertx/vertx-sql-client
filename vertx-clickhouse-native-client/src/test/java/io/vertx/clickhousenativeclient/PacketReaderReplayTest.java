package io.vertx.clickhousenativeclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.PacketReader;
import net.jpountz.lz4.LZ4Factory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class PacketReaderReplayTest {
  private static final Map<String, String> PROPS = Collections.unmodifiableMap(buildProperties());
  private static final LZ4Factory LZ4_FACTORY = LZ4Factory.safeInstance();

  private final ByteBuf buf;

  public PacketReaderReplayTest(String replayFile, String fragmented, ByteBuf buf) {
    this.buf = buf;
  }

  @Parameterized.Parameters(name = "{0}({1})")
  public static Iterable<Object[]> dataForTest() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.findAndRegisterModules();
    List<Object[]> result = new ArrayList<>();

    int continuousOffset = 8;
    for (String replayFile : Arrays.asList("/insert_prepare.yaml", "/with_max_block_size_and_2_datablocks.yaml")) {
      try (InputStream is = PacketReaderReplayTest.class.getResourceAsStream(replayFile)) {
        Map<String, byte[]> map = mapper.readValue(is, Map.class);

        List<byte[]> queryAnswers = map.entrySet()
          .stream()
          .filter(packet -> !packet.getKey().startsWith("peer0_"))
          .map(Map.Entry::getValue)
          .collect(Collectors.toList());
        byte[][] arrays = asPrimitiveByteArray(queryAnswers);
        ByteBuf fragmentedByteBuf = Unpooled.wrappedBuffer(arrays);
        ByteBuf continuousBuf = Unpooled.wrappedBuffer(new byte[fragmentedByteBuf.readableBytes()])
          .writerIndex(0);
        fragmentedByteBuf.readBytes(continuousBuf);
        fragmentedByteBuf.readerIndex(0);
        ByteBuf continuousWithOffsetBuf = Unpooled.wrappedBuffer(new byte[fragmentedByteBuf.readableBytes() + continuousOffset],
          continuousOffset, fragmentedByteBuf.readableBytes())
          .writerIndex(0);
        fragmentedByteBuf.readBytes(continuousWithOffsetBuf);
        fragmentedByteBuf.readerIndex(0);


        result.add(new Object[]{replayFile, "fragmented", fragmentedByteBuf});
        result.add(new Object[]{replayFile, "continuous", continuousBuf});
        result.add(new Object[]{replayFile, "continuousWithOffset", continuousWithOffsetBuf});
      }
    }
    return result;
  }

  @After
  public void cleanup() {
    buf.release();
  }

  @Test
  public void doReplayTest() {
    PooledByteBufAllocator allocator = new PooledByteBufAllocator();
    String fullName = "Clickhouse jython-driver";

    //1st packet: server hello
    PacketReader rdr = new PacketReader(null, fullName, PROPS, LZ4_FACTORY);
    ClickhouseNativeDatabaseMetadata md = (ClickhouseNativeDatabaseMetadata)rdr.receivePacket(allocator, buf);

    do {
      rdr = new PacketReader(md, fullName, PROPS, LZ4_FACTORY);
      Object packet = rdr.receivePacket(allocator, buf);
    } while (!rdr.isEndOfStream() && buf.readableBytes() > 0);
  }

  private static Map<String, String> buildProperties() {
    Map<String, String> props = new HashMap<>();
    props.put(ClickhouseConstants.OPTION_CLIENT_NAME, "jython-driver");
    props.put(ClickhouseConstants.OPTION_COMPRESSOR, "lz4_safe");
    props.put(ClickhouseConstants.OPTION_INITIAL_HOSTNAME, "bhorse");
    return props;
  }

  private static byte[][] asPrimitiveByteArray(List<byte[]> src) {
    byte[][] ret = new byte[src.size()][];
    for (int i = 0; i < src.size(); ++i) {
      ret[i] = src.get(i);
    }
    return ret;
  }
}

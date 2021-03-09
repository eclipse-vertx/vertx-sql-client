package io.vertx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.ResourceLeakDetector;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.PacketReader;
import net.jpountz.lz4.LZ4Factory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReplayTest {
  public static void main(String[] args) throws IOException {
    ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.findAndRegisterModules();
    //File file = new File("/home/vladimir/projects/vertx-sql-client/vertx-clickhouse-native-client/src/test/resources/insert_prepare.yaml");
    File file = new File("/home/vladimir/projects/vertx-sql-client/vertx-clickhouse-native-client/src/test/resources/with_max_block_size_and_2_datablocks.yaml");
    Map<String, byte[]> map =
      mapper.readValue(file, Map.class);
    List<byte[]> queryAnswers = map.entrySet()
      .stream()
      .filter(packet -> !packet.getKey().startsWith("peer0_"))
      .map(Map.Entry::getValue)
      .collect(Collectors.toList());
    byte[][] arrays = asPrimitiveByteArray(queryAnswers);
    ByteBuf fragmentedByteBuf = Unpooled.wrappedBuffer(arrays);
    ByteBuf continuousBuf = Unpooled.wrappedBuffer(new byte[fragmentedByteBuf.readableBytes()]).writerIndex(0);
    fragmentedByteBuf.readBytes(continuousBuf);
    fragmentedByteBuf.readerIndex(0);

    Map<String, String> props = new HashMap<>();
    props.put(ClickhouseConstants.OPTION_CLIENT_NAME, "jython-driver");
    props.put(ClickhouseConstants.OPTION_COMPRESSOR, "lz4_safe");
    props.put(ClickhouseConstants.OPTION_INITIAL_HOSTNAME, "bhorse");

    PooledByteBufAllocator allocator = new PooledByteBufAllocator();
    String fullName = "Clickhouse jython-driver";
    LZ4Factory lz4Factory = LZ4Factory.safeInstance();

    //1st packet: server hello
    PacketReader rdr = new PacketReader(null, fullName, props, lz4Factory);
    ClickhouseNativeDatabaseMetadata md = (ClickhouseNativeDatabaseMetadata)rdr.receivePacket(allocator, continuousBuf);

    do {
      rdr = new PacketReader(md, fullName, props, lz4Factory);
      Object packet = rdr.receivePacket(allocator, continuousBuf);
    } while (!rdr.isEndOfStream() && continuousBuf.readableBytes() > 0);
  }

  private static byte[][] asPrimitiveByteArray(List<byte[]> src) {
    byte[][] ret = new byte[src.size()][];
    for (int i = 0; i < src.size(); ++i) {
      ret[i] = src.get(i);
    }
    return ret;
  }
}

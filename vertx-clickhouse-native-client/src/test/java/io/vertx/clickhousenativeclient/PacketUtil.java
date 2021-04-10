package io.vertx.clickhousenativeclient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PacketUtil {
  public static List<byte[]> filterServerBlocks(Map<String, byte[]> map) {
    return map.entrySet()
      .stream()
      .filter(packet -> !packet.getKey().startsWith("peer0_"))
      .map(Map.Entry::getValue)
      .collect(Collectors.toList());
  }

  public static byte[][] asPrimitiveByteArray(List<byte[]> src) {
    byte[][] ret = new byte[src.size()][];
    for (int i = 0; i < src.size(); ++i) {
      ret[i] = src.get(i);
    }
    return ret;
  }
}

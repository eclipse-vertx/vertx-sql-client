package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class MultistringMessageReader {
  private final List<String> strings;
  private Integer stringsExpected;

  public MultistringMessageReader() {
    strings = new ArrayList<>();
  }

  public List<String> readFrom(ByteBuf in, ServerPacketType packetType) {
    if (stringsExpected == null) {
      stringsExpected = stringsInMessage(packetType);
    }
    String ln;
    while (strings.size() < stringsExpected && (ln = ByteBufUtils.readPascalString(in)) != null) {
      strings.add(ln);
    }
    if (strings.size() == stringsExpected) {
      return strings;
    }
    return null;
  }

  private int stringsInMessage(ServerPacketType type) {
    if (type == ServerPacketType.TABLE_COLUMNS) {
      return 2;
    }
    return 0;
  }
}

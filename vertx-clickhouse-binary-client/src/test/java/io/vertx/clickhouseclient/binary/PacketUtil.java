/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouseclient.binary;

import org.testcontainers.shaded.org.bouncycastle.util.Pack;

import java.util.*;
import java.util.stream.Collectors;

public class PacketUtil {
  public static List<byte[]> filterServerBlocks(Map<String, ?> src) {
    if (src.containsKey("packets")) {
      List<Map<String, ?>> packetsMaps = (List<Map<String, ?>>) src.get("packets");
      List<Packet> packets = new ArrayList<>();
      for (Map<String, ?> packetFields : packetsMaps) {
        Packet packet = new Packet((int) packetFields.get("peer"), (int) packetFields.get("index"), (byte[]) packetFields.get("data"));
        packets.add(packet);
      }
      packets.sort(Comparator.comparing(Packet::index));
      List<byte[]> peer0Packets =
        packets.stream()
          .filter(pckt -> pckt.peer() == 1)
          .map(Packet::data)
          .collect(Collectors.toList());
      return peer0Packets;
    } else {
      Map<String, byte[]> packets = (Map<String, byte[]>)src;
      return packets.entrySet()
        .stream()
        .filter(packet -> !packet.getKey().startsWith("peer0_"))
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());
    }
  }

  public static byte[][] asPrimitiveByteArray(List<byte[]> src) {
    byte[][] ret = new byte[src.size()][];
    for (int i = 0; i < src.size(); ++i) {
      ret[i] = src.get(i);
    }
    return ret;
  }
}


class Packet {
  private final int peer;
  private final int index;
  private final byte[] data;

  Packet(int peer, int index, byte[] data) {
    this.peer = peer;
    this.index = index;
    this.data = data;
  }

  public int peer() {
    return peer;
  }

  public int index() {
    return index;
  }

  public byte[] data() {
    return data;
  }
}

package io.vertx.mysqlclient.impl.codec;

final class Packets {
  static final int ERROR_PACKET_HEADER = 0xFF;
  static final int PACKET_PAYLOAD_LENGTH_LIMIT = 0xFFFFFF;
}

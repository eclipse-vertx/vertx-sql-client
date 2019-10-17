package io.vertx.mssqlclient.impl.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

public final class TdsPacket extends DefaultByteBufHolder {
  public static final int PACKET_HEADER_SIZE = 8;
  public static final int MAX_PACKET_DATA_SIZE = 0xFFFF - 8;

  private final MessageType type;
  private final MessageStatus status;
  private final int length;
  private final int processId;
  private final short packetId;

  private TdsPacket(MessageType type, MessageStatus status, int length, int processId, short packetId, ByteBuf data) {
    super(data);
    this.type = type;
    this.status = status;
    this.length = length;
    this.processId = processId;
    this.packetId = packetId;
  }

  public static TdsPacket newTdsPacket(MessageType type, MessageStatus status, int length, int processId, short packetId, ByteBuf data) {
    return new TdsPacket(type, status, length, processId, packetId, data);
  }

  public MessageType type() {
    return type;
  }

  public MessageStatus status() {
    return status;
  }

  public int length() {
    return length;
  }

  public int processId() {
    return processId;
  }

  public short packetId() {
    return packetId;
  }
}

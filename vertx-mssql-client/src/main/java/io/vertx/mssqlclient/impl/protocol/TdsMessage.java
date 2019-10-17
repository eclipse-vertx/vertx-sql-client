package io.vertx.mssqlclient.impl.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

public final class TdsMessage extends DefaultByteBufHolder {
  private final MessageType type;
  private final MessageStatus status;
  private final int processId;

  private TdsMessage(MessageType type, MessageStatus status, int processId, ByteBuf data) {
    super(data);
    this.type = type;
    this.status = status;
    this.processId = processId;
  }

  public static TdsMessage newTdsMessage(MessageType type, MessageStatus status, int processId, ByteBuf data) {
    return new TdsMessage(type, status, processId, data.retainedSlice());
  }

  public static TdsMessage newTdsMessageFromSinglePacket(TdsPacket tdsPacket) {
    ByteBuf packetData = tdsPacket.content().slice();
    return newTdsMessage(tdsPacket.type(), tdsPacket.status(), tdsPacket.processId(), packetData);
  }

  public MessageType type() {
    return type;
  }

  public MessageStatus status() {
    return status;
  }

  public int processId() {
    return processId;
  }
}

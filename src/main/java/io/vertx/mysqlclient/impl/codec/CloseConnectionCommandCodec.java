package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CloseConnectionCommand;

class CloseConnectionCommandCodec extends CommandCodec<Void, CloseConnectionCommand> {
  CloseConnectionCommandCodec(CloseConnectionCommand cmd) {
    super(cmd);
  }

  @Override
  void encodePayload(MySQLEncoder encoder) {
    super.encodePayload(encoder);
    ByteBuf packetBody = allocateBuffer();
    packetBody.writeByte(CommandType.COM_QUIT);
    sendPacketWithBody(packetBody);
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    // connection will be terminated later
  }
}

package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CloseConnectionCommand;

class CloseConnectionCommandCodec extends CommandCodec<Void, CloseConnectionCommand> {
  CloseConnectionCommandCodec(CloseConnectionCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendQuitCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    // connection will be terminated later
  }

  private void sendQuitCommand() {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId++);

    // encode packet payload
    packet.writeByte(CommandType.COM_QUIT);

    // set payload length
    int lenOfPayload = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, lenOfPayload);

    encoder.chctx.writeAndFlush(packet);
  }
}

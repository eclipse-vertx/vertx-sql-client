package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.command.InitDbCommand;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.nio.charset.StandardCharsets;

import static io.vertx.mysqlclient.impl.codec.Packets.*;
import static io.vertx.mysqlclient.impl.protocol.backend.OkPacket.OK_PACKET_HEADER;

class InitDbCommandCodec extends CommandCodec<Void, InitDbCommand> {
  InitDbCommandCodec(InitDbCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendInitDbCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    handleOkPacketOrErrorPacketPayload(payload);
  }

  private void sendInitDbCommand() {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_INIT_DB);
    packet.writeCharSequence(cmd.schemaName(), StandardCharsets.UTF_8);

    // set payload length
    int lenOfPayload = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, lenOfPayload);

    sendPacket(packet, lenOfPayload);
  }
}

package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.mysqlclient.impl.protocol.backend.OkPacket;
import io.vertx.sqlclient.impl.command.CloseCursorCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.nio.charset.StandardCharsets;

import static io.vertx.mysqlclient.impl.protocol.backend.ErrPacket.*;

public class ResetStatementCommandCodec extends CommandCodec<Void, CloseCursorCommand> {
  public ResetStatementCommandCodec(CloseCursorCommand cmd) {
    super(cmd);
  }

  @Override
  void encodePayload(MySQLEncoder encoder) {
    super.encodePayload(encoder);
    MySQLPreparedStatement ps = (MySQLPreparedStatement) cmd.statement();

    ps.isCursorOpen = false;

    ByteBuf packetBody = allocateBuffer();

    packetBody.writeByte(CommandType.COM_STMT_RESET);
    packetBody.writeIntLE((int) ps.statementId);

    sendPacketWithBody(packetBody);
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    int first = payload.getUnsignedByte(payload.readerIndex());
    if (first == ERROR_PACKET_HEADER) {
      handleErrorPacketPayload(payload);
    } else if (first == OkPacket.OK_PACKET_HEADER) {
      payload.readByte(); // skip header
      OkPacket okPacket = GenericPacketPayloadDecoder.decodeOkPacketBody(payload, StandardCharsets.UTF_8);
      completionHandler.handle(CommandResponse.success(null));
    }
  }
}

package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.mysqlclient.impl.protocol.backend.OkPacket;
import io.vertx.sqlclient.impl.command.CloseCursorCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

import static io.vertx.mysqlclient.impl.protocol.backend.ErrPacket.ERROR_PACKET_HEADER;

class ResetStatementCommandCodec extends CommandCodec<Void, CloseCursorCommand> {
  ResetStatementCommandCodec(CloseCursorCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    MySQLPreparedStatement ps = (MySQLPreparedStatement) cmd.statement();

    ps.isCursorOpen = false;

    encodePacket(payload -> {
      payload.writeByte(CommandType.COM_STMT_RESET);
      payload.writeIntLE((int) ps.statementId);
    });
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    int first = payload.getUnsignedByte(payload.readerIndex());
    if (first == ERROR_PACKET_HEADER) {
      handleErrorPacketPayload(payload);
    } else if (first == OkPacket.OK_PACKET_HEADER) {
      completionHandler.handle(CommandResponse.success(null));
    }
  }
}

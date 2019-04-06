package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.mysqlclient.impl.protocol.backend.OkPacket;
import io.vertx.sqlclient.impl.command.ClosePortalCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.nio.charset.StandardCharsets;

import static io.vertx.mysqlclient.impl.protocol.backend.ErrPacket.*;

public class ResetStatementCommandCodec extends CommandCodec<Void, ClosePortalCommand> {
  public ResetStatementCommandCodec(ClosePortalCommand cmd) {
    super(cmd);
  }

  @Override
  void encodePayload(MySQLEncoder encoder) {
    super.encodePayload(encoder);
    MySQLPreparedStatement ps = (MySQLPreparedStatement) cmd.ps();

    ByteBuf payload = encoder.chctx.alloc().ioBuffer();

    payload.writeByte(CommandType.COM_STMT_RESET);
    payload.writeIntLE((int) ps.statementId);

    encoder.writePacketAndFlush(sequenceId++, payload);
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

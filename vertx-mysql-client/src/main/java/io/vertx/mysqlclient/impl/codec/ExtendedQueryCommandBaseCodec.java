package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommandBase;

import java.nio.charset.StandardCharsets;

import static io.vertx.mysqlclient.impl.codec.Packets.*;

abstract class ExtendedQueryCommandBaseCodec<R, C extends ExtendedQueryCommandBase<R>> extends QueryCommandBaseCodec<R, C> {

  protected final MySQLPreparedStatement statement;

  ExtendedQueryCommandBaseCodec(C cmd) {
    super(cmd, DataFormat.BINARY);
    statement = (MySQLPreparedStatement) cmd.preparedStatement();
  }

  @Override
  protected void handleInitPacket(ByteBuf payload) {
    // may receive ERR_Packet, OK_Packet, Binary Protocol Resultset
    int firstByte = payload.getUnsignedByte(payload.readerIndex());
    if (firstByte == OK_PACKET_HEADER) {
      OkPacket okPacket = decodeOkPacketPayload(payload, StandardCharsets.UTF_8);
      handleSingleResultsetDecodingCompleted(okPacket.serverStatusFlags(), okPacket.affectedRows(), okPacket.lastInsertId());
    } else if (firstByte == ERROR_PACKET_HEADER) {
      handleErrorPacketPayload(payload);
    } else {
      handleResultsetColumnCountPacketBody(payload);
    }
  }

}

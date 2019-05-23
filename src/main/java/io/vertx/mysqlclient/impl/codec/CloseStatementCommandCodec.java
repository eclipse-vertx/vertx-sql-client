package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

public class CloseStatementCommandCodec extends CommandCodec<Void, CloseStatementCommand> {

  public CloseStatementCommandCodec(CloseStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encodePayload(MySQLEncoder encoder) {
    super.encodePayload(encoder);
    MySQLPreparedStatement ps = (MySQLPreparedStatement) cmd.statement();

    ByteBuf payload = encoder.chctx.alloc().ioBuffer();

    payload.writeByte(CommandType.COM_STMT_CLOSE);
    payload.writeIntLE((int) ps.statementId);

    encoder.writePacketAndFlush(sequenceId++, payload);

    completionHandler.handle(CommandResponse.success(null));
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    // no statement response
  }
}

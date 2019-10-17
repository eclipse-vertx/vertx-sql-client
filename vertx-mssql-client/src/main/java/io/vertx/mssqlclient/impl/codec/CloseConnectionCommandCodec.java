package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.vertx.sqlclient.impl.command.CloseConnectionCommand;

class CloseConnectionCommandCodec extends MSSQLCommandCodec<Void, CloseConnectionCommand> {
  CloseConnectionCommandCodec(CloseConnectionCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(TdsMessageEncoder encoder) {
    super.encode(encoder);
    encoder.chctx.channel().close();
  }

  @Override
  void decodeMessage(TdsMessage message, TdsMessageEncoder encoder) {
    // connection has been closed
  }
}

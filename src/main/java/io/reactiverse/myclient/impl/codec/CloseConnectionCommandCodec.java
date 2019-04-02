package io.reactiverse.myclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.reactiverse.myclient.impl.protocol.CommandType;
import io.reactiverse.sqlclient.impl.command.CloseConnectionCommand;

public class CloseConnectionCommandCodec extends CommandCodec<Void, CloseConnectionCommand> {
  public CloseConnectionCommandCodec(CloseConnectionCommand cmd) {
    super(cmd);
  }

  @Override
  void encodePayload(MyEncoder encoder) {
    super.encodePayload(encoder);
    ByteBuf payload = encoder.chctx.alloc().ioBuffer();
    payload.writeByte(CommandType.COM_QUIT);
    encoder.writePacketAndFlush(sequenceId++, payload);
  }

  @Override
  void decodePayload(ByteBuf payload, MyEncoder encoder, int payloadLength, int sequenceId) {
    // connection will be terminated later
  }
}

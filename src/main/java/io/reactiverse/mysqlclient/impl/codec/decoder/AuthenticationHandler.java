package io.reactiverse.mysqlclient.impl.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.reactiverse.mysqlclient.impl.MySQLExceptionFactory;
import io.reactiverse.mysqlclient.impl.MySQLSocketConnection;
import io.reactiverse.mysqlclient.impl.codec.GenericPacketPayloadDecoder;
import io.reactiverse.mysqlclient.impl.codec.MySQLPacketDecoder;
import io.vertx.core.Future;

import java.nio.charset.Charset;
import java.util.List;

import static io.reactiverse.mysqlclient.impl.protocol.backend.ErrPacket.*;
import static io.reactiverse.mysqlclient.impl.protocol.backend.OkPacket.*;

public class AuthenticationHandler extends MySQLPacketDecoder {
  private final Future<Void> authenticationFuture;

  public AuthenticationHandler(Charset charset, MySQLSocketConnection socketConnection, Future<Void> authenticationFuture) {
    super(charset, socketConnection);
    this.authenticationFuture = authenticationFuture;
  }

  @Override
  protected void decodePayload(ChannelHandlerContext ctx, ByteBuf payload, int payloadLength, int sequenceId, List<Object> out) {
    //TODO auth switch support
    int header = payload.readUnsignedByte();
    switch (header) {
      case OK_PACKET_HEADER:
        authenticationFuture.complete();
        break;
      case ERROR_PACKET_HEADER:
        authenticationFuture.fail(MySQLExceptionFactory.throwNewException(GenericPacketPayloadDecoder.decodeErrPacketBody(payload, charset)));
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }
}

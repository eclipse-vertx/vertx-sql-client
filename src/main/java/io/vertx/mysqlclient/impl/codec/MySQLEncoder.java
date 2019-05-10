package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.vertx.mysqlclient.impl.CharacterSetMapping;
import io.vertx.mysqlclient.impl.protocol.frontend.HandshakeResponse;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.mysqlclient.impl.util.Native41Authenticator;
import io.vertx.sqlclient.impl.command.CloseConnectionCommand;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import io.vertx.sqlclient.impl.command.InitCommand;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;

import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.CLIENT_CONNECT_ATTRS;
import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.CLIENT_CONNECT_WITH_DB;
import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.CLIENT_PLUGIN_AUTH;
import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA;
import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.CLIENT_SECURE_CONNECTION;

public class MySQLEncoder extends MessageToByteEncoder<CommandBase<?>> {

  private final ArrayDeque<CommandCodec<?, ?>> inflight;
  ChannelHandlerContext chctx;

  MySQLEncoder(ArrayDeque<CommandCodec<?, ?>> inflight) {
    this.inflight = inflight;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    chctx = ctx;
  }

  @Override
  protected void encode(ChannelHandlerContext chctx, CommandBase<?> msg, ByteBuf out) {
    CommandCodec<?, ?> ctx = wrap(msg);
    ctx.completionHandler = resp -> {
      CommandCodec c = inflight.poll();
      resp.cmd = (CommandBase) c.cmd;
      chctx.fireChannelRead(resp);
    };
    inflight.add(ctx);
    ctx.encodePayload(this);
  }

  private CommandCodec<?, ?> wrap(CommandBase<?> cmd) {
    if (cmd instanceof InitCommand) {
      return new InitCommandCodec((InitCommand) cmd);
    } else if (cmd instanceof SimpleQueryCommand) {
      return new SimpleQueryCommandCodec((SimpleQueryCommand) cmd);
    } else if (cmd instanceof ExtendedQueryCommand) {
      return new ExtendedQueryCommandCodec((ExtendedQueryCommand) cmd);
    } else if (cmd instanceof CloseConnectionCommand) {
      return new CloseConnectionCommandCodec((CloseConnectionCommand) cmd);
    } else if (cmd instanceof PrepareStatementCommand) {
      return new PrepareStatementCodec((PrepareStatementCommand) cmd);
    } else {
      System.out.println("Unsupported command " + cmd);
      throw new UnsupportedOperationException("Todo");
    }
  }

  void writeHandshakeResponseMessage(int sequenceId, HandshakeResponse message) {
    ByteBuf payload = chctx.alloc().ioBuffer();

    int clientCapabilitiesFlags = message.getClientCapabilitiesFlags();
    payload.writeIntLE(message.getClientCapabilitiesFlags());
    payload.writeIntLE(message.getMaxPacketSize());
    payload.writeByte(CharacterSetMapping.getCharsetByteValue(message.getCharset().name()));
    byte[] filler = new byte[23];
    payload.writeBytes(filler);
    BufferUtils.writeNullTerminatedString(payload, message.getUsername(), StandardCharsets.UTF_8);
    String password = message.getPassword();
    if (password == null || password.isEmpty()) {
      payload.writeByte(0);
    } else {
      //TODO support different auth methods here

      byte[] scrambledPassword = Native41Authenticator.encode(message.getPassword(), StandardCharsets.UTF_8, message.getScramble());
      if ((clientCapabilitiesFlags & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0) {
        BufferUtils.writeLengthEncodedInteger(payload, scrambledPassword.length);
        payload.writeBytes(scrambledPassword);
      } else if ((clientCapabilitiesFlags & CLIENT_SECURE_CONNECTION) != 0) {
        payload.writeByte(scrambledPassword.length);
        payload.writeBytes(scrambledPassword);
      } else {
        payload.writeByte(0);
      }
    }
    if ((clientCapabilitiesFlags & CLIENT_CONNECT_WITH_DB) != 0) {
      BufferUtils.writeNullTerminatedString(payload, message.getDatabase(), StandardCharsets.UTF_8);
    }
    if ((clientCapabilitiesFlags & CLIENT_PLUGIN_AUTH) != 0) {
      BufferUtils.writeNullTerminatedString(payload, message.getAuthMethodName(), StandardCharsets.UTF_8);
    }
    if ((clientCapabilitiesFlags & CLIENT_CONNECT_ATTRS) != 0) {
      ByteBuf kv = chctx.alloc().ioBuffer();
      try {
        message.getClientConnectAttrs().forEach((key, value) -> {
          BufferUtils.writeLengthEncodedString(kv, key, StandardCharsets.UTF_8);
          BufferUtils.writeLengthEncodedString(kv, value, StandardCharsets.UTF_8);
        });
        BufferUtils.writeLengthEncodedInteger(payload, kv.readableBytes());
        payload.writeBytes(kv);
      } finally {
        kv.release();
      }
    }

    writePacketAndFlush(sequenceId, payload);
  }

  void writePacketAndFlush(int sequenceId, ByteBuf payload) {
    ByteBuf header = chctx.alloc().ioBuffer();

    //TODO fragment the packet to avoid 16MB+ packet here ?

    // payload length
    header.writeMediumLE(payload.readableBytes());
    // sequence ID
    header.writeByte(sequenceId);

    chctx.write(header);
    chctx.writeAndFlush(payload);
  }
}

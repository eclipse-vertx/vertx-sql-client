package io.reactiverse.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.reactiverse.mysqlclient.impl.CharacterSetMapping;
import io.reactiverse.mysqlclient.impl.codec.datatype.DataTypeCodec;
import io.reactiverse.mysqlclient.impl.protocol.CommandType;
import io.reactiverse.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.reactiverse.mysqlclient.impl.protocol.frontend.HandshakeResponse;
import io.reactiverse.mysqlclient.impl.util.BufferUtils;
import io.reactiverse.mysqlclient.impl.util.Native41Authenticator;
import io.reactiverse.pgclient.Tuple;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static io.reactiverse.mysqlclient.impl.protocol.CapabilitiesFlag.*;

public class MySQLPacketEncoder {
  private final AtomicInteger sequenceIdCounter;
  private final ChannelHandlerContext ctx;
  private final Charset charset;

  public MySQLPacketEncoder(Charset charset, AtomicInteger sequenceIdCounter, ChannelHandlerContext ctx) {
    this.charset = charset;
    this.sequenceIdCounter = sequenceIdCounter;
    this.ctx = ctx;
  }

  /**
   * Calculate the payload length and write a full MySQL packet
   *
   * @param payload payload of the packet
   */
  private void writePacketAndFlush(ByteBuf payload) {
    writePacketAndFlush(payload, ctx.voidPromise());
  }

  private void writePacketAndFlush(ByteBuf payload, ChannelPromise channelPromise) {
    ByteBuf header = ctx.alloc().ioBuffer();

    //TODO fragment the packet to avoid 16MB+ packet here ?

    // payload length
    header.writeMediumLE(payload.readableBytes());
    // sequence ID
    header.writeByte(sequenceIdCounter.getAndIncrement());

    ctx.channel().eventLoop().execute(() -> {
      ctx.write(header);
      ctx.writeAndFlush(payload, channelPromise);
    });
  }

  public void writeHandshakeResponseMessage(HandshakeResponse message, ChannelPromise channelPromise) {
    ByteBuf payload = ctx.alloc().ioBuffer();

    int clientCapabilitiesFlags = message.getClientCapabilitiesFlags();
    payload.writeIntLE(message.getClientCapabilitiesFlags());
    payload.writeIntLE(message.getMaxPacketSize());
    payload.writeByte(CharacterSetMapping.getCharsetByteValue(message.getCharset().name()));
    byte[] filler = new byte[23];
    payload.writeBytes(filler);
    BufferUtils.writeNullTerminatedString(payload, message.getUsername(), charset);
    String password = message.getPassword();
    if (password == null || password.isEmpty()) {
      payload.writeByte(0);
    } else {
      //TODO support different auth methods here

      byte[] scrambledPassword = Native41Authenticator.encode(message.getPassword(), charset, message.getScramble());
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
      BufferUtils.writeNullTerminatedString(payload, message.getDatabase(), charset);
    }
    if ((clientCapabilitiesFlags & CLIENT_PLUGIN_AUTH) != 0) {
      BufferUtils.writeNullTerminatedString(payload, message.getAuthMethodName(), StandardCharsets.UTF_8);
    }
    if ((clientCapabilitiesFlags & CLIENT_CONNECT_ATTRS) != 0) {
      ByteBuf kv = ctx.alloc().ioBuffer();
      try {
        message.getClientConnectAttrs().forEach((key, value) -> {
          BufferUtils.writeLengthEncodedString(kv, key, charset);
          BufferUtils.writeLengthEncodedString(kv, value, charset);
        });
        BufferUtils.writeLengthEncodedInteger(payload, kv.readableBytes());
        payload.writeBytes(kv);
      } finally {
        kv.release();
      }
    }

    writePacketAndFlush(payload, channelPromise);
  }

  public void writePingMessage() {
    ByteBuf payload = ctx.alloc().ioBuffer();

    payload.writeByte(CommandType.COM_PING);

    writePacketAndFlush(payload);
  }

  public void writeQueryMessage(String sql) {
    ByteBuf payload = ctx.alloc().ioBuffer();

    payload.writeByte(CommandType.COM_QUERY);
    payload.writeCharSequence(sql, charset);

    writePacketAndFlush(payload);
  }

  public void writePrepareMessage(String sql) {
    ByteBuf payload = ctx.alloc().ioBuffer();

    payload.writeByte(CommandType.COM_STMT_PREPARE);
    payload.writeCharSequence(sql, charset);

    writePacketAndFlush(payload);
  }

  public void writeExecuteMessage(long statementId, ColumnDefinition[] paramsColumnDefinitions, byte sendType, Tuple params) {
    ByteBuf payload = ctx.alloc().ioBuffer();

    payload.writeByte(CommandType.COM_STMT_EXECUTE);
    payload.writeIntLE((int) statementId);
    // CURSOR_TYPE_NO_CURSOR
    payload.writeByte(0x00);
    // iteration count, always 1
    payload.writeIntLE(1);

    int numOfParams = paramsColumnDefinitions.length;
    int bitmapLength = (numOfParams + 7) / 8;
    byte[] nullBitmap = new byte[bitmapLength];

    int pos = payload.writerIndex();

    if (numOfParams > 0) {
      // write a dummy bitmap first
      payload.writeBytes(nullBitmap);
      payload.writeByte(sendType);
      if (sendType == 1) {
        for (int i = 0; i < numOfParams; i++) {
          Object value = params.getValue(i);
          if (value != null) {
            payload.writeByte(paramsColumnDefinitions[i].getType().id);
          } else {
            payload.writeByte(ColumnDefinition.ColumnType.MYSQL_TYPE_NULL);
          }
          // TODO handle parameter flag (unsigned or signed)
          payload.writeByte(0);
        }

        for (int i = 0; i < numOfParams; i++) {
          Object value = params.getValue(i);
          //FIXME make sure we have correctly handled null value here
          if (value != null) {
            DataTypeCodec.encodeBinary(paramsColumnDefinitions[i].getType(), value, payload);
          } else {
            nullBitmap[i / 8] |= (1 << (i & 7));
          }
        }

      }

      // padding null-bitmap content
      payload.setBytes(pos, nullBitmap);
    }

    writePacketAndFlush(payload);
  }
}

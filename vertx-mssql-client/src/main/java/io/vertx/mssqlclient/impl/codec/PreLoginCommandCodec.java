/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.impl.command.PreLoginCommand;
import io.vertx.mssqlclient.impl.protocol.MessageStatus;
import io.vertx.mssqlclient.impl.protocol.MessageType;
import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.vertx.mssqlclient.impl.protocol.client.prelogin.EncryptionOptionToken;
import io.vertx.mssqlclient.impl.protocol.client.prelogin.OptionToken;
import io.vertx.mssqlclient.impl.protocol.client.prelogin.VersionOptionToken;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.util.List;

class PreLoginCommandCodec extends MSSQLCommandCodec<Void, PreLoginCommand> {

  PreLoginCommandCodec(PreLoginCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(TdsMessageEncoder encoder) {
    super.encode(encoder);
    sendPreLoginMessage();
  }

  @Override
  void decodeMessage(TdsMessage message, TdsMessageEncoder encoder) {
    // nothing to do for now?
    completionHandler.handle(CommandResponse.success(null));
  }

  private void sendPreLoginMessage() {
    ChannelHandlerContext chctx = encoder.chctx;

    ByteBuf packet = chctx.alloc().ioBuffer();

    // packet header
    packet.writeByte(MessageType.PRE_LOGIN.value());
    packet.writeByte(MessageStatus.NORMAL.value() | MessageStatus.END_OF_MESSAGE.value());
    int packetLenIdx = packet.writerIndex();
    packet.writeShort(0); // set length later
    packet.writeShort(0x00);
    packet.writeByte(0x00); // FIXME packet ID
    packet.writeByte(0x00);

    // packet data
    int packetDataStartIdx = packet.writerIndex();

    List<OptionToken> optionTokens = cmd.optionTokens();

    int payloadStartIdx = packet.writerIndex();

    int totalLengthOfOptionsData = 0;

    /*
      We first predefine positions of the option token offset length,
      then set the offset lengths by calculating ByteBuf writer indexes diff later.
     */

    // predefined positions to store the ByteBuf writer index
    int versionOptionTokenOffsetLengthIdx = 0;
    int encryptionOptionTokenOffsetLengthIdx = 0;

    // option token header
    for (OptionToken token : optionTokens) {
      totalLengthOfOptionsData += token.optionLength();
      packet.writeByte(token.tokenType());
      switch (token.tokenType()) {
        case VersionOptionToken.TYPE:
          versionOptionTokenOffsetLengthIdx = packet.writerIndex();
          break;
        case EncryptionOptionToken.TYPE:
          encryptionOptionTokenOffsetLengthIdx = packet.writerIndex();
          break;
        default:
          throw new IllegalStateException("Unexpected token type");
      }
      packet.writeShort(0x00);
      packet.writeShort(token.optionLength());
    }

    // terminator token
    packet.writeByte(0xFF);

    // option token data
    for (OptionToken token : optionTokens) {
      encodeTokenData(token, packet);
    }

    // calculate Option offset
    int totalLengthOfPayload = packet.writerIndex() - payloadStartIdx;
    int offsetStart = totalLengthOfPayload - totalLengthOfOptionsData;

    for (OptionToken token : optionTokens) {
      switch (token.tokenType()) {
        case VersionOptionToken.TYPE:
          packet.setShort(versionOptionTokenOffsetLengthIdx, offsetStart);
          offsetStart += token.optionLength();
          break;
        case EncryptionOptionToken.TYPE:
          packet.setShort(encryptionOptionTokenOffsetLengthIdx, offsetStart);
          offsetStart += token.optionLength();
          break;
        default:
          throw new IllegalStateException("Unexpected token type");
      }
    }

    int packetLen = packet.writerIndex() - packetDataStartIdx + 8;
    packet.setShort(packetLenIdx, packetLen);

    chctx.writeAndFlush(packet);
  }

  private void encodeTokenData(OptionToken optionToken, ByteBuf payload) {
    switch (optionToken.tokenType()) {
      case VersionOptionToken.TYPE:
        payload.writeInt(0); // UL_VERSION
        payload.writeShort(0); // US_BUILD
        break;
      case EncryptionOptionToken.TYPE:
        payload.writeByte(((EncryptionOptionToken) optionToken).setting());
        break;
    }
  }
}

/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mssqlclient.impl.command.PreLoginCommand;
import io.vertx.mssqlclient.impl.protocol.client.prelogin.EncryptionOptionToken;
import io.vertx.mssqlclient.impl.protocol.client.prelogin.OptionToken;
import io.vertx.mssqlclient.impl.protocol.client.prelogin.VersionOptionToken;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.util.List;

import static io.vertx.mssqlclient.impl.codec.MessageType.PRE_LOGIN;

class PreLoginCommandCodec extends MSSQLCommandCodec<Void, PreLoginCommand> {

  PreLoginCommandCodec(TdsMessageCodec tdsMessageCodec, PreLoginCommand cmd) {
    super(tdsMessageCodec, cmd);
  }

  @Override
  void encode() {
    ByteBuf content = tdsMessageCodec.alloc().ioBuffer();

    // packet data
    List<OptionToken> optionTokens = cmd.optionTokens();

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
      content.writeByte(token.tokenType());
      switch (token.tokenType()) {
        case VersionOptionToken.TYPE:
          versionOptionTokenOffsetLengthIdx = content.writerIndex();
          break;
        case EncryptionOptionToken.TYPE:
          encryptionOptionTokenOffsetLengthIdx = content.writerIndex();
          break;
        default:
          throw new IllegalStateException("Unexpected token type");
      }
      content.writeShort(0x00);
      content.writeShort(token.optionLength());
    }

    // terminator token
    content.writeByte(0xFF);

    // option token data
    for (OptionToken token : optionTokens) {
      encodeTokenData(token, content);
    }

    // calculate Option offset
    int totalLengthOfPayload = content.writerIndex();
    int offsetStart = totalLengthOfPayload - totalLengthOfOptionsData;

    for (OptionToken token : optionTokens) {
      switch (token.tokenType()) {
        case VersionOptionToken.TYPE:
          content.setShort(versionOptionTokenOffsetLengthIdx, offsetStart);
          offsetStart += token.optionLength();
          break;
        case EncryptionOptionToken.TYPE:
          content.setShort(encryptionOptionTokenOffsetLengthIdx, offsetStart);
          offsetStart += token.optionLength();
          break;
        default:
          throw new IllegalStateException("Unexpected token type");
      }
    }

    tdsMessageCodec.encoder().writePacket(PRE_LOGIN, content);
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

  @Override
  void decode(ByteBuf payload) {
    completionHandler.handle(CommandResponse.success(null));
  }
}

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
import io.vertx.mssqlclient.impl.MSSQLDatabaseMetadata;
import io.vertx.mssqlclient.impl.command.PreLoginCommand;
import io.vertx.mssqlclient.impl.command.PreLoginResponse;
import io.vertx.sqlclient.codec.CommandResponse;

import static io.vertx.mssqlclient.impl.codec.EncryptionLevel.ENCRYPT_OFF;
import static io.vertx.mssqlclient.impl.codec.EncryptionLevel.ENCRYPT_ON;
import static io.vertx.mssqlclient.impl.codec.MessageType.PRE_LOGIN;

class PreLoginMSSQLCommandMessage extends MSSQLCommandMessage<PreLoginResponse, PreLoginCommand> {

  private static final int VERSION = 0x00;
  private static final int ENCRYPTION = 0x01;
  private static final int TERMINATOR = 0xFF;

  PreLoginMSSQLCommandMessage(PreLoginCommand cmd) {
    super(cmd);
  }

  @Override
  void encode() {
    ByteBuf content = tdsMessageCodec.alloc().ioBuffer();

    int versionOptionIndex = encodeOption(content, VERSION);
    int encryptionOptionIndex = encodeOption(content, ENCRYPTION);
    content.writeByte(TERMINATOR);

    encodeOptionOffset(content, versionOptionIndex, content.writerIndex());
    encodeOptionLength(content, versionOptionIndex, 6);
    content.writeZero(6);

    encodeOptionOffset(content, encryptionOptionIndex, content.writerIndex());
    encodeOptionLength(content, encryptionOptionIndex, 1);
    content.writeByte(cmd.sslRequired() ? ENCRYPT_ON : ENCRYPT_OFF);

    tdsMessageCodec.encoder().writeTdsMessage(PRE_LOGIN, content);
  }

  private int encodeOption(ByteBuf content, int token) {
    int start = content.writerIndex();
    content.writeByte(token);
    content.writeZero(4);
    return start;
  }

  private void encodeOptionOffset(ByteBuf content, int optionIndex, int offset) {
    content.setShort(optionIndex + 1, offset);
  }

  private void encodeOptionLength(ByteBuf content, int optionIndex, int length) {
    content.setShort(optionIndex + 3, length);
  }

  @Override
  void decode(ByteBuf payload) {
    int startOfMessage = payload.readerIndex();
    MSSQLDatabaseMetadata metadata = null;
    Byte encryptionLevel = null;
    while (true) {
      short optionType = payload.readUnsignedByte();
      if (optionType == TERMINATOR) {
        break;
      }
      int offset = payload.readUnsignedShort();
      payload.skipBytes(2); // length
      payload.markReaderIndex();
      payload.readerIndex(startOfMessage + offset);
      if (optionType == VERSION) {
        int major = payload.readUnsignedByte();
        int minor = payload.readUnsignedByte();
        int build = payload.readUnsignedShort();
        metadata = new MSSQLDatabaseMetadata(String.format("%d.%d.%d", major, minor, build), major, minor);
      } else if (optionType == ENCRYPTION) {
        encryptionLevel = payload.readByte();
      }
      payload.resetReaderIndex();
    }
    tdsMessageCodec.decoder().fireCommandResponse(CommandResponse.success(new PreLoginResponse(metadata, encryptionLevel)));
  }
}

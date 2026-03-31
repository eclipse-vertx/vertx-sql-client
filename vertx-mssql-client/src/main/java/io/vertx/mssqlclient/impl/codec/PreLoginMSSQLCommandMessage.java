/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
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

/**
 * PRELOGIN message codec for SQL Server connections.
 * <p>
 * The PRELOGIN message is sent by the client before authentication to negotiate
 * connection parameters including encryption settings.
 * <p>
 * <b>TDS 7.x Behavior:</b>
 * <ul>
 * <li>PRELOGIN is sent in cleartext before any encryption is established</li>
 * <li>Client and server negotiate encryption via the ENCRYPTION option</li>
 * <li>If encryption is negotiated, TLS handshake occurs after PRELOGIN exchange</li>
 * </ul>
 * <p>
 * <b>TDS 8.0 Behavior (EncryptionMode.STRICT):</b>
 * <ul>
 * <li>TLS is established BEFORE PRELOGIN is sent (connection already encrypted)</li>
 * <li>PRELOGIN message is sent encrypted</li>
 * <li>The ENCRYPTION option value is <b>ignored by the server</b> (per TDS 8.0 spec)</li>
 * <li>No encryption negotiation needed since TLS is already active</li>
 * </ul>
 *
 * @see <a href="https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-tds/60f56408-0188-4cd5-8b90-25c6f2423868">MS-TDS PRELOGIN Specification</a>
 */
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
    // Note: For TDS 8.0 (EncryptionMode.STRICT), cmd.sslRequired() is false because
    // SSL is already established via ConnectOptions before PRELOGIN is sent.
    // The server will ignore this value for TDS 8.0 connections (per protocol spec).
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

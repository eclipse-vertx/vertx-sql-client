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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.impl.protocol.MessageStatus;
import io.vertx.mssqlclient.impl.protocol.MessageType;
import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.vertx.mssqlclient.impl.protocol.client.login.LoginPacket;
import io.vertx.mssqlclient.impl.protocol.token.DataPacketStreamTokenType;
import io.vertx.mssqlclient.impl.utils.Utils;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.command.InitCommand;

import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_16LE;

class InitCommandCodec extends MSSQLCommandCodec<Connection, InitCommand> {
  InitCommandCodec(InitCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(TdsMessageEncoder encoder) {
    super.encode(encoder);
    sendLoginMessage();
  }

  @Override
  void decodeMessage(TdsMessage message, TdsMessageEncoder encoder) {
    ByteBuf messageBody = message.content();
    while (messageBody.isReadable()) {
      int tokenType = messageBody.readUnsignedByte();
      switch (tokenType) {
        //FIXME complete all the logic here
        case DataPacketStreamTokenType.LOGINACK_TOKEN:
          result = cmd.connection();
          break;
        case DataPacketStreamTokenType.ERROR_TOKEN:
          handleErrorToken(messageBody);
          break;
        case DataPacketStreamTokenType.INFO_TOKEN:
          break;
        case DataPacketStreamTokenType.ENVCHANGE_TOKEN:
          break;
        case DataPacketStreamTokenType.DONE_TOKEN:
          handleDoneToken();
          break;
      }
    }
  }

  private void sendLoginMessage() {
    ChannelHandlerContext chctx = encoder.chctx;

    ByteBuf packet = chctx.alloc().ioBuffer();

    // packet header
    packet.writeByte(MessageType.TDS7_LOGIN.value());
    packet.writeByte(MessageStatus.NORMAL.value() | MessageStatus.END_OF_MESSAGE.value());
    int packetLenIdx = packet.writerIndex();
    packet.writeShort(0); // set length later
    packet.writeShort(0x00);
    packet.writeByte(0x00); // FIXME packet ID
    packet.writeByte(0x00);

    int startIdx = packet.writerIndex(); // Length
    packet.writeInt(0x00); // set length later by calculating
    packet.writeInt(LoginPacket.SQL_SERVER_2017_VERSION); // TDSVersion
    packet.writeIntLE(LoginPacket.DEFAULT_PACKET_SIZE); // PacketSize
    packet.writeIntLE(0x00); // ClientProgVer
    packet.writeIntLE(0x00); // ClientPID
    packet.writeIntLE(0x00); // ConnectionID
    packet.writeByte(LoginPacket.DEFAULT_OPTION_FLAGS1
      | LoginPacket.OPTION_FLAGS1_DUMPLOAD_OFF); // OptionFlags1
    packet.writeByte(LoginPacket.DEFAULT_OPTION_FLAGS2); // OptionFlags2
    packet.writeByte(LoginPacket.DEFAULT_TYPE_FLAGS); // TypeFlags
    packet.writeByte(LoginPacket.DEFAULT_OPTION_FLAGS3); // OptionFlags3
    packet.writeIntLE(0x00); // ClientTimeZone
    packet.writeIntLE(0x00); // ClientLCID

    /*
      OffsetLength part:
      we set offset by calculating ByteBuf writer indexes diff.
     */
    Map<String, String> properties = cmd.properties();

    // HostName
    String hostName = Utils.getHostName();
    int hostNameOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(hostName.length());

    // UserName
    String userName = cmd.username();
    int userNameOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(userName.length());

    // Password
    String password = cmd.password();
    int passwordOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(password.length());

    // AppName
    CharSequence appName = properties.get("appName");
    if (appName == null || appName.length() == 0) {
      appName = MSSQLConnectOptions.DEFAULT_APP_NAME;
    }
    int appNameOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(appName.length());

    // ServerName
    String serverName = cmd.connection().socket().remoteAddress().host();
    int serverNameOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(serverName.length());

    // Unused or Extension
    int unusedOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(0);

    // CltIntName
    CharSequence interfaceLibraryName = properties.get("clientInterfaceName");
    if (interfaceLibraryName == null || interfaceLibraryName.length() == 0) {
      interfaceLibraryName = MSSQLConnectOptions.DEFAULT_CLIENT_INTERFACE_NAME;
    }
    int cltIntNameOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(interfaceLibraryName.length());

    // Language
    int languageOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(0);

    // Database
    String database = cmd.database();
    int databaseOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(database.length());

    // ClientID
    // 6 BYTE
    packet.writeIntLE(0x00);
    packet.writeShortLE(0x00);

    // SSPI
    int sspiOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(0x00);

    // AtchDBFile
    int atchDbFileOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(0x00);

    // ChangePassword
    int changePasswordOffsetLengthIdx = packet.writerIndex();
    packet.writeShortLE(0x00); // offset
    packet.writeShortLE(0x00);

    // SSPILong
    packet.writeIntLE(0x00);

    /*
      Data part: note we should set offset by calculation before writing data
     */
    packet.setShortLE(hostNameOffsetLengthIdx, packet.writerIndex() - startIdx);
    packet.writeCharSequence(hostName, UTF_16LE);

    packet.setShortLE(userNameOffsetLengthIdx, packet.writerIndex() - startIdx);
    packet.writeCharSequence(userName, UTF_16LE);

    packet.setShortLE(passwordOffsetLengthIdx, packet.writerIndex() - startIdx);
    writePassword(packet, password);

    packet.setShortLE(appNameOffsetLengthIdx, packet.writerIndex() - startIdx);
    packet.writeCharSequence(appName, UTF_16LE);

    packet.setShortLE(serverNameOffsetLengthIdx, packet.writerIndex() - startIdx);
    packet.writeCharSequence(serverName, UTF_16LE);

    packet.setShortLE(unusedOffsetLengthIdx, packet.writerIndex() - startIdx);

    packet.setShortLE(cltIntNameOffsetLengthIdx, packet.writerIndex() - startIdx);
    packet.writeCharSequence(interfaceLibraryName, UTF_16LE);

    packet.setShortLE(languageOffsetLengthIdx, packet.writerIndex() - startIdx);

    packet.setShortLE(databaseOffsetLengthIdx, packet.writerIndex() - startIdx);
    packet.writeCharSequence(database, UTF_16LE);

    packet.setShortLE(sspiOffsetLengthIdx, packet.writerIndex() - startIdx);

    packet.setShortLE(atchDbFileOffsetLengthIdx, packet.writerIndex() - startIdx);

    packet.setShortLE(changePasswordOffsetLengthIdx, packet.writerIndex() - startIdx);

    // set length
    packet.setIntLE(startIdx, packet.writerIndex() - startIdx);

    int packetLen = packet.writerIndex() - startIdx + 8;
    packet.setShort(packetLenIdx, packetLen);

    chctx.writeAndFlush(packet);

  }

  /*
    Before submitting a password from the client to the server,
    for every byte in the password buffer starting with the position pointed to by ibPassword or ibChangePassword,
    the client SHOULD first swap the four high bits with the four low bits and then do a bit-XOR with 0xA5 (10100101).
    After reading a submitted password, for every byte in the password buffer starting with the position pointed to by ibPassword or ibChangePassword,
    the server SHOULD first do a bit-XOR with 0xA5 (10100101) and then swap the four high bits with the four low bits.
   */
  private void writePassword(ByteBuf payload, String password) {
    byte[] bytes = password.getBytes(UTF_16LE);
    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      bytes[i] = (byte) ((b >> 4 | ((b & 0x0F) << 4)) ^ 0xA5);
    }
    payload.writeBytes(bytes);
  }
}

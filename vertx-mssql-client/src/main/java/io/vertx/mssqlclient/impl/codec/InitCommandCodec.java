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
import io.vertx.core.net.HostAndPort;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.impl.MSSQLSocketConnection;
import io.vertx.mssqlclient.impl.protocol.client.login.LoginPacket;
import io.vertx.mssqlclient.impl.utils.Utils;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.internal.command.InitCommand;

import java.util.Map;

import static io.vertx.mssqlclient.impl.codec.MessageType.TDS7_LOGIN;
import static io.vertx.mssqlclient.impl.utils.ByteBufUtils.readUnsignedShortLengthString;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.util.Locale.ENGLISH;

class InitCommandCodec extends MSSQLCommandCodec<Connection, InitCommand> {

  static final Object LOGIN_SENT = new Object();

  InitCommandCodec(InitCommand cmd) {
    super(cmd);
  }

  @Override
  void encode() {
    ByteBuf content = tdsMessageCodec.alloc().ioBuffer();

    int startIdx = content.writerIndex(); // Length
    content.writeZero(4); // set length later by calculating
    content.writeInt(LoginPacket.SQL_SERVER_2017_VERSION); // TDSVersion
    content.writeIntLE(tdsMessageCodec.encoder().packetSize()); // PacketSize
    content.writeZero(12); // ClientProgVer + ClientPID + ConnectionID
    content.writeByte(LoginPacket.DEFAULT_OPTION_FLAGS1 |
      LoginPacket.OPTION_FLAGS1_ORDER_X86 |
      LoginPacket.OPTION_FLAGS1_CHARSET_ASCII |
      LoginPacket.OPTION_FLAGS1_FLOAT_IEEE_754 |
      LoginPacket.OPTION_FLAGS1_USE_DB_OFF |
      LoginPacket.OPTION_FLAGS1_INIT_DB_FATAL |
      LoginPacket.OPTION_FLAGS1_SET_LANG_ON
    ); // OptionFlags1
    content.writeByte(LoginPacket.DEFAULT_OPTION_FLAGS2 |
      LoginPacket.OPTION_FLAGS2_ODBC_ON
    ); // OptionFlags2
    content.writeByte(LoginPacket.DEFAULT_TYPE_FLAGS); // TypeFlags
    content.writeByte(LoginPacket.DEFAULT_OPTION_FLAGS3); // OptionFlags3
    content.writeZero(8); // ClientTimeZone + ClientLCID

    /*
      OffsetLength part:
      we set offset by calculating ByteBuf writer indexes diff.
     */
    Map<String, String> properties = cmd.properties();

    // HostName
    String hostName = Utils.getHostName();
    int hostNameOffsetLengthIdx = content.writerIndex();
    content.writeZero(2); // offset
    content.writeShortLE(hostName.length());

    // UserName
    String userName = cmd.username();
    int userNameOffsetLengthIdx = content.writerIndex();
    content.writeZero(2); // offset
    content.writeShortLE(userName.length());

    // Password
    String password = cmd.password();
    int passwordOffsetLengthIdx = content.writerIndex();
    content.writeZero(2); // offset
    content.writeShortLE(password.length());

    // AppName
    CharSequence appName = properties.get("appName");
    if (appName == null || appName.length() == 0) {
      appName = MSSQLConnectOptions.DEFAULT_APP_NAME;
    }
    int appNameOffsetLengthIdx = content.writerIndex();
    content.writeZero(2); // offset
    content.writeShortLE(appName.length());

    // ServerName
    String serverName = ((MSSQLSocketConnection)cmd.connection()).socket().remoteAddress().host();
    int serverNameOffsetLengthIdx = content.writerIndex();
    content.writeZero(2); // offset
    content.writeShortLE(serverName.length());

    // Unused or Extension
    int unusedOffsetLengthIdx = content.writerIndex();
    content.writeZero(4);

    // CltIntName
    CharSequence interfaceLibraryName = properties.get("clientInterfaceName");
    if (interfaceLibraryName == null || interfaceLibraryName.length() == 0) {
      interfaceLibraryName = MSSQLConnectOptions.DEFAULT_CLIENT_INTERFACE_NAME;
    }
    int cltIntNameOffsetLengthIdx = content.writerIndex();
    content.writeZero(2); // offset
    content.writeShortLE(interfaceLibraryName.length());

    // Language
    int languageOffsetLengthIdx = content.writerIndex();
    content.writeZero(4);

    // Database
    String database = cmd.database();
    int databaseOffsetLengthIdx = content.writerIndex();
    content.writeZero(2); // offset
    content.writeShortLE(database.length());

    // ClientID
    // 6 BYTE
    content.writeZero(6);

    // SSPI
    int sspiOffsetLengthIdx = content.writerIndex();
    content.writeZero(4);

    // AtchDBFile
    int atchDbFileOffsetLengthIdx = content.writerIndex();
    content.writeZero(4);

    // ChangePassword
    int changePasswordOffsetLengthIdx = content.writerIndex();
    content.writeZero(4);

    // SSPILong
    content.writeZero(4);

    /*
      Data part: note we should set offset by calculation before writing data
     */
    content.setShortLE(hostNameOffsetLengthIdx, content.writerIndex() - startIdx);
    content.writeCharSequence(hostName, UTF_16LE);

    content.setShortLE(userNameOffsetLengthIdx, content.writerIndex() - startIdx);
    content.writeCharSequence(userName, UTF_16LE);

    content.setShortLE(passwordOffsetLengthIdx, content.writerIndex() - startIdx);
    writePassword(content, password);

    content.setShortLE(appNameOffsetLengthIdx, content.writerIndex() - startIdx);
    content.writeCharSequence(appName, UTF_16LE);

    content.setShortLE(serverNameOffsetLengthIdx, content.writerIndex() - startIdx);
    content.writeCharSequence(serverName, UTF_16LE);

    content.setShortLE(unusedOffsetLengthIdx, content.writerIndex() - startIdx);

    content.setShortLE(cltIntNameOffsetLengthIdx, content.writerIndex() - startIdx);
    content.writeCharSequence(interfaceLibraryName, UTF_16LE);

    content.setShortLE(languageOffsetLengthIdx, content.writerIndex() - startIdx);

    content.setShortLE(databaseOffsetLengthIdx, content.writerIndex() - startIdx);
    content.writeCharSequence(database, UTF_16LE);

    content.setShortLE(sspiOffsetLengthIdx, content.writerIndex() - startIdx);

    content.setShortLE(atchDbFileOffsetLengthIdx, content.writerIndex() - startIdx);

    content.setShortLE(changePasswordOffsetLengthIdx, content.writerIndex() - startIdx);

    // set length
    content.setIntLE(startIdx, content.writerIndex() - startIdx);

    tdsMessageCodec.encoder().writeTdsMessage(TDS7_LOGIN, content);

    tdsMessageCodec.chctx().pipeline().fireUserEventTriggered(LOGIN_SENT);
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

  @Override
  protected void handleLoginAck() {
    result = cmd.connection();
  }

  @Override
  protected void handleRouting(ByteBuf payload) {
    MSSQLSocketConnection conn = (MSSQLSocketConnection) this.result;
    if (conn == null) {
      throw new IllegalStateException("Routing ENVCHANGE token MUST be sent after the LOGINACK token in the login response");
    }
    if (payload.readUnsignedShortLE() <= 5) {
      throw new IllegalStateException("RoutingDataValueLength too short");
    }
    if (payload.readUnsignedByte() != 0) {
      throw new IllegalStateException("Protocol MUST be 0, specifying TCP-IP protocol");
    }
    int port;
    if ((port = payload.readUnsignedShortLE()) == 0) {
      throw new IllegalStateException("ProtocolProperty value of zero is not allowed when Protocol is TCP-IP");
    }
    String host = readUnsignedShortLengthString(payload);
    conn.setAlternateServer(HostAndPort.create(host.toLowerCase(ENGLISH), port));
  }
}

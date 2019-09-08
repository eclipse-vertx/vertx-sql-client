/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.MySQLCollation;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.mysqlclient.impl.util.CachingSha2Authenticator;
import io.vertx.mysqlclient.impl.util.Native41Authenticator;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.InitCommand;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.vertx.mysqlclient.impl.codec.CapabilitiesFlag.*;
import static io.vertx.mysqlclient.impl.codec.Packets.*;

class InitCommandCodec extends CommandCodec<Connection, InitCommand> {

  private static final int NONCE_LENGTH = 20;
  private static final int AUTH_PLUGIN_DATA_PART1_LENGTH = 8;

  private static final int ST_CONNECTING = 0;
  private static final int ST_AUTHENTICATING = 1;
  private static final int ST_CONNECTED = 2;

  private static final int AUTH_SWITCH_REQUEST_STATUS_FLAG = 0xFE;
  private static final int AUTH_MORE_DATA_STATUS_FLAG = 0x01;
  private static final int FAST_AUTH_STATUS_FLAG = 0x03;
  private static final int FULL_AUTHENTICATION_STATUS_FLAG = 0x04;

  private int status = ST_CONNECTING;

  private MySQLCollation collation;

  InitCommandCodec(InitCommand cmd) {
    super(cmd);
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength, int sequenceId) {
    switch (status) {
      case ST_CONNECTING:
        decodeInit0(encoder, cmd, payload);
        status = ST_AUTHENTICATING;
        break;
      case ST_AUTHENTICATING:
        decodeInit1(cmd, payload);
        break;
    }
  }

  private void decodeInit0(MySQLEncoder encoder, InitCommand cmd, ByteBuf payload) {
    short protocolVersion = payload.readUnsignedByte();

    String serverVersion = BufferUtils.readNullTerminatedString(payload, StandardCharsets.US_ASCII);
    // we assume the server version follows ${major}.${minor}.${release} in https://dev.mysql.com/doc/refman/8.0/en/which-version.html
    String[] versionNumbers = serverVersion.split("\\.");
    int majorVersion = Integer.parseInt(versionNumbers[0]);
    int minorVersion = Integer.parseInt(versionNumbers[1]);
    // we should truncate the possible suffixes here
    String releaseVersion = versionNumbers[2];
    int releaseNumber;
    int indexOfFirstSeparator = releaseVersion.indexOf("-");
    if (indexOfFirstSeparator != -1) {
      // handle unstable release suffixes
      String releaseNumberString = releaseVersion.substring(0, indexOfFirstSeparator);
      releaseNumber = Integer.parseInt(releaseNumberString);
    } else {
      releaseNumber = Integer.parseInt(versionNumbers[2]);
    }
    if (majorVersion == 5 && (minorVersion < 7 || (minorVersion == 7 && releaseNumber < 5))) {
      // EOF_HEADER is enabled
    } else {
      encoder.clientCapabilitiesFlag |= CLIENT_DEPRECATE_EOF;
    }

    long connectionId = payload.readUnsignedIntLE();

    // read first part of scramble
    byte[] scramble = new byte[NONCE_LENGTH];
    payload.readBytes(scramble, 0, AUTH_PLUGIN_DATA_PART1_LENGTH);

    //filler
    payload.readByte();

    // read lower 2 bytes of Capabilities flags
    int serverCapabilitiesFlags = payload.readUnsignedShortLE();

    short characterSet = payload.readUnsignedByte();

    int statusFlags = payload.readUnsignedShortLE();

    // read upper 2 bytes of Capabilities flags
    int capabilityFlagsUpper = payload.readUnsignedShortLE();
    serverCapabilitiesFlags |= (capabilityFlagsUpper << 16);

    // length of the combined auth_plugin_data (scramble)
    short lenOfAuthPluginData;
    boolean isClientPluginAuthSupported = (serverCapabilitiesFlags & CapabilitiesFlag.CLIENT_PLUGIN_AUTH) != 0;
    if (isClientPluginAuthSupported) {
      lenOfAuthPluginData = payload.readUnsignedByte();
    } else {
      payload.readerIndex(payload.readerIndex() + 1);
      lenOfAuthPluginData = 0;
    }

    // 10 bytes reserved
    payload.readerIndex(payload.readerIndex() + 10);

    // Rest of the plugin provided data
    payload.readBytes(scramble, AUTH_PLUGIN_DATA_PART1_LENGTH, Math.max(NONCE_LENGTH - AUTH_PLUGIN_DATA_PART1_LENGTH, lenOfAuthPluginData - 9));
    payload.readByte(); // reserved byte

    String authPluginName = null;
    if (isClientPluginAuthSupported) {
      authPluginName = BufferUtils.readNullTerminatedString(payload, StandardCharsets.UTF_8);
    }

    //TODO we may not need an extra object here?(inline)
    InitialHandshakePacket initialHandshakePacket = new InitialHandshakePacket(serverVersion,
      connectionId,
      serverCapabilitiesFlags,
      characterSet,
      statusFlags,
      scramble,
      authPluginName
    );

    boolean upgradeToSsl;
    String sslMode = cmd.properties().get("sslMode");
    switch (sslMode) {
      case "DISABLED":
        upgradeToSsl = false;
        break;
      case "PREFERRED":
        upgradeToSsl = isTlsSupportedByServer(serverCapabilitiesFlags);
        break;
      case "REQUIRED":
      case "VERIFY_CA":
      case "VERIFY_IDENTITY":
        upgradeToSsl = true;
        break;
      default:
        completionHandler.handle(CommandResponse.failure(new IllegalStateException("Unknown SSL mode to handle: " + sslMode)));
        return;
    }

    if (upgradeToSsl) {
      encoder.clientCapabilitiesFlag |= CLIENT_SSL;
      sendSslRequest();

      encoder.socketConnection.upgradeToSSLConnection(upgrade -> {
        if (upgrade.succeeded()) {
          doSendHandshakeResponseMessage(initialHandshakePacket);
        } else {
          completionHandler.handle(CommandResponse.failure(upgrade.cause()));
        }
      });
    } else {
      doSendHandshakeResponseMessage(initialHandshakePacket);
    }
  }

  private void doSendHandshakeResponseMessage(InitialHandshakePacket initialHandshakePacket) {
    if (cmd.database() != null && !cmd.database().isEmpty()) {
      encoder.clientCapabilitiesFlag |= CLIENT_CONNECT_WITH_DB;
    }
    String authMethodName = initialHandshakePacket.getAuthMethodName();
    byte[] serverScramble = initialHandshakePacket.getScramble();
    Map<String, String> properties = cmd.properties();
    checkCollation();
    encoder.charset = Charset.forName(collation.mappedJavaCharsetName());
    Map<String, String> clientConnectionAttributes = properties;
    if (clientConnectionAttributes != null && !clientConnectionAttributes.isEmpty()) {
      encoder.clientCapabilitiesFlag |= CLIENT_CONNECT_ATTRS;
    }
    encoder.clientCapabilitiesFlag &= initialHandshakePacket.getServerCapabilitiesFlags();
    sendHandshakeResponseMessage(cmd.username(), cmd.password(), cmd.database(), serverScramble, authMethodName, clientConnectionAttributes);
  }

  private void decodeInit1(InitCommand cmd, ByteBuf payload) {
    int header = payload.getUnsignedByte(payload.readerIndex());
    switch (header) {
      case OK_PACKET_HEADER:
        status = ST_CONNECTED;
        completionHandler.handle(CommandResponse.success(cmd.connection()));
        break;
      case ERROR_PACKET_HEADER:
        handleErrorPacketPayload(payload);
        break;
      case AUTH_SWITCH_REQUEST_STATUS_FLAG:
        // Protocol::AuthSwitchRequest
        payload.skipBytes(1); // status flag, always 0xFE
        String pluginName = BufferUtils.readNullTerminatedString(payload, StandardCharsets.UTF_8);
        byte[] nonce = new byte[NONCE_LENGTH];
        payload.readBytes(nonce);
        byte[] scrambledPassword;
        switch (pluginName) {
          case "mysql_native_password":
            scrambledPassword = Native41Authenticator.encode(cmd.password().getBytes(), nonce);
            break;
          case "caching_sha2_password":
            scrambledPassword = CachingSha2Authenticator.encode(cmd.password().getBytes(), nonce);
            break;
          default:
            completionHandler.handle(CommandResponse.failure(new UnsupportedOperationException("Unsupported authentication method: " + pluginName)));
            return;
        }
        int scrambledPasswordPacketLength = scrambledPassword.length;
        ByteBuf scrambledPasswordPacket = allocateBuffer(scrambledPasswordPacketLength + 4);
        scrambledPasswordPacket.writeMediumLE(scrambledPasswordPacketLength);
        scrambledPasswordPacket.writeByte(sequenceId);
        scrambledPasswordPacket.writeBytes(scrambledPassword);
        sendNonSplitPacket(scrambledPasswordPacket);
        break;
      case AUTH_MORE_DATA_STATUS_FLAG:
        payload.skipBytes(1); // skip the status flag
        byte flag = payload.readByte();
        if (flag == FULL_AUTHENTICATION_STATUS_FLAG) {
          if (encoder.socketConnection.isSsl()) {
            // send the non-scrambled password directly since it's on a secure connection
            byte[] password = cmd.password().getBytes();
            int nonScrambledPasswordPacketLength = password.length + 1;
            ByteBuf nonScrambledPasswordPacket = allocateBuffer(nonScrambledPasswordPacketLength + 4);
            nonScrambledPasswordPacket.writeMediumLE(nonScrambledPasswordPacketLength);
            nonScrambledPasswordPacket.writeByte(sequenceId);
            nonScrambledPasswordPacket.writeBytes(password);
            nonScrambledPasswordPacket.writeByte(0x00); // end with a 0x00
            sendNonSplitPacket(nonScrambledPasswordPacket);
          } else {
            //TODO public key exchange support?
            completionHandler.handle(CommandResponse.failure(new UnsupportedOperationException("Public Key Request is not supported by now, you should use caching_sha2_password authentication with TLS enabled")));
            return;
          }
        } else if (flag == FAST_AUTH_STATUS_FLAG) {
          // fast auth success
          return;
        } else {
          throw new UnsupportedOperationException("Unsupported flag for AuthMoreData : " + flag);
        }
        break;
      default:
        completionHandler.handle(CommandResponse.failure(new IllegalStateException("Unhandled state with header: " + header)));
        return;
    }
  }

  private void sendSslRequest() {
    ByteBuf packet = allocateBuffer(36);
    // encode packet header
    packet.writeMediumLE(32);
    packet.writeByte(sequenceId);

    // encode SSLRequest payload
    packet.writeIntLE(encoder.clientCapabilitiesFlag);
    packet.writeIntLE(PACKET_PAYLOAD_LENGTH_LIMIT);
    checkCollation();
    packet.writeByte(collation.collationId());
    byte[] filler = new byte[23];
    packet.writeBytes(filler);

    sendNonSplitPacket(packet);
  }

  private void sendHandshakeResponseMessage(String username, String password, String database, byte[] nonce, String authMethodName, Map<String, String> clientConnectionAttributes) {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId);

    // encode packet payload
    int clientCapabilitiesFlags = encoder.clientCapabilitiesFlag;
    packet.writeIntLE(clientCapabilitiesFlags);
    packet.writeIntLE(PACKET_PAYLOAD_LENGTH_LIMIT);
    packet.writeByte(collation.collationId());
    packet.writeZero(23); // filler
    BufferUtils.writeNullTerminatedString(packet, username, StandardCharsets.UTF_8);
    if (password == null || password.isEmpty()) {
      packet.writeByte(0);
    } else {
      byte[] scrambledPassword;
      switch (authMethodName) {
        case "mysql_native_password":
          scrambledPassword = Native41Authenticator.encode(password.getBytes(), nonce);
          break;
        case "caching_sha2_password":
          scrambledPassword = CachingSha2Authenticator.encode(password.getBytes(), nonce);
          break;
        default:
          completionHandler.handle(CommandResponse.failure(new UnsupportedOperationException("Unsupported authentication method: " + authMethodName)));
          return;
      }
      if ((clientCapabilitiesFlags & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0) {
        BufferUtils.writeLengthEncodedInteger(packet, scrambledPassword.length);
        packet.writeBytes(scrambledPassword);
      } else if ((clientCapabilitiesFlags & CLIENT_SECURE_CONNECTION) != 0) {
        packet.writeByte(scrambledPassword.length);
        packet.writeBytes(scrambledPassword);
      } else {
        packet.writeByte(0);
      }
    }
    if ((clientCapabilitiesFlags & CLIENT_CONNECT_WITH_DB) != 0) {
      BufferUtils.writeNullTerminatedString(packet, database, StandardCharsets.UTF_8);
    }
    if ((clientCapabilitiesFlags & CLIENT_PLUGIN_AUTH) != 0) {
      BufferUtils.writeNullTerminatedString(packet, authMethodName, StandardCharsets.UTF_8);
    }
    if ((clientCapabilitiesFlags & CLIENT_CONNECT_ATTRS) != 0) {
      ByteBuf kv = encoder.chctx.alloc().ioBuffer();
      for (Map.Entry<String, String> attribute : clientConnectionAttributes.entrySet()) {
        if (nonAttributePropertyKeys.contains(attribute.getKey())) {
          // we store it in the properties but it's not an attribute
        } else {
          BufferUtils.writeLengthEncodedString(kv, attribute.getKey(), StandardCharsets.UTF_8);
          BufferUtils.writeLengthEncodedString(kv, attribute.getValue(), StandardCharsets.UTF_8);
        }
      }
      BufferUtils.writeLengthEncodedInteger(packet, kv.readableBytes());
      packet.writeBytes(kv);
    }

    // set payload length
    int payloadLength = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, payloadLength);

    sendPacket(packet, payloadLength);
  }

  private boolean isTlsSupportedByServer(int serverCapabilitiesFlags) {
    return (serverCapabilitiesFlags & CLIENT_SSL) != 0;
  }

  private void checkCollation() {
    if (this.collation == null) {
      collation = MySQLCollation.valueOfName(cmd.properties().get("collation"));
    }
  }
}

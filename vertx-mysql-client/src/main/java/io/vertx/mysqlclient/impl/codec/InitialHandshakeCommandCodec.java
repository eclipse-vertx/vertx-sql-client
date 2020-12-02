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
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mysqlclient.MySQLAuthenticationPlugin;
import io.vertx.mysqlclient.SslMode;
import io.vertx.mysqlclient.impl.MySQLDatabaseMetadata;
import io.vertx.mysqlclient.impl.command.InitialHandshakeCommand;
import io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.mysqlclient.impl.util.CachingSha2Authenticator;
import io.vertx.mysqlclient.impl.util.Native41Authenticator;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.*;
import static io.vertx.mysqlclient.impl.protocol.Packets.*;

class InitialHandshakeCommandCodec extends AuthenticationCommandBaseCodec<Connection, InitialHandshakeCommand> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InitialHandshakeCommandCodec.class);

  private static final int AUTH_PLUGIN_DATA_PART1_LENGTH = 8;

  private static final int ST_CONNECTING = 0;
  private static final int ST_AUTHENTICATING = 1;
  private static final int ST_CONNECTED = 2;

  private int status = ST_CONNECTING;

  InitialHandshakeCommandCodec(InitialHandshakeCommand cmd) {
    super(cmd);
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    switch (status) {
      case ST_CONNECTING:
        handleInitialHandshake(payload);
        status = ST_AUTHENTICATING;
        break;
      case ST_AUTHENTICATING:
        handleAuthentication(payload);
        break;
    }
  }

  private void handleInitialHandshake(ByteBuf payload) {
    encoder.clientCapabilitiesFlag = cmd.initialCapabilitiesFlags();
    encoder.encodingCharset = cmd.charsetEncoding();
    short protocolVersion = payload.readUnsignedByte();

    String serverVersion = BufferUtils.readNullTerminatedString(payload, StandardCharsets.US_ASCII);
    MySQLDatabaseMetadata md = MySQLDatabaseMetadata.parse(serverVersion);
    encoder.socketConnection.metaData = md;
    if (md.majorVersion() == 5 &&
        (md.minorVersion() < 7 || (md.minorVersion() == 7 && md.microVersion() < 5))) {
      // EOF_HEADER has to be enabled for older MySQL version which does not support the CLIENT_DEPRECATE_EOF flag
    } else {
      encoder.clientCapabilitiesFlag |= CLIENT_DEPRECATE_EOF;
    }

    long connectionId = payload.readUnsignedIntLE();

    // read first part of scramble
    this.authPluginData = new byte[NONCE_LENGTH];
    payload.readBytes(authPluginData, 0, AUTH_PLUGIN_DATA_PART1_LENGTH);

    //filler
    payload.readByte();

    // read lower 2 bytes of Capabilities flags
    int lowerServerCapabilitiesFlags = payload.readUnsignedShortLE();

    short characterSet = payload.readUnsignedByte();

    int statusFlags = payload.readUnsignedShortLE();

    // read upper 2 bytes of Capabilities flags
    int capabilityFlagsUpper = payload.readUnsignedShortLE();
    final int serverCapabilitiesFlags = (lowerServerCapabilitiesFlags | (capabilityFlagsUpper << 16));

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
    payload.readBytes(authPluginData, AUTH_PLUGIN_DATA_PART1_LENGTH, Math.max(NONCE_LENGTH - AUTH_PLUGIN_DATA_PART1_LENGTH, lenOfAuthPluginData - 9));
    payload.readByte(); // reserved byte

    // we assume the server supports auth plugin
    final String serverAuthPluginName = BufferUtils.readNullTerminatedString(payload, StandardCharsets.UTF_8);

    boolean upgradeToSsl;
    SslMode sslMode = cmd.sslMode();
    switch (sslMode) {
      case DISABLED:
        upgradeToSsl = false;
        break;
      case PREFERRED:
        upgradeToSsl = isTlsSupportedByServer(serverCapabilitiesFlags);
        break;
      case REQUIRED:
      case VERIFY_CA:
      case VERIFY_IDENTITY:
        upgradeToSsl = true;
        break;
      default:
        completionHandler.handle(CommandResponse.failure(new IllegalStateException("Unknown SSL mode to handle: " + sslMode)));
        return;
    }

    if (upgradeToSsl) {
      encoder.clientCapabilitiesFlag |= CLIENT_SSL;
      sendSslRequest();

      encoder.socketConnection.upgradeToSsl(upgrade -> {
        if (upgrade.succeeded()) {
          doSendHandshakeResponseMessage(serverAuthPluginName, cmd.authenticationPlugin(), authPluginData, serverCapabilitiesFlags);
        } else {
          completionHandler.handle(CommandResponse.failure(upgrade.cause()));
        }
      });
    } else {
      doSendHandshakeResponseMessage(serverAuthPluginName, cmd.authenticationPlugin(), authPluginData, serverCapabilitiesFlags);
    }
  }

  private void doSendHandshakeResponseMessage(String serverAuthPluginName, MySQLAuthenticationPlugin authPlugin, byte[] nonce, int serverCapabilitiesFlags) {
    Map<String, String> clientConnectionAttributes = cmd.connectionAttributes();
    encoder.clientCapabilitiesFlag &= serverCapabilitiesFlags;
    String clientPluginName = authPlugin == MySQLAuthenticationPlugin.DEFAULT ? serverAuthPluginName : authPlugin.value;
    sendHandshakeResponseMessage(cmd.username(), cmd.password(), cmd.database(), nonce, clientPluginName, clientConnectionAttributes);
  }

  private void handleAuthentication(ByteBuf payload) {
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
        handleAuthSwitchRequest(cmd.password().getBytes(StandardCharsets.UTF_8), payload);
        break;
      case AUTH_MORE_DATA_STATUS_FLAG:
        handleAuthMoreData(cmd.password().getBytes(StandardCharsets.UTF_8), payload);
        break;
      default:
        completionHandler.handle(CommandResponse.failure(new IllegalStateException("Unhandled state with header: " + header)));
    }
  }

  private void handleAuthSwitchRequest(byte[] password, ByteBuf payload) {
    // Protocol::AuthSwitchRequest
    payload.skipBytes(1); // status flag, always 0xFE
    String pluginName = BufferUtils.readNullTerminatedString(payload, StandardCharsets.UTF_8);
    byte[] nonce = new byte[NONCE_LENGTH];
    payload.readBytes(nonce);
    byte[] authResponse;
    switch (pluginName) {
      case "mysql_native_password":
        authResponse = Native41Authenticator.encode(password, nonce);
        break;
      case "caching_sha2_password":
        authResponse = CachingSha2Authenticator.encode(password, nonce);
        break;
      case "mysql_clear_password":
        authResponse = password;
        break;
      default:
        completionHandler.handle(CommandResponse.failure(new UnsupportedOperationException("Unsupported authentication method: " + pluginName)));
        return;
    }
    sendBytesAsPacket(authResponse);
  }

  private void sendSslRequest() {
    ByteBuf packet = allocateBuffer(36);
    // encode packet header
    packet.writeMediumLE(32);
    packet.writeByte(sequenceId);

    // encode SSLRequest payload
    packet.writeIntLE(encoder.clientCapabilitiesFlag);
    packet.writeIntLE(PACKET_PAYLOAD_LENGTH_LIMIT);
    packet.writeByte(cmd.collation().collationId());
    packet.writeZero(23); // filler

    sendNonSplitPacket(packet);
  }

  private void sendHandshakeResponseMessage(String username, String password, String database, byte[] nonce, String clientPluginName, Map<String, String> clientConnectionAttributes) {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId);

    // encode packet payload
    int clientCapabilitiesFlags = encoder.clientCapabilitiesFlag;
    packet.writeIntLE(clientCapabilitiesFlags);
    packet.writeIntLE(PACKET_PAYLOAD_LENGTH_LIMIT);
    packet.writeByte(cmd.collation().collationId());
    packet.writeZero(23); // filler
    BufferUtils.writeNullTerminatedString(packet, username, StandardCharsets.UTF_8);
    String authMethod = clientPluginName;
    if (password.isEmpty()) {
      packet.writeByte(0);
    } else {
      byte[] authResponse;
      switch (authMethod) {
        case "mysql_native_password":
          authResponse = Native41Authenticator.encode(password.getBytes(StandardCharsets.UTF_8), nonce);
          break;
        case "caching_sha2_password":
          authResponse = CachingSha2Authenticator.encode(password.getBytes(StandardCharsets.UTF_8), nonce);
          break;
        case "mysql_clear_password":
          authResponse = password.getBytes(StandardCharsets.UTF_8);
          break;
        default:
          LOGGER.warn("Unknown authentication method: " + authMethod + ", the client will try to use mysql_native_password instead.");
          authMethod = "mysql_native_password";
          authResponse = Native41Authenticator.encode(password.getBytes(StandardCharsets.UTF_8), nonce);
          break;
      }
      if ((clientCapabilitiesFlags & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0) {
        BufferUtils.writeLengthEncodedInteger(packet, authResponse.length);
        packet.writeBytes(authResponse);
      } else if ((clientCapabilitiesFlags & CLIENT_SECURE_CONNECTION) != 0) {
        packet.writeByte(authResponse.length);
        packet.writeBytes(authResponse);
      } else {
        packet.writeByte(0);
      }
    }
    if ((clientCapabilitiesFlags & CLIENT_CONNECT_WITH_DB) != 0) {
      BufferUtils.writeNullTerminatedString(packet, database, StandardCharsets.UTF_8);
    }
    if ((clientCapabilitiesFlags & CLIENT_PLUGIN_AUTH) != 0) {
      BufferUtils.writeNullTerminatedString(packet, authMethod, StandardCharsets.UTF_8);
    }
    if ((clientCapabilitiesFlags & CLIENT_CONNECT_ATTRS) != 0) {
      encodeConnectionAttributes(clientConnectionAttributes, packet);
    }

    // set payload length
    int payloadLength = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, payloadLength);

    sendPacket(packet, payloadLength);
  }

  private boolean isTlsSupportedByServer(int serverCapabilitiesFlags) {
    return (serverCapabilitiesFlags & CLIENT_SSL) != 0;
  }
}

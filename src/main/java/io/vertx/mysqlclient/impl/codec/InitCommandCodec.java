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
import io.vertx.mysqlclient.impl.CharacterSetMapping;
import io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag;
import io.vertx.mysqlclient.impl.protocol.backend.ErrPacket;
import io.vertx.mysqlclient.impl.protocol.backend.InitialHandshakePacket;
import io.vertx.mysqlclient.impl.protocol.frontend.HandshakeResponse;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.mysqlclient.impl.util.Native41Authenticator;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.InitCommand;

import java.nio.charset.StandardCharsets;

import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.*;
import static io.vertx.mysqlclient.impl.protocol.backend.ErrPacket.ERROR_PACKET_HEADER;
import static io.vertx.mysqlclient.impl.protocol.backend.OkPacket.OK_PACKET_HEADER;

class InitCommandCodec extends CommandCodec<Connection, InitCommand> {

  private static final int SCRAMBLE_LENGTH = 20;
  private static final int AUTH_PLUGIN_DATA_PART1_LENGTH = 8;

  private static final int ST_CONNECTING = 0;
  private static final int ST_AUTHENTICATING = 1;
  private static final int ST_CONNECTED = 2;

  private int status = 0;

  InitCommandCodec(InitCommand cmd) {
    super(cmd);
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
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
    long connectionId = payload.readUnsignedIntLE();

    // read first part of scramble
    byte[] scramble = new byte[SCRAMBLE_LENGTH];
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
    payload.readBytes(scramble, AUTH_PLUGIN_DATA_PART1_LENGTH, SCRAMBLE_LENGTH - AUTH_PLUGIN_DATA_PART1_LENGTH);
    // 20 byte long scramble end with a '/0' character
    payload.readByte();

    String authPluginName = null;
    if (isClientPluginAuthSupported) {
      authPluginName = BufferUtils.readNullTerminatedString(payload, StandardCharsets.US_ASCII);
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

    boolean ssl = false;
    if (ssl) {
      //TODO ssl
    } else {
      if (cmd.database() != null && !cmd.database().isEmpty()) {
        encoder.clientCapabilitiesFlag |= CLIENT_CONNECT_WITH_DB;
      }
      encoder.clientCapabilitiesFlag &= initialHandshakePacket.getServerCapabilitiesFlags();
      String authMethodName = initialHandshakePacket.getAuthMethodName();
      byte[] serverScramble = initialHandshakePacket.getScramble();
      HandshakeResponse handshakeResponse = new HandshakeResponse(cmd.username(), StandardCharsets.UTF_8, cmd.password(), cmd.database(), serverScramble, encoder.clientCapabilitiesFlag, authMethodName, null);
      writeHandshakeResponseMessage(handshakeResponse);
    }
  }

  private void decodeInit1(InitCommand cmd, ByteBuf payload) {
    //TODO auth switch support
    int header = payload.readUnsignedByte();
    switch (header) {
      case OK_PACKET_HEADER:
        status = ST_CONNECTED;
        completionHandler.handle(CommandResponse.success(cmd.connection()));
        break;
      case ERROR_PACKET_HEADER:
        ErrPacket packet = GenericPacketPayloadDecoder.decodeErrPacketPayload(payload, StandardCharsets.UTF_8);
        completionHandler.handle(CommandResponse.failure(packet.getErrorMessage()));
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  void writeHandshakeResponseMessage(HandshakeResponse message) {
    encodePacket(payload -> {
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
        ByteBuf kv = encoder.chctx.alloc().ioBuffer();
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
    });
  }
}

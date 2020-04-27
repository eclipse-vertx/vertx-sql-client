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

package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.MySQLCollation;
import io.vertx.mysqlclient.impl.command.ChangeUserCommand;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.mysqlclient.impl.util.CachingSha2Authenticator;
import io.vertx.mysqlclient.impl.util.Native41Authenticator;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.*;
import static io.vertx.mysqlclient.impl.protocol.Packets.*;

class ChangeUserCommandCodec extends AuthenticationCommandBaseCodec<Void, ChangeUserCommand> {
  ChangeUserCommandCodec(ChangeUserCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendChangeUserCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    int header = payload.getUnsignedByte(payload.readerIndex());
    switch (header) {
      case AUTH_SWITCH_REQUEST_STATUS_FLAG:
        handleAuthSwitchRequest(cmd.password().getBytes(StandardCharsets.UTF_8), payload);
        break;
      case AUTH_MORE_DATA_STATUS_FLAG:
        handleAuthMoreData(cmd.password().getBytes(StandardCharsets.UTF_8), payload);
        break;
      case OK_PACKET_HEADER:
        completionHandler.handle(CommandResponse.success(null));
        break;
      case ERROR_PACKET_HEADER:
        handleErrorPacketPayload(payload);
        break;
    }
  }

  private void handleAuthSwitchRequest(byte[] password, ByteBuf payload) {
    // Protocol::AuthSwitchRequest
    payload.skipBytes(1); // status flag, always 0xFE
    String pluginName = BufferUtils.readNullTerminatedString(payload, StandardCharsets.UTF_8);
    authPluginData = new byte[NONCE_LENGTH];
    payload.readBytes(authPluginData);
    byte[] scrambledPassword;
    switch (pluginName) {
      case "mysql_native_password":
        scrambledPassword = Native41Authenticator.encode(password, authPluginData);
        break;
      case "caching_sha2_password":
        scrambledPassword = CachingSha2Authenticator.encode(password, authPluginData);
        break;
      default:
        completionHandler.handle(CommandResponse.failure(new UnsupportedOperationException("Unsupported authentication method: " + pluginName)));
        return;
    }
    sendBytesAsPacket(scrambledPassword);
  }

  private void sendChangeUserCommand() {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_CHANGE_USER);
    BufferUtils.writeNullTerminatedString(packet, cmd.username(), StandardCharsets.UTF_8);
    String password = cmd.password();
    if (password.isEmpty()) {
      packet.writeByte(0);
    } else {
      packet.writeByte(password.length());
      packet.writeCharSequence(password, StandardCharsets.UTF_8);
    }
    BufferUtils.writeNullTerminatedString(packet, cmd.database(), StandardCharsets.UTF_8);
    MySQLCollation collation = MySQLCollation.valueOfName(cmd.collation());
    int collationId = collation.collationId();
    encoder.charset = Charset.forName(collation.mappedJavaCharsetName());
    packet.writeShortLE(collationId);

    if ((encoder.clientCapabilitiesFlag & CLIENT_PLUGIN_AUTH) != 0) {
      BufferUtils.writeNullTerminatedString(packet, "mysql_native_password", StandardCharsets.UTF_8);
    }
    if ((encoder.clientCapabilitiesFlag & CLIENT_CONNECT_ATTRS) != 0) {
      Map<String, String> clientConnectionAttributes = cmd.connectionAttributes();
      if (clientConnectionAttributes != null) {
        encodeConnectionAttributes(clientConnectionAttributes, packet);
      }
    }

    // set payload length
    int lenOfPayload = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, lenOfPayload);

    sendPacket(packet, lenOfPayload);
  }
}

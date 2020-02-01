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
import io.vertx.core.Handler;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag;
import io.vertx.mysqlclient.impl.datatype.DataType;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static io.vertx.mysqlclient.impl.protocol.Packets.*;

abstract class CommandCodec<R, C extends CommandBase<R>> {

  Handler<? super CommandResponse<R>> completionHandler;
  public Throwable failure;
  public R result;
  final C cmd;
  int sequenceId;
  MySQLEncoder encoder;

  CommandCodec(C cmd) {
    this.cmd = cmd;
  }

  abstract void decodePayload(ByteBuf payload, int payloadLength);

  void encode(MySQLEncoder encoder) {
    this.encoder = encoder;
  }

  ByteBuf allocateBuffer() {
    return encoder.chctx.alloc().ioBuffer();
  }

  ByteBuf allocateBuffer(int capacity) {
    return encoder.chctx.alloc().ioBuffer(capacity);
  }

  void sendPacket(ByteBuf packet, int payloadLength) {
    if (payloadLength >= PACKET_PAYLOAD_LENGTH_LIMIT) {
      /*
         The original packet exceeds the limit of packet length, split the packet here.
         if payload length is exactly 16MBytes-1byte(0xFFFFFF), an empty packet is needed to indicate the termination.
       */
      sendSplitPacket(packet);
    } else {
      sendNonSplitPacket(packet);
    }
  }

  private void sendSplitPacket(ByteBuf packet) {
    ByteBuf payload = packet.skipBytes(4);
    while (payload.readableBytes() >= PACKET_PAYLOAD_LENGTH_LIMIT) {
      // send a packet with 0xFFFFFF length payload
      ByteBuf packetHeader = allocateBuffer(4);
      packetHeader.writeMediumLE(PACKET_PAYLOAD_LENGTH_LIMIT);
      packetHeader.writeByte(sequenceId++);
      encoder.chctx.write(packetHeader);
      encoder.chctx.write(payload.readRetainedSlice(PACKET_PAYLOAD_LENGTH_LIMIT));
    }

    // send a packet with last part of the payload
    ByteBuf packetHeader = allocateBuffer(4);
    packetHeader.writeMediumLE(payload.readableBytes());
    packetHeader.writeByte(sequenceId++);
    encoder.chctx.write(packetHeader);
    encoder.chctx.writeAndFlush(payload);
  }

  void sendNonSplitPacket(ByteBuf packet) {
    sequenceId++;
    encoder.chctx.writeAndFlush(packet);
  }

  final void sendBytesAsPacket(byte[] payload) {
    int payloadLength = payload.length;
    ByteBuf packet = allocateBuffer(payloadLength + 4);
    // encode packet header
    packet.writeMediumLE(payloadLength);
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeBytes(payload);

    sendNonSplitPacket(packet);
  }

  void handleOkPacketOrErrorPacketPayload(ByteBuf payload) {
    int header = payload.getUnsignedByte(payload.readerIndex());
    switch (header) {
      case EOF_PACKET_HEADER:
      case OK_PACKET_HEADER:
        completionHandler.handle(CommandResponse.success(null));
        break;
      case ERROR_PACKET_HEADER:
        handleErrorPacketPayload(payload);
        break;
    }
  }

  void handleErrorPacketPayload(ByteBuf payload) {
    payload.skipBytes(1); // skip ERR packet header
    int errorCode = payload.readUnsignedShortLE();
    String sqlState = null;
    if ((encoder.clientCapabilitiesFlag & CapabilitiesFlag.CLIENT_PROTOCOL_41) != 0) {
      payload.skipBytes(1); // SQL state marker will always be #
      sqlState = BufferUtils.readFixedLengthString(payload, 5, StandardCharsets.UTF_8);
    }
    String errorMessage = readRestOfPacketString(payload, StandardCharsets.UTF_8);
    completionHandler.handle(CommandResponse.failure(new MySQLException(errorMessage, errorCode, sqlState)));
  }

  OkPacket decodeOkPacketPayload(ByteBuf payload, Charset charset) {
    payload.skipBytes(1); // skip OK packet header
    long affectedRows = BufferUtils.readLengthEncodedInteger(payload);
    long lastInsertId = BufferUtils.readLengthEncodedInteger(payload);
    int serverStatusFlags = 0;
    int numberOfWarnings = 0;
    if ((encoder.clientCapabilitiesFlag & CapabilitiesFlag.CLIENT_PROTOCOL_41) != 0) {
      serverStatusFlags = payload.readUnsignedShortLE();
      numberOfWarnings = payload.readUnsignedShortLE();
    } else if ((encoder.clientCapabilitiesFlag & CapabilitiesFlag.CLIENT_TRANSACTIONS) != 0) {
      serverStatusFlags = payload.readUnsignedShortLE();
    }
    String statusInfo;
    String sessionStateInfo = null;
    if (payload.readableBytes() == 0) {
      // handle when OK packet does not contain server status info
      statusInfo = null;
    } else if ((encoder.clientCapabilitiesFlag & CapabilitiesFlag.CLIENT_SESSION_TRACK) != 0) {
      statusInfo = BufferUtils.readLengthEncodedString(payload, charset);
      if ((serverStatusFlags & ServerStatusFlags.SERVER_SESSION_STATE_CHANGED) != 0) {
        sessionStateInfo = BufferUtils.readLengthEncodedString(payload, charset);
      }
    } else {
      statusInfo = readRestOfPacketString(payload, charset);
    }
    return new OkPacket(affectedRows, lastInsertId, serverStatusFlags, numberOfWarnings, statusInfo, sessionStateInfo);
  }

  EofPacket decodeEofPacketPayload(ByteBuf payload) {
    payload.skipBytes(1); // skip EOF_Packet header
    int numberOfWarnings = payload.readUnsignedShortLE();
    int serverStatusFlags = payload.readUnsignedShortLE();
    return new EofPacket(numberOfWarnings, serverStatusFlags);
  }

  String readRestOfPacketString(ByteBuf payload, Charset charset) {
    return BufferUtils.readFixedLengthString(payload, payload.readableBytes(), charset);
  }

  ColumnDefinition decodeColumnDefinitionPacketPayload(ByteBuf payload) {
    String catalog = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    String schema = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    String table = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    String orgTable = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    String name = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    String orgName = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    long lengthOfFixedLengthFields = BufferUtils.readLengthEncodedInteger(payload);
    int characterSet = payload.readUnsignedShortLE();
    long columnLength = payload.readUnsignedIntLE();
    DataType type = DataType.valueOf(payload.readUnsignedByte());
    int flags = payload.readUnsignedShortLE();
    byte decimals = payload.readByte();
    return new ColumnDefinition(catalog, schema, table, orgTable, name, orgName, characterSet, columnLength, type, flags, decimals);
  }

  void skipEofPacketIfNeeded(ByteBuf payload) {
    if (!isDeprecatingEofFlagEnabled()) {
      payload.skipBytes(5);
    }
  }

  boolean isDeprecatingEofFlagEnabled() {
    return (encoder.clientCapabilitiesFlag & CapabilitiesFlag.CLIENT_DEPRECATE_EOF) != 0;
  }
}

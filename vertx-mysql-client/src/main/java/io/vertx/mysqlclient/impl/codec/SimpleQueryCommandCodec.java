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
import io.vertx.mysqlclient.impl.datatype.DataFormat;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static io.vertx.mysqlclient.impl.protocol.Packets.*;

class SimpleQueryCommandCodec<T> extends QueryCommandBaseCodec<T, SimpleQueryCommand<T>> {

  SimpleQueryCommandCodec(SimpleQueryCommand<T> cmd) {
    super(cmd, DataFormat.TEXT);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendQueryCommand();
  }

  @Override
  protected void handleInitPacket(ByteBuf payload) {
    // may receive ERR_Packet, OK_Packet, LOCAL INFILE Request, Text Resultset
    int firstByte = payload.getUnsignedByte(payload.readerIndex());
    if (firstByte == OK_PACKET_HEADER) {
      OkPacket okPacket = decodeOkPacketPayload(payload);
      handleSingleResultsetDecodingCompleted(okPacket.serverStatusFlags(), okPacket.affectedRows(), okPacket.lastInsertId());
    } else if (firstByte == ERROR_PACKET_HEADER) {
      handleErrorPacketPayload(payload);
    } else if (firstByte == 0xFB) {
      payload.skipBytes(1);
      String filename = readRestOfPacketString(payload, StandardCharsets.UTF_8);
      sendPackets(filename);
    } else {
      handleResultsetColumnCountPacketBody(payload);
    }
  }

  private void sendQueryCommand() {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_QUERY);
    packet.writeCharSequence(cmd.sql(), encoder.encodingCharset);

    // set payload length
    int payloadLength = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, payloadLength);

    sendPacket(packet, payloadLength);
  }

  private void sendPackets(String filePath) {
    /*
      We will try to use zero-copy file transfer in order to gain better performance.
      File content needs to be wrapped in MySQL packets so we calculate the length of the file and then send a pre-calculated packet header with the content.
     */
    File file = new File(filePath);
    long length = file.length();
    // 16MB+ packet necessary?

    ByteBuf packetHeader = allocateBuffer(4);
    packetHeader.writeMediumLE((int) length);
    packetHeader.writeByte(sequenceId++);
    encoder.chctx.write(packetHeader);
    encoder.socketConnection.socket().sendFile(filePath, 0)
      .onComplete(v -> {
        // an empty packet needs to be sent after the file is sent in MySQL packets
        sendEmptyPacket();
      });
  }

  private void sendEmptyPacket() {
    ByteBuf packet = allocateBuffer(4);
    // encode packet header
    packet.writeMediumLE(0);
    packet.writeByte(sequenceId);

    sendNonSplitPacket(packet);
  }
}

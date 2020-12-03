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
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.impl.datatype.DataFormat;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
      handleLocalInfile(payload);
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

  private void handleLocalInfile(ByteBuf payload) {
    payload.skipBytes(1);
    String filename = readRestOfPacketString(payload, StandardCharsets.UTF_8);
    /*
      We will try to use zero-copy file transfer in order to gain better performance.
      File content needs to be wrapped in MySQL packets so we calculate the length of the file and then send a pre-calculated packet header with the content.
     */
    File file = new File(filename);
    long fileLength = file.length();

    List<Supplier<Future<Void>>> sendingFileInPacketContList = new ArrayList<>();

    int offset = 0; // file write index
    int remainingLen = (int) fileLength; // remaining file length

    while (remainingLen >= PACKET_PAYLOAD_LENGTH_LIMIT) {
      final int currentOffset = offset;
      sendingFileInPacketContList.add(() -> sendFileInPacket(filename, currentOffset, PACKET_PAYLOAD_LENGTH_LIMIT));
      remainingLen -= PACKET_PAYLOAD_LENGTH_LIMIT;
      offset += PACKET_PAYLOAD_LENGTH_LIMIT;
    }

    final int tailLength = remainingLen;
    final int tailOffset = offset;

    Future<Void> cont = Future.succeededFuture();

    for (Supplier<Future<Void>> futureSupplier : sendingFileInPacketContList) {
      // send the sliced packet with size equal to packet limit
      cont = cont.flatMap(v -> futureSupplier.get());
    }

    if (tailLength > 0) {
      // the last sliced packet being sent whose size is less than the packet limit
      cont = cont.flatMap(v -> sendFileInPacket(filename, tailOffset, tailLength));
    } else {
      // empty file or nothing else to send
    }

    // an empty packet needs to be sent after the whole file is sent in MySQL packets
    cont.onComplete(v -> sendEmptyPacket());
  }

  private Future<Void> sendFileInPacket(String filename, int offset, int length) {
    Promise<Void> promise = Promise.promise();
    ByteBuf packetHeader = allocateBuffer(4);
    packetHeader.writeMediumLE(length);
    packetHeader.writeByte(sequenceId++);
    encoder.chctx.write(packetHeader);
    encoder.socketConnection.socket().sendFile(filename, offset, length, promise);
    return promise.future();
  }

  private void sendEmptyPacket() {
    ByteBuf packet = allocateBuffer(4);
    // encode packet header
    packet.writeMediumLE(0);
    packet.writeByte(sequenceId);

    sendNonSplitPacket(packet);
  }
}

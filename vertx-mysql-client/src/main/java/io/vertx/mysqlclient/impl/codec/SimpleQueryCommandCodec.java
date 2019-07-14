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
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

import java.nio.charset.StandardCharsets;

import static io.vertx.mysqlclient.impl.codec.Packets.*;

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
      OkPacket okPacket = decodeOkPacketPayload(payload, StandardCharsets.UTF_8);
      handleSingleResultsetDecodingCompleted(okPacket.serverStatusFlags(), (int) okPacket.affectedRows());
    } else if (firstByte == ERROR_PACKET_HEADER) {
      handleErrorPacketPayload(payload);
    } else if (firstByte == 0xFB) {
      //TODO LOCAL INFILE Request support
      completionHandler.handle(CommandResponse.failure(new UnsupportedOperationException("LOCAL INFILE is not supported for now")));
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
    packet.writeCharSequence(cmd.sql(), StandardCharsets.UTF_8);

    // set payload length
    int payloadLength = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, payloadLength);

    sendPacket(packet, payloadLength);
  }
}

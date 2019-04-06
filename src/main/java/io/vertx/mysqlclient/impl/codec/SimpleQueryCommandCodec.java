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
import io.vertx.mysqlclient.impl.codec.datatype.DataFormat;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

import java.nio.charset.StandardCharsets;

import static io.vertx.mysqlclient.impl.protocol.backend.ErrPacket.*;
import static io.vertx.mysqlclient.impl.protocol.backend.OkPacket.*;

class SimpleQueryCommandCodec<T> extends QueryCommandBaseCodec<T, SimpleQueryCommand<T>> {

  SimpleQueryCommandCodec(SimpleQueryCommand<T> cmd) {
    super(cmd, DataFormat.TEXT);
  }

  @Override
  void encodePayload(MySQLEncoder encoder) {
    super.encodePayload(encoder);
    ByteBuf payload = encoder.chctx.alloc().ioBuffer();
    payload.writeByte(CommandType.COM_QUERY);
    payload.writeCharSequence(cmd.sql(), StandardCharsets.UTF_8);
    encoder.writePacketAndFlush(sequenceId++, payload);
  }

  @Override
  protected void handleInitPacket(ByteBuf payload) {
    // may receive ERR_Packet, OK_Packet, LOCAL INFILE Request, Text Resultset
    int firstByte = payload.getUnsignedByte(payload.readerIndex());
    if (firstByte == OK_PACKET_HEADER) {
      handleSingleResultsetDecodingCompleted(payload);
    } else if (firstByte == ERROR_PACKET_HEADER) {
      handleErrorPacketPayload(payload);
    } else if (firstByte == 0xFB) {
      //TODO LOCAL INFILE Request support
    } else {
      handleResultsetColumnCountPacketBody(payload);
    }
  }
}

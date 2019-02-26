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
package io.reactiverse.pgclient.impl.my.codec;

import io.netty.buffer.ByteBuf;
import io.reactiverse.mysqlclient.impl.ColumnMetadata;
import io.reactiverse.mysqlclient.impl.RowResultDecoder;
import io.reactiverse.mysqlclient.impl.codec.GenericPacketPayloadDecoder;
import io.reactiverse.mysqlclient.impl.codec.datatype.DataFormat;
import io.reactiverse.mysqlclient.impl.codec.datatype.DataType;
import io.reactiverse.mysqlclient.impl.protocol.CommandType;
import io.reactiverse.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.reactiverse.mysqlclient.impl.protocol.backend.OkPacket;
import io.reactiverse.mysqlclient.impl.util.BufferUtils;
import io.reactiverse.pgclient.impl.command.CommandResponse;
import io.reactiverse.pgclient.impl.command.SimpleQueryCommand;

import java.nio.charset.StandardCharsets;

import static io.reactiverse.mysqlclient.impl.protocol.backend.EofPacket.EOF_PACKET_HEADER;
import static io.reactiverse.mysqlclient.impl.protocol.backend.ErrPacket.ERROR_PACKET_HEADER;
import static io.reactiverse.mysqlclient.impl.protocol.backend.OkPacket.OK_PACKET_HEADER;

class SimpleQueryCommandCodec<T> extends QueryCommandBaseCodec<T, SimpleQueryCommand<T>> {

  SimpleQueryCommandCodec(SimpleQueryCommand<T> cmd) {
    super(cmd, DataFormat.TEXT);
  }

  @Override
  void encodePayload(MyEncoder encoder) {
    super.encodePayload(encoder);
    ByteBuf payload = encoder.chctx.alloc().ioBuffer();
    payload.writeByte(CommandType.COM_QUERY);
    payload.writeCharSequence(cmd.sql(), StandardCharsets.UTF_8);
    encoder.writePacketAndFlush(sequenceId++, payload);
  }
}

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
import io.vertx.mysqlclient.impl.codec.datatype.DataType;
import io.vertx.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.vertx.mysqlclient.impl.protocol.backend.ErrPacket;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.core.Handler;

import java.nio.charset.StandardCharsets;

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

  void encodePayload(MySQLEncoder encoder) {
    this.encoder = encoder;
  }

  abstract void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId);

  protected ColumnDefinition decodeColumnDefinitionPacketPayload(ByteBuf payload) {
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

  protected void handleErrorPacketPayload(ByteBuf payload) {
    // we have checked the header should be ERROR_PACKET_HEADER
    payload.readUnsignedByte(); // skip header
    ErrPacket packet = GenericPacketPayloadDecoder.decodeErrPacketBody(payload, StandardCharsets.UTF_8);
    completionHandler.handle(CommandResponse.failure(packet.toException()));
  }
}

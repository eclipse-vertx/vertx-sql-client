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
import io.netty.channel.ChannelHandlerContext;
import io.vertx.mysqlclient.impl.protocol.backend.ErrPacket;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.core.Handler;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

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

  abstract void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId);

  void encode(MySQLEncoder encoder) {
    this.encoder = encoder;
  }

  void encodePacket(Consumer<ByteBuf> payloadEncoder) {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId++);

    // encode packet payload
    payloadEncoder.accept(packet);

    // set payload length
    int lenOfPayload = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, lenOfPayload);

    encoder.chctx.writeAndFlush(packet);
  }

  ByteBuf allocateBuffer() {
    return encoder.chctx.alloc().ioBuffer();
  }

  void handleErrorPacketPayload(ByteBuf payload) {
    // we have checked the header should be ERROR_PACKET_HEADER
    payload.readUnsignedByte(); // skip header
    ErrPacket packet = GenericPacketPayloadDecoder.decodeErrPacketPayload(payload, StandardCharsets.UTF_8);
    completionHandler.handle(CommandResponse.failure(packet.toException()));
  }
}

/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.mssqlclient.impl.protocol.MessageType;
import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

import java.nio.charset.StandardCharsets;

import static io.vertx.mssqlclient.impl.codec.TokenType.*;
import static io.vertx.mssqlclient.impl.protocol.MessageStatus.END_OF_MESSAGE;
import static io.vertx.mssqlclient.impl.protocol.MessageStatus.NORMAL;

class SQLBatchCommandCodec<T> extends QueryCommandBaseCodec<T, SimpleQueryCommand<T>> {
  SQLBatchCommandCodec(SimpleQueryCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(TdsMessageEncoder encoder) {
    super.encode(encoder);
    sendBatchClientRequest();
  }

  @Override
  void decodeMessage(TdsMessage message, TdsMessageEncoder encoder) {
    ByteBuf messageBody = message.content();
    while (messageBody.isReadable()) {
      int tokenType = messageBody.readUnsignedByte();
      switch (tokenType) {
        case COLMETADATA:
          MSSQLRowDesc rowDesc = decodeColmetadataToken(messageBody);
          rowResultDecoder = new RowResultDecoder<>(cmd.collector(), rowDesc);
          break;
        case ROW:
          handleRow(messageBody);
          break;
        case NBCROW:
          handleNbcRow(messageBody);
          break;
        case DONE:
        case DONEPROC:
          short status = messageBody.readShortLE();
          short curCmd = messageBody.readShortLE();
          long doneRowCount = messageBody.readLongLE();
          handleResultSetDone((int) doneRowCount);
          break;
        case INFO:
        case ORDER:
          int tokenLength = messageBody.readUnsignedShortLE();
          messageBody.skipBytes(tokenLength);
          break;
        case ERROR:
          handleErrorToken(messageBody);
          break;
        case ENVCHANGE:
          handleEnvChangeToken(messageBody);
          break;
        default:
          throw new UnsupportedOperationException("Unsupported token: " + tokenType);
      }
    }
    complete();
  }

  private void sendBatchClientRequest() {
    ChannelHandlerContext chctx = encoder.chctx;

    ByteBuf packet = chctx.alloc().ioBuffer();

    // packet header
    packet.writeByte(MessageType.SQL_BATCH.value());
    packet.writeByte(NORMAL | END_OF_MESSAGE);
    int packetLenIdx = packet.writerIndex();
    packet.writeShort(0); // set length later
    packet.writeShort(0x00);
    packet.writeByte(0x00); // FIXME packet ID
    packet.writeByte(0x00);

    int start = packet.writerIndex();
    packet.writeIntLE(0x00); // TotalLength for ALL_HEADERS
    encodeTransactionDescriptor(packet);
    // set TotalLength for ALL_HEADERS
    packet.setIntLE(start, packet.writerIndex() - start);

    // SQLText
    packet.writeCharSequence(cmd.sql(), StandardCharsets.UTF_16LE);

    int packetLen = packet.writerIndex() - packetLenIdx + 2;
    packet.setShort(packetLenIdx, packetLen);

    chctx.writeAndFlush(packet, encoder.chctx.voidPromise());
  }
}

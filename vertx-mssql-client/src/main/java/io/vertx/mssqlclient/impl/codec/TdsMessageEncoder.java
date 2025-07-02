/*
 * Copyright (c) 2011-2024 Contributors to the Eclipse Foundation
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
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import static io.vertx.mssqlclient.MSSQLConnectOptions.MIN_PACKET_SIZE;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.END_OF_MESSAGE;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.NORMAL;
import static io.vertx.mssqlclient.impl.codec.TdsPacket.PACKET_HEADER_SIZE;
import static java.lang.Math.max;

public class TdsMessageEncoder extends ChannelOutboundHandlerAdapter {
  private final TdsMessageCodec tdsMessageCodec;

  private ChannelHandlerContext chctx;
  private int payloadMaxLength;

  public TdsMessageEncoder(TdsMessageCodec tdsMessageCodec, int desiredPacketSize) {
    this.tdsMessageCodec = tdsMessageCodec;
    setPacketSize(desiredPacketSize);
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    chctx = ctx;
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof MSSQLCommandMessage<?, ?>) {
      MSSQLCommandMessage<?, ?> cmd = (MSSQLCommandMessage<?, ?>) msg;
      cmd.tdsMessageCodec = tdsMessageCodec;
      write(cmd);
    } else {
      super.write(ctx, msg, promise);
    }
  }

  void write(MSSQLCommandMessage<?, ?> cmd) {
    if (tdsMessageCodec.add(cmd)) {
      cmd.encode();
    }
  }

  public int packetSize() {
    return payloadMaxLength + PACKET_HEADER_SIZE;
  }

  void setPacketSize(int packetSize) {
    int packetMaxLength = max(MIN_PACKET_SIZE, packetSize);
    payloadMaxLength = packetMaxLength - PACKET_HEADER_SIZE;
  }

  void encodeHeaders(ByteBuf content) {
    int startIdx = content.writerIndex();
    content.writeIntLE(0x00); // TotalLength for ALL_HEADERS
    encodeTransactionDescriptor(content);
    // set TotalLength for ALL_HEADERS
    content.setIntLE(startIdx, content.writerIndex() - startIdx);
  }

  private void encodeTransactionDescriptor(ByteBuf content) {
    content.writeIntLE(18); // HeaderLength is always 18
    content.writeShortLE(0x0002); // HeaderType
    content.writeLongLE(tdsMessageCodec.transactionDescriptor());
    content.writeIntLE(1);
  }

  void writeTdsMessage(short messageType, ByteBuf tdsMessageContent) {
    int remaining = tdsMessageContent.writerIndex();
    while (remaining > 0) {
      int payloadLength = Math.min(remaining, payloadMaxLength);
      tdsMessageContent.writerIndex(tdsMessageContent.readerIndex() + payloadLength);
      short status = NORMAL;
      ByteBuf payload;
      if (payloadLength == remaining) {
        status |= END_OF_MESSAGE;
        payload = tdsMessageContent;
      } else {
        payload = tdsMessageContent.readRetainedSlice(payloadLength);
      }
      writeTdsPacket(messageType, status, payloadLength, payload);
      remaining -= payloadLength;
    }
  }

  private void writeTdsPacket(short messageType, short status, int length, ByteBuf payload) {
    ByteBuf header = chctx.alloc().ioBuffer(PACKET_HEADER_SIZE);
    header.writeByte(messageType);
    header.writeByte(status);
    header.writeShort(PACKET_HEADER_SIZE + length);
    header.writeZero(4);
    chctx.write(header, chctx.voidPromise());
    if (status == END_OF_MESSAGE) {
      chctx.writeAndFlush(payload, chctx.voidPromise());
    } else {
      chctx.write(payload, chctx.voidPromise());
    }
  }
}

/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
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
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.vertx.mssqlclient.impl.command.PreLoginCommand;
import io.vertx.sqlclient.impl.command.*;

import static io.vertx.mssqlclient.MSSQLConnectOptions.MIN_PACKET_SIZE;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.END_OF_MESSAGE;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.NORMAL;
import static io.vertx.mssqlclient.impl.codec.TdsPacket.PACKET_HEADER_SIZE;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class TdsMessageEncoder extends ChannelOutboundHandlerAdapter {
  private final TdsMessageCodec tdsMessageCodec;

  private ChannelHandlerContext chctx;
  private ByteBufAllocator alloc;
  private int payloadMaxLength;

  public TdsMessageEncoder(TdsMessageCodec tdsMessageCodec, int packetSize) {
    this.tdsMessageCodec = tdsMessageCodec;
    setPacketSize(packetSize);
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    chctx = ctx;
    alloc = chctx.alloc();
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof CommandBase<?>) {
      CommandBase<?> cmd = (CommandBase<?>) msg;
      write(cmd);
    } else {
      super.write(ctx, msg, promise);
    }
  }

  void write(CommandBase<?> cmd) {
    MSSQLCommandCodec<?, ?> codec = wrap(cmd);
    codec.completionHandler = resp -> {
      MSSQLCommandCodec<?, ?> c = this.tdsMessageCodec.poll();
      resp.cmd = (CommandBase) c.cmd;
      chctx.fireChannelRead(resp);
    };
    this.tdsMessageCodec.add(codec);
    codec.encode();
  }

  private MSSQLCommandCodec<?, ?> wrap(CommandBase<?> cmd) {
    if (cmd instanceof PreLoginCommand) {
      return new PreLoginCommandCodec(tdsMessageCodec, (PreLoginCommand) cmd);
    } else if (cmd instanceof InitCommand) {
      return new InitCommandCodec(tdsMessageCodec, (InitCommand) cmd);
    } else if (cmd instanceof SimpleQueryCommand) {
      return new SQLBatchCommandCodec<>(tdsMessageCodec, (SimpleQueryCommand<?>) cmd);
    } else if (cmd instanceof PrepareStatementCommand) {
      return new PrepareStatementCodec(tdsMessageCodec, (PrepareStatementCommand) cmd);
    } else if (cmd instanceof ExtendedQueryCommand) {
      return ExtendedQueryCommandBaseCodec.create(tdsMessageCodec, (ExtendedQueryCommand<?>) cmd);
    } else if (cmd instanceof CloseStatementCommand) {
      return new CloseStatementCommandCodec(tdsMessageCodec, (CloseStatementCommand) cmd);
    } else if (cmd == CloseConnectionCommand.INSTANCE) {
      return new CloseConnectionCommandCodec(tdsMessageCodec, (CloseConnectionCommand) cmd);
    } else if (cmd instanceof CloseCursorCommand) {
      return new CloseCursorCommandCodec(tdsMessageCodec, (CloseCursorCommand) cmd);
    } else {
      throw new UnsupportedOperationException();
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
    try {
      int tdsMessageLength = tdsMessageContent.writerIndex();
      int numPackets = (tdsMessageLength / payloadMaxLength) + ((tdsMessageLength % payloadMaxLength) == 0 ? 0 : 1);

      for (int i = 0; i < numPackets; i++) {
        int start = i * payloadMaxLength;
        int length = min(tdsMessageLength - start, payloadMaxLength);
        ByteBuf slice = tdsMessageContent.retainedSlice(start, length);
        short status = NORMAL;
        if (i == numPackets - 1) {
          status |= END_OF_MESSAGE;
        }
        writeTdsPacket(messageType, status, length, slice);
      }
    } finally {
      ReferenceCountUtil.release(tdsMessageContent);
    }
  }

  private void writeTdsPacket(short messageType, short status, int length, ByteBuf payload) {
    ByteBuf header = alloc.ioBuffer(8);
    header.writeByte(messageType);
    header.writeByte(status);
    header.writeShort(PACKET_HEADER_SIZE + length);
    header.writeZero(4);
    chctx.write(header, chctx.voidPromise());
    chctx.writeAndFlush(payload, chctx.voidPromise());
  }
}

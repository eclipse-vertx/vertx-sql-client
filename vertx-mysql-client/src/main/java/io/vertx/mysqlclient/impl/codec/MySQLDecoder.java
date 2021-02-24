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

package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.vertx.mysqlclient.impl.protocol.MySQLPacket;

import java.util.ArrayDeque;
import java.util.Deque;

import static io.vertx.mysqlclient.impl.protocol.Packets.PACKET_PAYLOAD_LENGTH_LIMIT;

public class MySQLDecoder extends ChannelInboundHandlerAdapter {

  private final ArrayDeque<CommandCodec<?, ?>> inflight;
  private ChannelHandlerContext chctx;

  private ByteBuf accumulationBuffer;

  // this holds a queue of MySQL retained sliced packets which internally share the reference count with the accumulation buffer
  private final Deque<MySQLPacket> compositePacket = new ArrayDeque<>();

  public MySQLDecoder(ArrayDeque<CommandCodec<?, ?>> inflight) {
    this.inflight = inflight;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    this.chctx = ctx;
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    if (accumulationBuffer != null) {
      ByteBuf buf = this.accumulationBuffer;
      this.accumulationBuffer = null;
      buf.release();
    }

    if (!compositePacket.isEmpty()) {
      Deque<MySQLPacket> compositePacket = this.compositePacket;
      for (MySQLPacket mySQLPacket : compositePacket) {
        mySQLPacket.release();
      }
      this.compositePacket.clear();
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf buffer = (ByteBuf) msg;

    if (accumulationBuffer == null) {
      accumulationBuffer = buffer;
    } else {
      CompositeByteBuf composite;
      if (accumulationBuffer instanceof CompositeByteBuf) {
        composite = (CompositeByteBuf) accumulationBuffer;
      } else {
        composite = ctx.alloc().compositeDirectBuffer();
        composite.addComponent(true, accumulationBuffer);
        accumulationBuffer = composite;
      }
      composite.addComponent(true, buffer);
    }

    while (true) {
      int readableBytes = accumulationBuffer.readableBytes();
      if (readableBytes == 0) {
        // clean the accumulation buffer
        accumulationBuffer.release();
        accumulationBuffer = null;
        return;
      }
      if (readableBytes < 4) {
        // not a full packet, missing packet header, wait for next read event
        return;
      }
      // packet start
      int packetStartIdx = accumulationBuffer.readerIndex();
      int packetPayloadLength = accumulationBuffer.getUnsignedMediumLE(packetStartIdx);
      short sequenceId = accumulationBuffer.getUnsignedByte(packetStartIdx + 3);
      if (accumulationBuffer.writerIndex() < packetStartIdx + 4 + packetPayloadLength) {
        // not a full packet, missing full packet content, wait for next read event
        return;
      } else {
        checkDecoding(packetPayloadLength, sequenceId, accumulationBuffer, packetStartIdx);
      }
    }
  }

  private void checkDecoding(int payloadLength,
                             short sequenceId,
                             ByteBuf accumulationBuffer,
                             int packetStartIdx) {
    // payload length should never be greater than packet payload length limit
    if (payloadLength == PACKET_PAYLOAD_LENGTH_LIMIT) {
      // just append it to the composite packet without any other operation
      appendMySQLSlicedPacket(PACKET_PAYLOAD_LENGTH_LIMIT, sequenceId, accumulationBuffer, packetStartIdx);
    } else {
      // check if it's a composite packet last packet
      if (!compositePacket.isEmpty()) {
        appendMySQLSlicedPacket(payloadLength, sequenceId, accumulationBuffer, packetStartIdx);
        // now decode the composite packet
        handleCompositePacket();
      } else {
        // a single packet, just decode it directly
        int payloadStartIdx = packetStartIdx + 4;
        try {
          handleSinglePacketPayload(payloadLength, sequenceId, accumulationBuffer, payloadStartIdx);
        } catch (Exception ex) {
          chctx.fireExceptionCaught(ex);
        } finally {
          accumulationBuffer.readerIndex(payloadStartIdx + payloadLength);
        }
      }
    }
  }

  private void appendMySQLSlicedPacket(int payloadLength, short sequenceId, ByteBuf accumulationBuffer, int packetStartIdx) {
    ByteBuf slicedPacketContent = accumulationBuffer.retainedSlice(packetStartIdx + 4, payloadLength);
    accumulationBuffer.readerIndex(packetStartIdx + 4 + payloadLength);
    MySQLPacket mySQLPacket = new MySQLPacket(payloadLength, sequenceId, slicedPacketContent);
    compositePacket.add(mySQLPacket);
  }

  private void handleCompositePacket() {
    int payloadLength = 0;
    short sequenceId = 0;
    CompositeByteBuf compositePacketPayload = chctx.alloc().compositeDirectBuffer(compositePacket.size());
    for (MySQLPacket mySQLPacket : compositePacket) {
      payloadLength += mySQLPacket.payloadLength();
      sequenceId = mySQLPacket.sequenceId();
      compositePacketPayload.addComponent(true, mySQLPacket.content());
    }
    handleSinglePacketPayload(payloadLength, sequenceId, compositePacketPayload, 0);
    compositePacketPayload.release(); // this will also decrease the ref counts of sub retained sliced packet
    compositePacket.clear();
  }

  /**
   * Decode a single packet in the accumulation buffer, we have already checked the payload length.
   * Note the accumulation buffer reader index needs to be set at the packet payload readerIndex start.
   */
  private void handleSinglePacketPayload(int payloadLength,
                                         short sequenceId,
                                         ByteBuf payloadBuffer,
                                         int payloadStartIdx) {
    checkFireAndForgetCommands();
    CommandCodec<?, ?> ctx = inflight.peek();
    ctx.sequenceId = sequenceId + 1;
    payloadBuffer.readerIndex(payloadStartIdx);
    ctx.decodePayload(payloadBuffer, payloadLength);
    checkFireAndForgetCommands();
  }

  private void checkFireAndForgetCommands() {
    // check if there is any completed command
    CommandCodec<?, ?> commandCodec;
    while ((commandCodec = inflight.peek()) != null && commandCodec.receiveNoResponsePacket()) {
      commandCodec.decodePayload(null, 0);
    }
  }
}

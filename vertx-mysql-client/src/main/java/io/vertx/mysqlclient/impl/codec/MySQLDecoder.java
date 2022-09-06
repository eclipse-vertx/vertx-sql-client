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

package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayDeque;

import static io.vertx.mysqlclient.impl.protocol.Packets.PACKET_PAYLOAD_LENGTH_LIMIT;

class MySQLDecoder extends ChannelInboundHandlerAdapter {

  private final ArrayDeque<CommandCodec<?, ?>> inflight;

  private ByteBufAllocator alloc;
  private ByteBuf payload;
  private short sequenceId;

  MySQLDecoder(ArrayDeque<CommandCodec<?, ?>> inflight) {
    this.inflight = inflight;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    alloc = ctx.alloc();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf packet = (ByteBuf) msg;
    int payloadLength = packet.readUnsignedMediumLE();
    sequenceId = packet.readUnsignedByte();
    if (payload != null) {
      CompositeByteBuf compositeByteBuf = (CompositeByteBuf) payload;
      compositeByteBuf.addComponent(true, packet.slice());
    } else if (payloadLength >= PACKET_PAYLOAD_LENGTH_LIMIT) {
      payload = alloc.compositeDirectBuffer().addComponent(true, packet.slice());
    } else {
      payload = packet;
    }
    if (payloadLength < PACKET_PAYLOAD_LENGTH_LIMIT) {
      decodePackets();
    }
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    releaseMessage();
  }

  private void releaseMessage() {
    if (payload != null) {
      payload.release();
      payload = null;
    }
  }

  private void decodePackets() {
    try {
      MySQLCodec.checkFireAndForgetCommands(inflight);
      CommandCodec<?, ?> ctx = inflight.peek();
      if (ctx == null) {
        throw new IllegalStateException("No command codec for packet");
      }
      ctx.sequenceId = sequenceId + 1;
      ctx.decodePayload(payload, payload.readableBytes());
      MySQLCodec.checkFireAndForgetCommands(inflight);
    } finally {
      releaseMessage();
    }
  }
}

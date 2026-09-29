/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
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
import io.vertx.core.impl.NoStackTraceThrowable;

import static io.vertx.mysqlclient.impl.protocol.Packets.PACKET_PAYLOAD_LENGTH_LIMIT;

class MySQLDecoder extends ChannelInboundHandlerAdapter {

  private final MySQLCodec codec;
  private final int maxAllowedPacket;
  private ByteBuf payload;
  private short sequenceId;

  MySQLDecoder(MySQLCodec codec, int maxAllowedPacket) {
    this.codec = codec;
    this.maxAllowedPacket = maxAllowedPacket;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf packet = (ByteBuf) msg;
    int payloadLength = packet.readUnsignedMediumLE();
    sequenceId = packet.readUnsignedByte();
    if (payload != null) {
      CompositeByteBuf compositeByteBuf = (CompositeByteBuf) payload;
      compositeByteBuf.addComponent(true, packet);
      if (compositeByteBuf.readableBytes() > maxAllowedPacket) {
        releaseMessage();
        codec.failAndClose(new NoStackTraceThrowable("MySQL reassembled message size exceeds limit of " + maxAllowedPacket + " bytes"));
        return;
      }
    } else if (payloadLength >= PACKET_PAYLOAD_LENGTH_LIMIT) {
      payload = ctx.alloc().compositeDirectBuffer().addComponent(true, packet);
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
      codec.checkFireAndForgetCommands();
      CommandCodec<?, ?> ctx = codec.peek();
      if (ctx == null) {
        throw new IllegalStateException("No command codec for packet");
      }
      ctx.sequenceId = sequenceId + 1;
      ctx.decodePayload(payload, payload.readableBytes());
      codec.checkFireAndForgetCommands();
    } finally {
      releaseMessage();
    }
  }
}

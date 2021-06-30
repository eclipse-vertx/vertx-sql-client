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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import static io.vertx.mssqlclient.MSSQLConnectOptions.MAX_PACKET_SIZE;
import static io.vertx.mssqlclient.impl.codec.TdsPacket.PACKET_HEADER_SIZE;

public class TdsPacketDecoder extends LengthFieldBasedFrameDecoder {

  public TdsPacketDecoder() {
    super(MAX_PACKET_SIZE, 2, 2, -4, 0);
  }

  @Override
  protected TdsPacket decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    ByteBuf byteBuf = (ByteBuf) super.decode(ctx, in);
    if (byteBuf == null) {
      return null;
    }

    short type = in.getUnsignedByte(0);
    short status = in.getUnsignedByte(1);
    int length = in.getUnsignedShort(2);

    ByteBuf data = byteBuf.slice(PACKET_HEADER_SIZE, length - PACKET_HEADER_SIZE);

    return new TdsPacket(type, status, length, data);
  }
}

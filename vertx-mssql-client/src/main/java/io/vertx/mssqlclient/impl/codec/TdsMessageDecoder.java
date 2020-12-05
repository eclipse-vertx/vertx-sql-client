/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.impl.protocol.MessageStatus;
import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.vertx.mssqlclient.impl.protocol.TdsPacket;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;

class TdsMessageDecoder extends MessageToMessageDecoder<TdsPacket> {
  private final ArrayDeque<MSSQLCommandCodec<?, ?>> inflight;
  private final TdsMessageEncoder encoder;

  private TdsMessage message;

  TdsMessageDecoder(ArrayDeque<MSSQLCommandCodec<?, ?>> inflight, TdsMessageEncoder encoder) {
    this.inflight = inflight;
    this.encoder = encoder;
  }

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, TdsPacket tdsPacket, List<Object> list) throws Exception {
    // assemble packets
    if (tdsPacket.status() == MessageStatus.END_OF_MESSAGE) {
      if (message == null) {
        message = TdsMessage.newTdsMessageFromSinglePacket(tdsPacket);
        decodeMessage();
      } else {
        // last packet of this message
        CompositeByteBuf messageData = (CompositeByteBuf) message.content();
        messageData.addComponent(true, tdsPacket.content());
        decodeMessage();
      }
    } else {
      if (message == null) {
        // first packet of this message and there will be more packets
        CompositeByteBuf messageData = channelHandlerContext.alloc().compositeDirectBuffer();
        messageData.addComponent(true, tdsPacket.content());
        message = TdsMessage.newTdsMessage(tdsPacket.type(), tdsPacket.status(), tdsPacket.processId(), messageData);
      } else {
        CompositeByteBuf messageData = (CompositeByteBuf) message.content();
        messageData.addComponent(true, tdsPacket.content());
      }
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    clearInflightCommands(ctx, "Fail to read any response from the server, the underlying connection might get lost unexpectedly.");
    super.channelInactive(ctx);
  }

  private void decodeMessage() {
    inflight.peek().decodeMessage(message, encoder);
    this.message = null;
  }

  private void clearInflightCommands(ChannelHandlerContext ctx, String failureMsg) {
    // SQL Server provides a rollback mechanism, this is used for low level connection getting lost.
    for (Iterator<MSSQLCommandCodec<?, ?>> it = inflight.iterator(); it.hasNext();) {
      MSSQLCommandCodec<?, ?> codec = it.next();
      it.remove();
      CommandResponse<Object> failure = CommandResponse.failure(failureMsg);
      failure.cmd = (CommandBase) codec.cmd;
      ctx.fireChannelRead(failure);
    }
  }
}

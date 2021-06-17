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
import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.vertx.mssqlclient.impl.protocol.client.rpc.ProcId;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

import static io.vertx.mssqlclient.impl.codec.DataType.INTN;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.END_OF_MESSAGE;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.NORMAL;
import static io.vertx.mssqlclient.impl.codec.MessageType.RPC;
import static io.vertx.mssqlclient.impl.codec.TokenType.*;

class CloseStatementCommandCodec extends MSSQLCommandCodec<Void, CloseStatementCommand> {

  CloseStatementCommandCodec(CloseStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(TdsMessageEncoder encoder) {
    super.encode(encoder);
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.statement();
    if (ps.handle > 0) {
      sendUnprepareRequest();
    } else {
      completionHandler.handle(CommandResponse.success(null));
    }
  }

  @Override
  void decodeMessage(TdsMessage message, TdsMessageEncoder encoder) {
    ByteBuf messageBody = message.content();
    while (messageBody.isReadable()) {
      int tokenType = messageBody.readUnsignedByte();
      switch (tokenType) {
        case ERROR:
          handleErrorToken(messageBody);
          break;
        case DONEPROC:
          messageBody.skipBytes(12);
          break;
        case RETURNSTATUS:
          messageBody.skipBytes(4);
          break;
        default:
          throw new UnsupportedOperationException("Unsupported token: " + tokenType);
      }
    }
    complete();
  }

  private void sendUnprepareRequest() {
    ChannelHandlerContext chctx = encoder.chctx;

    ByteBuf packet = chctx.alloc().ioBuffer();

    // packet header
    packet.writeByte(RPC);
    packet.writeByte(NORMAL | END_OF_MESSAGE);
    int packetLenIdx = packet.writerIndex();
    packet.writeShort(0); // set length later
    packet.writeShort(0x00);
    packet.writeByte(0x00); // FIXME packet ID
    packet.writeByte(0x00);

    int start = packet.writerIndex();
    packet.writeIntLE(0x00); // TotalLength for ALL_HEADERS
    encodeTransactionDescriptor(packet, 0, 1);
    // set TotalLength for ALL_HEADERS
    packet.setIntLE(start, packet.writerIndex() - start);

    /*
      RPCReqBatch
     */
    packet.writeShortLE(0xFFFF);
    packet.writeShortLE(ProcId.Sp_Unprepare);

    // Option flags
    packet.writeShortLE(0x0000);

    INTN.encodeParam(packet, null, false, ((MSSQLPreparedStatement) cmd.statement()).handle);

    int packetLen = packet.writerIndex() - packetLenIdx + 2;
    packet.setShort(packetLenIdx, packetLen);

    chctx.writeAndFlush(packet);
  }

  protected void encodeTransactionDescriptor(ByteBuf payload, long transactionDescriptor, int outstandingRequestCount) {
    payload.writeIntLE(18); // HeaderLength is always 18
    payload.writeShortLE(0x0002); // HeaderType
    payload.writeLongLE(transactionDescriptor);
    payload.writeIntLE(outstandingRequestCount);
  }
}

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
import io.vertx.sqlclient.data.NullValue;
import io.vertx.sqlclient.impl.TupleInternal;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import static io.vertx.mssqlclient.impl.codec.DataType.*;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.END_OF_MESSAGE;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.NORMAL;
import static io.vertx.mssqlclient.impl.codec.MessageType.RPC;
import static io.vertx.mssqlclient.impl.codec.TokenType.*;

abstract class ExtendedQueryCommandBaseCodec<T> extends QueryCommandBaseCodec<T, ExtendedQueryCommand<T>> {

  protected int rowCount;

  ExtendedQueryCommandBaseCodec(ExtendedQueryCommand<T> cmd) {
    super(cmd);
  }

  @Override
  void encode(TdsMessageEncoder encoder) {
    super.encode(encoder);
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.preparedStatement();
    if (ps.handle > 0) {
      sendExecRequest();
    } else {
      sendPrepexecRequest();
    }
  }

  @Override
  void decodeMessage(TdsMessage message, TdsMessageEncoder encoder) {
    ByteBuf messageBody = message.content();
    while (messageBody.isReadable()) {
      int tokenType = messageBody.readUnsignedByte();
      MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.preparedStatement();
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
          messageBody.skipBytes(12); // this should only be after ERROR?
          break;
        case INFO:
        case ORDER:
          int tokenLength = messageBody.readUnsignedShortLE();
          messageBody.skipBytes(tokenLength);
          break;
        case ERROR:
          handleErrorToken(messageBody);
          break;
        case DONEPROC:
          messageBody.skipBytes(12);
          handleResultSetDone(rowCount);
          break;
        case DONEINPROC:
          short status = messageBody.readShortLE();
          short curCmd = messageBody.readShortLE();
          long doneRowCount = messageBody.readLongLE();
          if ((status | DoneToken.STATUS_DONE_FINAL) != 0) {
            rowCount += doneRowCount;
          } else {
            handleResultSetDone((int) doneRowCount);
          }
          break;
        case RETURNSTATUS:
          messageBody.skipBytes(4);
          break;
        case RETURNVALUE:
          if (ps.handle == 0) {
            messageBody.skipBytes(2); // skip ordinal position
            messageBody.skipBytes(2 * messageBody.readUnsignedByte()); // skip param name
            messageBody.skipBytes(1); // skip status
            messageBody.skipBytes(4); // skip user type
            messageBody.skipBytes(2); // skip flags
            messageBody.skipBytes(1); // skip type id
            messageBody.skipBytes(2); // max length and length
            ps.handle = messageBody.readIntLE();
          }
          messageBody.skipBytes(messageBody.readableBytes()); // FIXME
          handleResultSetDone(rowCount);
          break;
        default:
          throw new UnsupportedOperationException("Unsupported token: " + tokenType);
      }
    }
    handleMessageDecoded();
  }

  @Override
  protected void handleResultSetDone(int affectedRows) {
    super.handleResultSetDone(rowCount);
    rowCount = 0;
  }

  protected abstract void handleMessageDecoded();

  private void sendPrepexecRequest() {
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
    encodeTransactionDescriptor(packet);
    // set TotalLength for ALL_HEADERS
    packet.setIntLE(start, packet.writerIndex() - start);

    // RPCReqBatch
    packet.writeShortLE(0xFFFF);
    packet.writeShortLE(ProcId.Sp_PrepExec);

    // Option flags
    packet.writeShortLE(0x0000);

    // Parameter

    // OUT Parameter
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.ps;
    INTN.encodeParam(packet, null, true, ps.handle);

    TupleInternal params = prepexecRequestParams();

    // Param definitions
    String paramDefinitions = parseParamDefinitions(params);
    NVARCHAR.encodeParam(packet, null, false, paramDefinitions);

    // SQL text
    NVARCHAR.encodeParam(packet, null, false, cmd.sql());

    // Param values
    encodeParams(packet, params);

    int packetLen = packet.writerIndex() - packetLenIdx + 2;
    packet.setShort(packetLenIdx, packetLen);

    chctx.writeAndFlush(packet);
  }

  protected abstract TupleInternal prepexecRequestParams();

  void sendExecRequest() {
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
    encodeTransactionDescriptor(packet);
    // set TotalLength for ALL_HEADERS
    packet.setIntLE(start, packet.writerIndex() - start);

    writeRpcRequestBatch(packet);

    int packetLen = packet.writerIndex() - packetLenIdx + 2;
    packet.setShort(packetLenIdx, packetLen);

    chctx.writeAndFlush(packet, encoder.chctx.voidPromise());
  }

  protected void writeRpcRequestBatch(ByteBuf packet) {
    // RPCReqBatch
    packet.writeShortLE(0xFFFF);
    packet.writeShortLE(ProcId.Sp_Execute);

    // Option flags
    packet.writeShortLE(0x0000);

    // Parameter

    // OUT Parameter
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.ps;
    INTN.encodeParam(packet, null, true, ps.handle);

    // Param values
    encodeParams(packet, execRequestParams());
  }

  protected abstract TupleInternal execRequestParams();

  private String parseParamDefinitions(TupleInternal params) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        stringBuilder.append(",");
      }
      stringBuilder.append("@P").append(i + 1).append(" ");
      Object param = params.getValueInternal(i);
      if (param == null) {
        stringBuilder.append(NULL.paramDefinition(null));
      } else if (param instanceof NullValue) {
        Class<?> valueClass = ((NullValue) param).type();
        DataType dataType = forValueClass(valueClass);
        stringBuilder.append(dataType.paramDefinition(null));
      } else {
        Class<?> valueClass = param.getClass();
        DataType dataType = forValueClass(valueClass);
        stringBuilder.append(dataType.paramDefinition(param));
      }
    }
    return stringBuilder.toString();
  }

  private void encodeParams(ByteBuf buffer, TupleInternal params) {
    for (int i = 0; i < params.size(); i++) {
      String name = "@P" + (i + 1);
      Object value = params.getValue(i);
      if (value == null) {
        NULL.encodeParam(buffer, name, false, null);
      } else {
        DataType dataType = DataType.forValueClass(value.getClass());
        dataType.encodeParam(buffer, name, false, value);
      }
    }
  }
}

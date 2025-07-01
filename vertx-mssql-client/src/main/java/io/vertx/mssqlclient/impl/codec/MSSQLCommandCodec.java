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
import io.netty.channel.ChannelHandler;
import io.netty.handler.ssl.SslHandler;
import io.vertx.mssqlclient.MSSQLException;
import io.vertx.mssqlclient.MSSQLInfo;
import io.vertx.mssqlclient.impl.command.PreLoginCommand;
import io.vertx.sqlclient.codec.CommandMessage;
import io.vertx.sqlclient.spi.protocol.CloseConnectionCommand;
import io.vertx.sqlclient.spi.protocol.CloseCursorCommand;
import io.vertx.sqlclient.spi.protocol.CloseStatementCommand;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.codec.CommandResponse;
import io.vertx.sqlclient.spi.protocol.InitCommand;
import io.vertx.sqlclient.spi.protocol.PrepareStatementCommand;
import io.vertx.sqlclient.spi.protocol.SimpleQueryCommand;

import static io.vertx.mssqlclient.impl.codec.EnvChange.*;
import static io.vertx.mssqlclient.impl.codec.TokenType.*;
import static io.vertx.mssqlclient.impl.utils.ByteBufUtils.readUnsignedByteLengthString;
import static io.vertx.mssqlclient.impl.utils.ByteBufUtils.readUnsignedShortLengthString;

public abstract class MSSQLCommandCodec<R, C extends CommandBase<R>> extends CommandMessage<R, C> {

  public TdsMessageCodec tdsMessageCodec;

  public MSSQLException failure;
  public R result;

  MSSQLCommandCodec(C cmd) {
    super(cmd);
  }

  public static MSSQLCommandCodec<?, ?> wrap(CommandBase<?> cmd) {
    if (cmd instanceof PreLoginCommand) {
      return new PreLoginCommandCodec((PreLoginCommand) cmd);
    } else if (cmd instanceof InitCommand) {
      return new InitCommandCodec((InitCommand) cmd);
    } else if (cmd instanceof SimpleQueryCommand) {
      return new SQLBatchCommandCodec<>((SimpleQueryCommand<?>) cmd);
    } else if (cmd instanceof PrepareStatementCommand) {
      return new PrepareStatementCodec((PrepareStatementCommand) cmd);
    } else if (cmd instanceof CloseStatementCommand) {
      return new CloseStatementCommandCodec((CloseStatementCommand) cmd);
    } else if (cmd == CloseConnectionCommand.INSTANCE) {
      return new CloseConnectionCommandCodec((CloseConnectionCommand) cmd);
    } else if (cmd instanceof CloseCursorCommand) {
      return new CloseCursorCommandCodec((CloseCursorCommand) cmd);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  abstract void encode();

  void decode(ByteBuf payload) {
    while (payload.isReadable()) {
      short tokenType = payload.readUnsignedByte();
      switch (tokenType) {
        case LOGINACK:
          payload.skipBytes(payload.readUnsignedShortLE());
          handleLoginAck();
          break;
        case COLMETADATA:
          handleColumnMetadata(payload);
          break;
        case ROW:
          handleRow(payload);
          break;
        case NBCROW:
          handleNbcRow(payload);
          break;
        case DONEINPROC:
        case DONEPROC:
        case DONE:
          handleDone(tokenType, payload);
          break;
        case INFO:
          handleInfo(payload);
          break;
        case ORDER:
        case TABNAME:
        case COLINFO:
          payload.skipBytes(payload.readUnsignedShortLE());
          break;
        case RETURNSTATUS:
          payload.skipBytes(4);
          break;
        case RETURNVALUE:
          handleReturnValue(payload);
          break;
        case ERROR:
          handleError(payload);
          break;
        case ENVCHANGE:
          handleEnvChange(payload);
          break;
        default:
          throw new UnsupportedOperationException("Unsupported token: 0x" + Integer.toHexString(tokenType));
      }
    }
    handleDecodingComplete();
  }

  protected void handleInfo(ByteBuf payload) {
    payload.skipBytes(2); // length

    MSSQLInfo info = new MSSQLInfo()
      .setNumber(payload.readIntLE())
      .setState(payload.readByte())
      .setSeverity(payload.readByte())
      .setMessage(readUnsignedShortLengthString(payload))
      .setServerName(readUnsignedByteLengthString(payload))
      .setProcedureName(readUnsignedByteLengthString(payload))
      .setLineNumber(payload.readIntLE());

    tdsMessageCodec.chctx().fireChannelRead(info);
  }

  protected void handleLoginAck() {
  }

  private void handleColumnMetadata(ByteBuf payload) {
    int columnCount = payload.readUnsignedShortLE();
    if (columnCount == 0xFFFF) { // no metadata
      return;
    }

    ColumnData[] columnDatas = new ColumnData[columnCount];

    for (int i = 0; i < columnCount; i++) {
      payload.skipBytes(6);
      DataType dataType = DataType.forId(payload.readUnsignedByte());
      TypeInfo metadata = dataType.decodeTypeInfo(payload);
      String columnName = readUnsignedByteLengthString(payload);
      columnDatas[i] = new ColumnData(columnName, dataType, metadata);
    }

    handleRowDesc(createRowDesc(columnDatas));
  }

  protected MSSQLRowDesc createRowDesc(ColumnData[] columnData) {
    return MSSQLRowDesc.create(columnData, false);
  }

  protected void handleRowDesc(MSSQLRowDesc mssqlRowDesc) {
  }

  protected void handleRow(ByteBuf payload) {
  }

  protected void handleNbcRow(ByteBuf payload) {
  }

  private void handleDone(short tokenType, ByteBuf content) {
    short status = content.readShortLE();
    if ((status & Done.STATUS_DONE_COUNT) != 0) {
      content.skipBytes(2);
      handleAffectedRows(content.readLongLE());
    } else {
      content.skipBytes(10);
    }
    handleDone(tokenType);
  }

  protected void handleAffectedRows(long count) {
  }

  protected void handleDone(short tokenType) {
  }

  protected void handleReturnValue(ByteBuf payload) {
  }

  private void handleError(ByteBuf buffer) {
    buffer.skipBytes(2); // length

    int number = buffer.readIntLE();
    byte state = buffer.readByte();
    byte severity = buffer.readByte();
    String message = readUnsignedShortLengthString(buffer);
    String serverName = readUnsignedByteLengthString(buffer);
    String procedureName = readUnsignedByteLengthString(buffer);
    int lineNumber = buffer.readIntLE();

    MSSQLException failure = new MSSQLException(number, state, severity, message, serverName, procedureName, lineNumber);

    if (this.failure == null) {
      this.failure = failure;
    } else {
      this.failure.add(failure);
    }
  }

  private void handleEnvChange(ByteBuf payload) {
    int totalLength = payload.readUnsignedShortLE();
    int startPos = payload.readerIndex();
    short type = payload.readUnsignedByte();
    switch (type) {
      case PACKETSIZE:
        handlePacketSizeChange(payload);
        break;
      case XACT_BEGIN:
      case DTC_ENLIST:
        if (payload.readUnsignedByte() != 8) {
          throw new IllegalStateException();
        }
        tdsMessageCodec.setTransactionDescriptor(payload.readLongLE());
        break;
      case XACT_COMMIT:
      case XACT_ROLLBACK:
      case DTC_DEFECT:
        tdsMessageCodec.setTransactionDescriptor(0);
        break;
      case ROUTING:
        handleRouting(payload);
        break;
      default:
        break;
    }
    payload.readerIndex(startPos + totalLength);
  }

  private void handlePacketSizeChange(ByteBuf payload) {
    int packetSize = Integer.parseInt(readUnsignedByteLengthString(payload));
    ChannelHandler first = tdsMessageCodec.chctx().pipeline().first();
    if (first instanceof SslHandler) {
      SslHandler sslHandler = (SslHandler) first;
      sslHandler.setWrapDataSize(packetSize);
    }
    tdsMessageCodec.encoder().setPacketSize(packetSize);
  }

  protected void handleRouting(ByteBuf payload) {
  }

  protected void handleDecodingComplete() {
    complete();
  }

  void complete() {
    CommandResponse<R> resp;
    if (failure != null) {
      resp = CommandResponse.failure(failure);
    } else {
      resp = CommandResponse.success(result);
    }
    tdsMessageCodec.decoder().fireCommandResponse(resp);
  }
}

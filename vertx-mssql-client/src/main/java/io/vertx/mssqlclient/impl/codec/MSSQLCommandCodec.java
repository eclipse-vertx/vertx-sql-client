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
import io.vertx.core.Handler;
import io.vertx.mssqlclient.MSSQLException;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;

import static io.vertx.mssqlclient.impl.codec.EnvChange.*;
import static io.vertx.mssqlclient.impl.codec.TokenType.*;
import static io.vertx.mssqlclient.impl.utils.ByteBufUtils.readUnsignedByteLengthString;
import static io.vertx.mssqlclient.impl.utils.ByteBufUtils.readUnsignedShortLengthString;

abstract class MSSQLCommandCodec<R, C extends CommandBase<R>> {

  protected final TdsMessageCodec tdsMessageCodec;

  final C cmd;
  public MSSQLException failure;
  public R result;
  Handler<? super CommandResponse<R>> completionHandler;

  MSSQLCommandCodec(TdsMessageCodec tdsMessageCodec, C cmd) {
    this.tdsMessageCodec = tdsMessageCodec;
    this.cmd = cmd;
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
        case ORDER:
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
          throw new UnsupportedOperationException("Unsupported token: " + tokenType);
      }
    }
    handleDecodingComplete();
  }

  protected void handleLoginAck() {
  }

  private void handleColumnMetadata(ByteBuf payload) {
    int columnCount = payload.readUnsignedShortLE();

    ColumnData[] columnDatas = new ColumnData[columnCount];

    for (int i = 0; i < columnCount; i++) {
      long userType = payload.readUnsignedIntLE();
      int flags = payload.readUnsignedShortLE();
      DataType dataType = DataType.forId(payload.readUnsignedByte());
      DataType.Metadata metadata = dataType.decodeMetadata(payload);
      String columnName = readUnsignedByteLengthString(payload);
      columnDatas[i] = new ColumnData(columnName, dataType, metadata);
    }

    handleRowDesc(new MSSQLRowDesc(columnDatas));
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
    // token value has been processed
    int length = buffer.readUnsignedShortLE();

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
    int type = payload.readUnsignedByte();
    switch (type) {
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
      default:
        break;
    }
    payload.readerIndex(startPos + totalLength);
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
    completionHandler.handle(resp);
  }

}

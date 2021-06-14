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
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.command.QueryCommandBase;

import java.util.stream.Collector;

import static io.vertx.mssqlclient.impl.protocol.EnvChange.*;
import static io.vertx.mssqlclient.impl.utils.ByteBufUtils.readUnsignedByteLengthString;

abstract class QueryCommandBaseCodec<T, C extends QueryCommandBase<T>> extends MSSQLCommandCodec<Boolean, C> {
  protected RowResultDecoder<?, T> rowResultDecoder;

  QueryCommandBaseCodec(C cmd) {
    super(cmd);
  }

  private static <A, T> T emptyResult(Collector<Row, A, T> collector) {
    return collector.finisher().apply(collector.supplier().get());
  }

  protected void encodeTransactionDescriptor(ByteBuf payload) {
    payload.writeIntLE(18); // HeaderLength is always 18
    payload.writeShortLE(0x0002); // HeaderType
    payload.writeLongLE(encoder.transactionDescriptor);
    payload.writeIntLE(1);
  }

  protected MSSQLRowDesc decodeColmetadataToken(ByteBuf payload) {
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

    return new MSSQLRowDesc(columnDatas);
  }

  protected void handleRow(ByteBuf payload) {
    rowResultDecoder.handleRow(rowResultDecoder.desc.columnDatas.length, payload);
  }

  protected void handleNbcRow(ByteBuf payload) {
    rowResultDecoder.handleNbcRow(rowResultDecoder.desc.columnDatas.length, payload);
  }

  protected void handleResultSetDone(int affectedRows) {
    this.result = false;
    T result;
    Throwable failure;
    int size;
    RowDesc rowDesc;
    if (rowResultDecoder != null) {
      failure = rowResultDecoder.complete();
      result = rowResultDecoder.result();
      rowDesc = rowResultDecoder.desc;
      size = rowResultDecoder.size();
      rowResultDecoder.reset();
    } else {
      result = emptyResult(cmd.collector());
      failure = null;
      size = 0;
      rowDesc = null;
    }
    cmd.resultHandler().handleResult(affectedRows, size, rowDesc, result, failure);
  }

  void handleEnvChangeToken(ByteBuf messageBody) {
    int totalLength = messageBody.readUnsignedShortLE();
    int startPos = messageBody.readerIndex();
    int type = messageBody.readUnsignedByte();
    switch (type) {
      case XACT_BEGIN:
      case DTC_ENLIST:
        if (messageBody.readUnsignedByte() != 8) {
          throw new IllegalStateException();
        }
        encoder.transactionDescriptor = messageBody.readLongLE();
        break;
      case XACT_COMMIT:
      case XACT_ROLLBACK:
      case DTC_DEFECT:
        encoder.transactionDescriptor = 0;
        break;
      default:
        break;
    }
    messageBody.readerIndex(startPos + totalLength);
  }
}


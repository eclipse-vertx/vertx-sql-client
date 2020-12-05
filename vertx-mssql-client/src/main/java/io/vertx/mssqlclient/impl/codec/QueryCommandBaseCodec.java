/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
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
import io.vertx.mssqlclient.impl.protocol.datatype.*;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.command.QueryCommandBase;

import java.util.stream.Collector;

import static io.vertx.mssqlclient.impl.protocol.EnvChange.*;
import static io.vertx.mssqlclient.impl.protocol.datatype.MSSQLDataTypeId.*;

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
      MSSQLDataType dataType = decodeDataTypeMetadata(payload);
      String columnName = readByteLenVarchar(payload);
      columnDatas[i] = new ColumnData(userType, flags, dataType, columnName);
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

  private MSSQLDataType decodeDataTypeMetadata(ByteBuf payload) {
    int typeInfo = payload.readUnsignedByte();
    switch (typeInfo) {
      /*
       * FixedLen DataType
       */
      case INT1TYPE_ID:
        return FixedLenDataType.INT1TYPE;
      case INT2TYPE_ID:
        return FixedLenDataType.INT2TYPE;
      case INT4TYPE_ID:
        return FixedLenDataType.INT4TYPE;
      case INT8TYPE_ID:
        return FixedLenDataType.INT8TYPE;
      case FLT4TYPE_ID:
        return FixedLenDataType.FLT4TYPE;
      case FLT8TYPE_ID:
        return FixedLenDataType.FLT8TYPE;
      case BITTYPE_ID:
        return FixedLenDataType.BITTYPE;
      /*
       * Variable Length Data Type
       */
      case NUMERICNTYPE_ID:
      case DECIMALNTYPE_ID:
        short numericTypeSize = payload.readUnsignedByte();
        byte numericPrecision = payload.readByte();
        byte numericScale = payload.readByte();
        return new NumericDataType(NUMERICNTYPE_ID, Numeric.class, numericPrecision, numericScale);
      case INTNTYPE_ID:
        byte intNTypeLength = payload.readByte();
        return IntNDataType.valueOf(intNTypeLength);
      case FLTNTYPE_ID:
        byte fltNTypeLength = payload.readByte();
        return FloatNDataType.valueOf(fltNTypeLength);
      case BITNTYPE_ID:
        payload.skipBytes(1); // should only be 1
        return BitNDataType.BIT_1_DATA_TYPE;
      case DATENTYPE_ID:
        return FixedLenDataType.DATENTYPE;
      case TIMENTYPE_ID:
        byte scale = payload.readByte();
        return new TimeNDataType(scale);
      case BIGCHARTYPE_ID:
      case BIGVARCHRTYPE_ID:
      case NCHARTYPE_ID:
      case NVARCHARTYPE_ID:
        int size = payload.readUnsignedShortLE();
        short collateCodepage = payload.readShortLE();
        short collateFlags = payload.readShortLE();
        byte collateCharsetId = payload.readByte();
        return new TextWithCollationDataType(typeInfo, String.class, null);
      default:
        throw new UnsupportedOperationException("Unsupported type with typeinfo: " + typeInfo);
    }
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


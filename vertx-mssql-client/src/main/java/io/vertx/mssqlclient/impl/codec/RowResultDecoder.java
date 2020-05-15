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

import io.vertx.mssqlclient.impl.MSSQLRowImpl;
import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDecoder;

import java.util.stream.Collector;

class RowResultDecoder<C, R> extends RowDecoder<C, R> {

  final MSSQLRowDesc desc;

  private RowStreamTokenType handleType = RowStreamTokenType.ROW;

  RowResultDecoder(Collector<Row, C, R> collector, MSSQLRowDesc desc) {
    super(collector);
    this.desc = desc;
  }

  @Override
  public Row decodeRow(int len, ByteBuf in) {
    switch (handleType) {
      case ROW:
        return decodeMssqlRow(len, in);
      case NBCROW:
        return decodeMssqlNbcRow(len, in);
      default:
        throw new UnsupportedOperationException("Unknown row stream token type");
    }
  }

  @Override
  public void handleRow(int len, ByteBuf in) {
    this.handleType = RowStreamTokenType.ROW;
    super.handleRow(len, in);
  }

  public void handleNbcRow(int len, ByteBuf in) {
    this.handleType = RowStreamTokenType.NBCROW;
    super.handleRow(len, in);
  }

  private Row decodeMssqlRow(int len, ByteBuf in) {
    Row row = new MSSQLRowImpl(desc);
    for (int c = 0; c < len; c++) {
      Object decoded = null;
      ColumnData columnData = desc.columnDatas[c];
      decoded = MSSQLDataTypeCodec.decode(columnData.dataType(), in);
      row.addValue(decoded);
    }
    return row;
  }

  private Row decodeMssqlNbcRow(int len, ByteBuf in) {
    Row row = new MSSQLRowImpl(desc);
    int nullBitmapByteCount = (len >> 3) + 1;
    int nullBitMapStartIdx = in.readerIndex();
    in.skipBytes(nullBitmapByteCount);

    for (int c = 0; c < len; c++) {
      int bytePos = c >> 3;
      int bitPos = c & 7;
      byte mask = (byte) (1 << bitPos);
      byte nullByte = in.getByte(nullBitMapStartIdx + bytePos);
      Object decoded = null;
      if ((nullByte & mask) == 0) {
        // not null
        ColumnData columnData = desc.columnDatas[c];
        decoded = MSSQLDataTypeCodec.decode(columnData.dataType(), in);
      }
      row.addValue(decoded);
    }
    return row;
  }

  enum RowStreamTokenType {
    ROW, NBCROW, ALTROW
  }
}

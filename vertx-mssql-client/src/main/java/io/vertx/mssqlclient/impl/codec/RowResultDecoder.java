/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
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
import io.vertx.mssqlclient.impl.MSSQLRowImpl;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDecoder;

import java.util.Objects;
import java.util.stream.Collector;

public class RowResultDecoder<C, R> extends RowDecoder<C, R> {

  private static final int FETCH_MISSING = 0x0002;

  private final MSSQLRowDesc desc;

  private Row decoded;

  public RowResultDecoder(Collector<Row, C, R> collector, MSSQLRowDesc desc) {
    super(collector);
    this.desc = desc;
  }

  public MSSQLRowDesc desc() {
    return desc;
  }

  @Override
  public Row decodeRow(int len, ByteBuf in) {
    Row row = Objects.requireNonNull(decoded);
    decoded = null;
    return row;
  }

  public void handleRow(ByteBuf in) {
    decoded = decodeMssqlRow(in);
    if (decoded != null) {
      super.handleRow(-1, in);
    }
  }

  public void handleNbcRow(ByteBuf in) {
    decoded = decodeMssqlNbcRow(in);
    if (decoded != null) {
      super.handleRow(-1, in);
    }
  }

  private Row decodeMssqlRow(ByteBuf in) {
    Row row = new MSSQLRowImpl(desc);
    int len = desc.size();
    for (int c = 0; c < len; c++) {
      ColumnData columnData = desc.get(c);
      row.addValue(columnData.dataType().decodeValue(in, columnData.metadata()));
    }
    return ifNotMissing(in, row);
  }

  private Row ifNotMissing(ByteBuf in, Row row) {
    Row result;
    if (desc.hasRowStat() && in.readIntLE() == FETCH_MISSING) {
      result = null;
    } else {
      result = row;
    }
    return result;
  }

  private Row decodeMssqlNbcRow(ByteBuf in) {
    Row row = new MSSQLRowImpl(desc);
    int len = desc.size();
    int nullBitmapByteCount = ((len - 1) >> 3) + 1;
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
        ColumnData columnData = desc.get(c);
        decoded = columnData.dataType().decodeValue(in, columnData.metadata());
      }
      row.addValue(decoded);
    }
    return ifNotMissing(in, row);
  }
}

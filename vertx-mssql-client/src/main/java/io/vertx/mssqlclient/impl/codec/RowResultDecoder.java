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
import io.vertx.sqlclient.impl.RowInternal;

import java.util.stream.Collector;

public class RowResultDecoder<C, R> extends RowDecoder<C, R> {

  private static final int FETCH_MISSING = 0x0002;

  private final MSSQLRowDesc desc;
  public boolean nbc;

  public RowResultDecoder(Collector<Row, C, R> collector, MSSQLRowDesc desc) {
    super(collector);
    this.desc = desc;
  }

  public MSSQLRowDesc desc() {
    return desc;
  }

  @Override
  protected RowInternal row() {
    return new MSSQLRowImpl(desc);
  }

  @Override
  protected boolean decodeRow(int len, ByteBuf in, Row row) {
    if (nbc) {
      return decodeMssqlNbcRow(in, row);
    } else {
      return decodeMssqlRow(in, row);
    }
  }

  private boolean decodeMssqlRow(ByteBuf in, Row row) {
    int len = desc.size();
    for (int c = 0; c < len; c++) {
      ColumnData columnData = desc.get(c);
      row.addValue(columnData.dataType().decodeValue(in, columnData.typeInfo()));
    }
    return ifNotMissing(in, row);
  }

  private boolean ifNotMissing(ByteBuf in, Row row) {
    if (desc.hasRowStat() && in.readIntLE() == FETCH_MISSING) {
      return false;
    } else {
      return true;
    }
  }

  private boolean decodeMssqlNbcRow(ByteBuf in, Row row) {
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
        decoded = columnData.dataType().decodeValue(in, columnData.typeInfo());
      }
      row.addValue(decoded);
    }
    return ifNotMissing(in, row);
  }
}

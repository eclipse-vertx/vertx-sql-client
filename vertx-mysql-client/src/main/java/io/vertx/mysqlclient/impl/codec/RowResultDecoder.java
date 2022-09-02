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

package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.MySQLRowDesc;
import io.vertx.mysqlclient.impl.MySQLRowImpl;
import io.vertx.mysqlclient.impl.datatype.DataFormat;
import io.vertx.mysqlclient.impl.datatype.DataType;
import io.vertx.mysqlclient.impl.datatype.DataTypeCodec;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDecoder;

import java.util.stream.Collector;

class RowResultDecoder<C, R> extends RowDecoder<C, R> {
  private static final int NULL = 0xFB;

  MySQLRowDesc rowDesc;

  RowResultDecoder(Collector<Row, C, R> collector, MySQLRowDesc rowDesc) {
    super(collector);
    this.rowDesc = rowDesc;
  }

  @Override
  protected Row decodeRow(int len, ByteBuf in) {
    Row row = new MySQLRowImpl(rowDesc);
    if (rowDesc.dataFormat() == DataFormat.BINARY) {
      // BINARY row decoding
      // 0x00 packet header
      // null_bitmap
      int nullBitmapLength = (len + 7 + 2) >>  3;
      int nullBitmapIdx = 1 + in.readerIndex();
      in.skipBytes(1 + nullBitmapLength);

      // values
      for (int c = 0; c < len; c++) {
        int val = c + 2;
        int bytePos = val >> 3;
        int bitPos = val & 7;
        byte mask = (byte) (1 << bitPos);
        byte nullByte = (byte) (in.getByte(nullBitmapIdx + bytePos) & mask);
        Object decoded = null;
        if (nullByte == 0) {
          // non-null
          ColumnDefinition columnDef = rowDesc.get(c);
          DataType dataType = columnDef.type();
          int collationId = columnDef.characterSet();
          int columnDefinitionFlags = columnDef.flags();
          decoded = DataTypeCodec.decodeBinary(dataType, collationId, columnDefinitionFlags, in);
        }
        row.addValue(decoded);
      }
    } else {
      // TEXT row decoding
      for (int c = 0; c < len; c++) {
        Object decoded = null;
        if (in.getUnsignedByte(in.readerIndex()) == NULL) {
          in.skipBytes(1);
        } else {
          ColumnDefinition columnDef = rowDesc.get(c);
          DataType dataType = columnDef.type();
          int columnDefinitionFlags = columnDef.flags();
          int collationId = columnDef.characterSet();
          decoded = DataTypeCodec.decodeText(dataType, collationId, columnDefinitionFlags, in);
        }
        row.addValue(decoded);
      }
    }
    return row;
  }
}


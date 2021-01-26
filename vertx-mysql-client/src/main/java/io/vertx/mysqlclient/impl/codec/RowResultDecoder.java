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
import io.vertx.mysqlclient.impl.MySQLCollation;
import io.vertx.mysqlclient.impl.MySQLRowDesc;
import io.vertx.mysqlclient.impl.MySQLRowImpl;
import io.vertx.mysqlclient.impl.datatype.DataFormat;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.mysqlclient.typecodec.MySQLDataTypeCodecRegistry;
import io.vertx.mysqlclient.typecodec.MySQLType;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.codec.DataTypeCodec;
import io.vertx.sqlclient.impl.RowDecoder;

import java.util.stream.Collector;

class RowResultDecoder<C, R> extends RowDecoder<C, R> {
  private static final int NULL = 0xFB;

  MySQLRowDesc rowDesc;
  MySQLDataTypeCodecRegistry dataTypeCodecRegistry;

  RowResultDecoder(Collector<Row, C, R> collector, MySQLRowDesc rowDesc, MySQLDataTypeCodecRegistry dataTypeCodecRegistry) {
    super(collector);
    this.rowDesc = rowDesc;
    this.dataTypeCodecRegistry = dataTypeCodecRegistry;
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
          ColumnDefinition columnDef = rowDesc.columnDefinitions()[c];
          int type = columnDef.type();
          int collationId = rowDesc.columnDefinitions()[c].characterSet();
          int columnDefinitionFlags = columnDef.flags();

          // data type codec decoding
          decoded = decodeBinaryRowValue(type, collationId, columnDefinitionFlags, in);
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
          int type = rowDesc.columnDefinitions()[c].type();
          int columnDefinitionFlags = rowDesc.columnDefinitions()[c].flags();
          int collationId = rowDesc.columnDefinitions()[c].characterSet();
          decoded = decodeTextualRowValue(type, collationId, columnDefinitionFlags, in);
        }
        row.addValue(decoded);
      }
    }
    return row;
  }


  private Object decodeBinaryRowValue(int type, int collationId, int columnDefinitionFlags, ByteBuf buffer) {
    MySQLType mySQLType;
    DataTypeCodec<?, ?> dataTypeCodec;
    long len;

    switch (type) {
      case ColumnDefinition.ColumnType.MYSQL_TYPE_TINY:
        mySQLType = isUnsignedNumeric(columnDefinitionFlags) ? MySQLType.UNSIGNED_TINYINT : MySQLType.TINYINT;
        len = 1;
        break;
      case ColumnDefinition.ColumnType.MYSQL_TYPE_STRING:
        mySQLType = MySQLType.STRING;
        len = BufferUtils.readLengthEncodedInteger(buffer);
        break;
      //TODO many other types implementation
      default:
        mySQLType = MySQLType.UNKNOWN;
        len = BufferUtils.readLengthEncodedInteger(buffer);
        break;
    }
    try {
      dataTypeCodec = dataTypeCodecRegistry.registries().get(mySQLType.identifier());
    } catch (Exception ex) {
      // log
      return null;
    }


    return dataTypeCodec.binaryDecode(buffer, buffer.readerIndex(), len, MySQLCollation.getJavaCharsetByCollationId(collationId));
  }

  private Object decodeTextualRowValue(int type, int collationId, int columnDefinitionFlags, ByteBuf buffer) {
    MySQLType mySQLType;
    DataTypeCodec<?, ?> dataTypeCodec;
    long len = BufferUtils.readLengthEncodedInteger(buffer);

    switch (type) {
      case ColumnDefinition.ColumnType.MYSQL_TYPE_TINY:
        mySQLType = isUnsignedNumeric(columnDefinitionFlags) ? MySQLType.UNSIGNED_TINYINT : MySQLType.TINYINT;
        break;
      case ColumnDefinition.ColumnType.MYSQL_TYPE_STRING:
        mySQLType = MySQLType.STRING;
        break;
      //TODO many other types implementation
      default:
        mySQLType = MySQLType.UNKNOWN;
        break;
    }
    try {
      dataTypeCodec = dataTypeCodecRegistry.registries().get(mySQLType.identifier());
    } catch (Exception ex) {
      // log
      return null;
    }

    return dataTypeCodec.textualDecode(buffer, buffer.readerIndex(), len, MySQLCollation.getJavaCharsetByCollationId(collationId));
  }

  private static boolean isUnsignedNumeric(int columnDefinitionFlags) {
    return (columnDefinitionFlags & ColumnDefinition.ColumnDefinitionFlags.UNSIGNED_FLAG) != 0;
  }

}


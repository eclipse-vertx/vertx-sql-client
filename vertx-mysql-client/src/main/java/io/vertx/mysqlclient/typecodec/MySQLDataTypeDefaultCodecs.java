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

package io.vertx.mysqlclient.typecodec;

import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.codec.DataType;
import io.vertx.sqlclient.codec.DataTypeCodec;
import io.vertx.sqlclient.impl.codec.CommonCodec;

import java.nio.charset.Charset;
import java.sql.JDBCType;

// make this feature optional via configuration to avoid megamorphic virtual calls?
public class MySQLDataTypeDefaultCodecs {
  public static class TinyIntTypeCodec implements DataTypeCodec<Byte, Byte> {

    public static final TinyIntTypeCodec INSTANCE = new TinyIntTypeCodec();

    private static final DataType<Byte, Byte> TINYINT_DATATYPE = new DataType<Byte, Byte>() {
      @Override
      public int identifier() {
        return MySQLType.TINYINT.identifier();
      }

      @Override
      public JDBCType jdbcType() {
        return JDBCType.TINYINT;
      }

      @Override
      public Class<Byte> encodingJavaClass() {
        return Byte.class;
      }

      @Override
      public Class<Byte> decodingJavaClass() {
        return Byte.class;
      }
    };

    private TinyIntTypeCodec() {
    }

    @Override
    public DataType<Byte, Byte> dataType() {
      return TINYINT_DATATYPE;
    }

    @Override
    public void encode(ByteBuf buffer, Byte value) {
      buffer.writeByte(value);
    }

    @Override
    public Byte binaryDecode(ByteBuf buffer, int readerIndex, long length, Charset charset) {
      return buffer.readByte();
    }

    @Override
    public Byte textualDecode(ByteBuf buffer, int readerIndex, long length, Charset charset) {
     return (byte) CommonCodec.decodeDecStringToLong(readerIndex, (int) length, buffer);
    }

  }

  public static class UnsignedTinyIntCodec implements DataTypeCodec<Short, Short> {

    public static final UnsignedTinyIntCodec INSTANCE = new UnsignedTinyIntCodec();

    private static final DataType<Short, Short> UNSIGNEDINT_DATATYPE = new DataType<Short, Short>() {
      @Override
      public int identifier() {
        return MySQLType.UNSIGNED_TINYINT.identifier();
      }

      @Override
      public JDBCType jdbcType() {
        return JDBCType.TINYINT;
      }

      @Override
      public Class<Short> encodingJavaClass() {
        return Short.class;
      }

      @Override
      public Class<Short> decodingJavaClass() {
        return Short.class;
      }
    };

    @Override
    public DataType<Short, Short> dataType() {
      return UNSIGNEDINT_DATATYPE;
    }

    @Override
    public void encode(ByteBuf buffer, Short value) {
      buffer.writeShortLE(value);
    }

    @Override
    public Short textualDecode(ByteBuf buffer, int readerIndex, long length, Charset charset) {
      return (short) CommonCodec.decodeDecStringToLong(readerIndex, (int) length, buffer);
    }

    @Override
    public Short binaryDecode(ByteBuf buffer, int readerIndex, long length, Charset charset) {
      return buffer.readShortLE();
    }
  }

  //TODO there are many classes to implement...
}

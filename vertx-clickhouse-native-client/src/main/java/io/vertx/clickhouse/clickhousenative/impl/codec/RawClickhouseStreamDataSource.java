/*
 *
 *  * Copyright (c) 2021 Vladimir Vishnevsky
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;

public class RawClickhouseStreamDataSource implements ClickhouseStreamDataSource {
  private ByteBuf source;

  public RawClickhouseStreamDataSource() {
  }

  @Override
  public void moreData(ByteBuf source, ByteBufAllocator alloc) {
    this.source = source;
  }

  @Override
  public int readableBytes() {
    return source.readableBytes();
  }

  @Override
  public void skipBytes(int length) {
    source.skipBytes(length);
  }

  @Override
  public String readPascalString() {
    return ByteBufUtils.readPascalString(source);
  }

  @Override
  public Integer readULeb128() {
    return ByteBufUtils.readULeb128(source);
  }

  @Override
  public boolean readBoolean() {
    return source.readBoolean();
  }

  @Override
  public int readIntLE() {
    return source.readIntLE();
  }

  @Override
  public ByteBuf readSlice(int nBytes) {
    return source.readSlice(nBytes);
  }

  @Override
  public void readBytes(byte[] dst) {
    source.readBytes(dst);
  }

  @Override
  public byte readByte() {
    return source.readByte();
  }

  @Override
  public long readLongLE() {
    return source.readLongLE();
  }

  @Override
  public short readShortLE() {
    return source.readShortLE();
  }

  @Override
  public float readFloatLE() {
    return source.readFloatLE();
  }

  @Override
  public double readDoubleLE() {
    return source.readDoubleLE();
  }

  @Override
  public void finish() {
  }

  @Override
  public String hexdump() {
    return source != null
      ? "[" + ByteBufUtil.hexDump(source, 0, source.writerIndex()) + "][" + ByteBufUtil.hexDump(source) + "]"
      : null;
  }
}

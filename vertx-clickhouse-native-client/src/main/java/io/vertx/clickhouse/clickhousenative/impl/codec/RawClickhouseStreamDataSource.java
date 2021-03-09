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
  public String readPascalString() {
    return ByteBufUtils.readPascalString(source);
  }

  @Override
  public Integer readULeb128() {
    return ByteBufUtils.readULeb128(source);
  }

  @Override
  public Boolean readBoolean() {
    return source.readBoolean();
  }

  @Override
  public Integer readIntLE() {
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
  public String hexdump() {
    return source != null
      ? "[" + ByteBufUtil.hexDump(source, 0, source.writerIndex()) + "][" + ByteBufUtil.hexDump(source) + "]"
      : null;
  }
}

package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public interface ClickhouseStreamDataSource {
  void moreData(ByteBuf buf, ByteBufAllocator ctx);
  int readableBytes();
  String readPascalString();
  Integer readULeb128();
  Boolean readBoolean();
  Integer readIntLE();
  ByteBuf readSlice(int nBytes);
  void readBytes(byte[] dst);
  byte readByte();
  long readLongLE();
  short readShortLE();
  String hexdump();
  default void finish(){
  }
}

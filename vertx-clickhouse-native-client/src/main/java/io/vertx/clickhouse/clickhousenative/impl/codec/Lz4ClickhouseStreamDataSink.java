package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import ru.yandex.clickhouse.util.ClickHouseCityHash;

import static io.vertx.clickhouse.clickhousenative.ClickhouseConstants.COMPRESSION_METHOD_LZ4;

class Lz4ClickhouseStreamDataSink implements ClickhouseStreamDataSink {
  private static final Logger LOG = LoggerFactory.getLogger(Lz4ClickhouseStreamDataSink.class);

  public static final int HEADER_SIZE = 1 + 4 + 4;

  private final ByteBuf sink;
  private final LZ4Factory lz4Factory;
  private final ByteBuf tmpStorage;
  private final ChannelHandlerContext ctx;

  Lz4ClickhouseStreamDataSink(ByteBuf sink, LZ4Factory lz4Factory, ChannelHandlerContext ctx) {
    this.sink = sink;
    this.tmpStorage = ctx.alloc().heapBuffer();
    this.lz4Factory = lz4Factory;
    this.ctx = ctx;
  }

  @Override
  public void writeULeb128(int value) {
    ByteBufUtils.writeULeb128(value, tmpStorage);
  }

  @Override
  public void writeByte(int value) {
    tmpStorage.writeByte(value);
  }

  @Override
  public void writeIntLE(int value) {
    tmpStorage.writeIntLE(value);
  }

  @Override
  public void writeBytes(byte[] value) {
    tmpStorage.writeBytes(value);
  }

  @Override
  public void writeBoolean(boolean value) {
    tmpStorage.writeBoolean(value);
  }

  @Override
  public void writePascalString(String str) {
    ByteBufUtils.writePascalString(str, tmpStorage);
  }

  @Override
  public void finish() {
    ByteBuf compressed = null;
    try {
      compressed = getCompressedBuffer(tmpStorage);
      byte[] compressedBytes = compressed.array();
      long[] cityHash = ClickHouseCityHash.cityHash128(compressedBytes, 0, compressed.readableBytes());
      sink.writeLongLE(cityHash[0]);
      sink.writeLongLE(cityHash[1]);
      sink.writeBytes(compressed);
    } finally {
      tmpStorage.release();
      if (compressed != null) {
        compressed.release();
      }
    }
  }

  private ByteBuf getCompressedBuffer(ByteBuf from) {
    LZ4Compressor compressor = lz4Factory.fastCompressor();
    int uncompressedLen = from.readableBytes();
    int maxCompressedLen = compressor.maxCompressedLength(uncompressedLen);
    ByteBuf tmp = ctx.alloc().heapBuffer(maxCompressedLen + HEADER_SIZE);
    tmp.writeByte(COMPRESSION_METHOD_LZ4);
    int compressedLen = compressor.compress(from.array(), 0, uncompressedLen, tmp.array(), HEADER_SIZE);
    int compressedBlockLen = HEADER_SIZE + compressedLen;
    tmp.writeIntLE(compressedBlockLen);
    tmp.writeIntLE(uncompressedLen);
    tmp.writerIndex(compressedBlockLen);
    return tmp;
  }
}

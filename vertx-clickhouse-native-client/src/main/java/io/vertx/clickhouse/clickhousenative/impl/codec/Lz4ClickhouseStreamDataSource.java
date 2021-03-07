package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import ru.yandex.clickhouse.util.ClickHouseCityHash;

import java.util.Arrays;

public class Lz4ClickhouseStreamDataSource implements ClickhouseStreamDataSource {
  private static final Logger LOG = LoggerFactory.getLogger(Lz4ClickhouseStreamDataSource.class);

  //cityhash size + compression method byte + sizeWithHeader + decompressed size
  public static final int MIN_BLOCK_PREFIX = 16 + 1 + 4 + 4;

  private final LZ4Factory lz4Factory;
  private final ByteBuf decompressedData;
  private long[] serverCityHash;
  private Long sizeWithHeader;
  private Long compressedSize;
  private Long uncompressedSize;
  private Integer checkSummedReaderIndex;

  public Lz4ClickhouseStreamDataSource(LZ4Factory lz4Factory, ByteBufAllocator alloc) {
    this.lz4Factory = lz4Factory;
    this.decompressedData = alloc.buffer();
  }

  @Override
  public void moreData(ByteBuf buf, ByteBufAllocator alloc) {
    if (serverCityHash == null && buf.readableBytes() >= MIN_BLOCK_PREFIX) {
      serverCityHash = new long[2];
      dumpHeader(buf);
      LOG.info("lz4 header dump: " + ByteBufUtil.hexDump(buf, buf.readerIndex(), MIN_BLOCK_PREFIX) +
               "; buf hash: " + buf.hashCode() + "; identityHash:" + System.identityHashCode(buf) +
               "; readerIndex: " + buf.readerIndex() + "; writerIndex: " + buf.writerIndex());
      LOG.info("full dump: " + ByteBufUtil.hexDump(buf));
      serverCityHash[0] = buf.readLongLE();
      serverCityHash[1] = buf.readLongLE();
      checkSummedReaderIndex = buf.readerIndex();
      int compressionMethod = buf.readUnsignedByte();
      if (compressionMethod != ClickhouseConstants.COMPRESSION_METHOD_LZ4) {
        decompressedData.release();
        String msg = String.format("unexpected compression method type 0x%X; expects 0x%X",
          compressionMethod, ClickhouseConstants.COMPRESSION_METHOD_LZ4);
        throw new IllegalStateException(msg);
      }
      sizeWithHeader = buf.readUnsignedIntLE();
      if (sizeWithHeader > Integer.MAX_VALUE) {
        throw new IllegalStateException("block is too big: " + sizeWithHeader + "; limit " + Integer.MAX_VALUE);
      }
      compressedSize = sizeWithHeader - 1 - 4;
      uncompressedSize = buf.readUnsignedIntLE();
      if (uncompressedSize > Integer.MAX_VALUE) {
        throw new IllegalStateException("uncompressedSize is too big: " + uncompressedSize + "; limit " + Integer.MAX_VALUE);
      }
      LOG.info(String.format("compressed size: %d(0x%X), sizeWithHeader: %d(0x%X), uncompressed size: %d(0x%X)",
        compressedSize, compressedSize, sizeWithHeader, sizeWithHeader, uncompressedSize, uncompressedSize));
    }
    if (compressedSize == null) {
      return;
    }
    if (buf.readableBytes() < compressedSize) {
      return;
    }
    long[] oursCityHash;
    ByteBuf arrayBb = alloc.heapBuffer(sizeWithHeader.intValue());
    buf.readerIndex(checkSummedReaderIndex);
    buf.readBytes(arrayBb);
    oursCityHash = ClickHouseCityHash.cityHash128(arrayBb.array(), arrayBb.arrayOffset(), sizeWithHeader.intValue());
    //reposition at the beginning of the compressed data, need to skip compression method byte, sizeWithHeader and uncompressed size
    arrayBb.readerIndex(1 + 4 + 4);


    LOG.info("compressed bytes: " + ByteBufUtil.hexDump(arrayBb, arrayBb.readerIndex(), compressedSize.intValue() - 4));
    LOG.info("readableBytes: " + arrayBb.readableBytes() + "; comprDataLen: " + (compressedSize.intValue() - 4));
    if (!Arrays.equals(serverCityHash, oursCityHash)) {
      throw new IllegalStateException("CityHash mismatch; server's: " +
        Arrays.toString(Utils.hex(serverCityHash)) + "; ours: " +
        Arrays.toString(Utils.hex(oursCityHash)));
    }
    byte[] uncompressedBytes = new byte[uncompressedSize.intValue()];
    LZ4FastDecompressor decompressor = lz4Factory.fastDecompressor();
    //LOG.info("compressed bytes: " + ByteBufUtil.hexDump(arrayBb));
    decompressor.decompress(arrayBb.array(), arrayBb.arrayOffset() + arrayBb.readerIndex(), uncompressedBytes, 0, uncompressedBytes.length);
    LOG.info("decompressed " + uncompressedBytes.length + " bytes of data");
    //LOG.info("decompressed data: " + ByteBufUtil.hexDump(uncompressedBytes) + "; asStr: " + new String(uncompressedBytes, StandardCharsets.UTF_8));
    decompressedData.writeBytes(uncompressedBytes);
    serverCityHash = null;
    sizeWithHeader = null;
    compressedSize = null;
    uncompressedSize = null;
    checkSummedReaderIndex = null;
  }

  private void dumpHeader(ByteBuf buf) {
    String h1 = ByteBufUtil.hexDump(buf, buf.readerIndex(), 8);
    String h2 = ByteBufUtil.hexDump(buf, buf.readerIndex() + 8, 8);
    String method = ByteBufUtil.hexDump(buf, buf.readerIndex() + 16, 1);
    String sizeWithHeader = ByteBufUtil.hexDump(buf, buf.readerIndex() + 17, 4);
    String uncompressedSize = ByteBufUtil.hexDump(buf, buf.readerIndex() + 21, 4);
    LOG.info(String.format("header: [%s:%s]:%s:%s:%s", h1, h2, method, sizeWithHeader, uncompressedSize));
  }

  @Override
  public int readableBytes() {
    return decompressedData.readableBytes();
  }

  @Override
  public String readPascalString() {
    return ByteBufUtils.readPascalString(decompressedData);
  }

  @Override
  public Integer readULeb128() {
    return ByteBufUtils.readULeb128(decompressedData);
  }

  @Override
  public Boolean readBoolean() {
    return decompressedData.readBoolean();
  }

  @Override
  public Integer readIntLE() {
    return decompressedData.readIntLE();
  }

  @Override
  public ByteBuf readSlice(int nBytes) {
    return decompressedData.readSlice(nBytes);
  }

  @Override
  public void readBytes(byte[] dst) {
    decompressedData.readBytes(dst);
  }

  @Override
  public byte readByte() {
    return decompressedData.readByte();
  }

  @Override
  public long readLongLE() {
    return decompressedData.readLongLE();
  }

  @Override
  public short readShortLE() {
    return decompressedData.readShortLE();
  }
}

package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
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
  private Long compressedAndSizeSize;
  private Long uncompressedSize;
  private Integer checkSummedReaderIndex;

  public Lz4ClickhouseStreamDataSource(LZ4Factory lz4Factory, ByteBufAllocator alloc) {
    this.lz4Factory = lz4Factory;
    this.decompressedData = alloc.heapBuffer();
  }

  @Override
  public void moreData(ByteBuf buf, ByteBufAllocator alloc) {
    if (serverCityHash == null) {
      if (buf.readableBytes() >= MIN_BLOCK_PREFIX) {
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
        compressedAndSizeSize = sizeWithHeader - 1 - 4;
        uncompressedSize = buf.readUnsignedIntLE();
        if (uncompressedSize > Integer.MAX_VALUE) {
          throw new IllegalStateException("uncompressedSize is too big: " + uncompressedSize + "; limit " + Integer.MAX_VALUE);
        }
        LOG.info(String.format("compressed size: %d(0x%X), sizeWithHeader: %d(0x%X), uncompressed size: %d(0x%X)",
          compressedAndSizeSize, compressedAndSizeSize, sizeWithHeader, sizeWithHeader, uncompressedSize, uncompressedSize));
      }
    }
    if (uncompressedSize == null) {
      return;
    }
    //TODO smagellan: eliminate this var (make compressedAndSizeSize = sizeWithHeader - 1 - 4 - 4 and rename to compressedDataSize)
    int compressedDataSize = compressedAndSizeSize.intValue() - 4;
    if (buf.readableBytes() < compressedDataSize) {
      return;
    }
    //TODO: maybe skip allocation if buf.hasArray() == true
    ByteBuf arrayBb = alloc.heapBuffer(sizeWithHeader.intValue());
    buf.readerIndex(checkSummedReaderIndex);
    buf.readBytes(arrayBb);
    long[] oursCityHash = ClickHouseCityHash.cityHash128(arrayBb.array(), arrayBb.arrayOffset(), sizeWithHeader.intValue());
    //reposition at the beginning of the compressed data, need to skip compression method byte, sizeWithHeader and uncompressed size
    arrayBb.readerIndex(1 + 4 + 4);

    if (!Arrays.equals(serverCityHash, oursCityHash)) {
      LOG.error("cityhash mismatch");
      LOG.error("all available data: " + ByteBufUtil.hexDump(buf, 0, buf.readerIndex() + buf.readableBytes()));
      LOG.error("data from reader index(" + buf.readerIndex() + "): " + ByteBufUtil.hexDump(buf));
      LOG.error("compressed block bytes w/header: " + ByteBufUtil.hexDump(arrayBb, arrayBb.readerIndex() - (1 + 4 + 4), sizeWithHeader.intValue()));
      LOG.error("readableBytes: " + arrayBb.readableBytes() + "; comprDataLen: " + compressedDataSize);
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
    arrayBb.release();
    serverCityHash = null;
    sizeWithHeader = null;
    compressedAndSizeSize = null;
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
  public void skipBytes(int length) {
    decompressedData.skipBytes(length);
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
  public boolean readBoolean() {
    return decompressedData.readBoolean();
  }

  @Override
  public int readIntLE() {
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

  @Override
  public float readFloatLE() {
    return decompressedData.readFloatLE();
  }

  @Override
  public double readDoubleLE() {
    return decompressedData.readDoubleLE();
  }

  @Override
  public String hexdump() {
    return ByteBufUtil.hexDump(decompressedData);
  }

  @Override
  public void finish() {
    decompressedData.release();
  }

  public static void main(String[] args) {
    String bytesStr = "747572653a20412062756720776974682073656e696f726974792e3143d201f20273206d616b65207665727920666173742c0b00a06163637572617465206dba00f1106b65732e4f3c7363726970743e616c6572742822546869732073686f756c643a01f002626520646973706c6179656420696e2061fa015177736572203100f00220626f782e22293b3c2f7363726970743e01008ae197e847628104accc74807378ba29825c00000051000000f027010002ffffffff00020102696405496e7433320c000000076d65737361676506537472696e672ae38395e383ace383bce383a0e383af0900f00882afe381aee38399e383b3e38381e3839ee383bce382af060c02800c000001030cb4060c00000100a783ac6cd55c7a7cb5ac46bddb86e21482140000000a000000a0010002ffffffff0000000300000000000a00010002ffffffff0008020a6576656e745f74696d65084461746554696d65cf415f60cf415f60176576656e745f74696d655f6d6963726f7365636f6e64730655496e743332035706001657060009686f73745f6e616d6506537472696e670662686f7273650662686f7273650871756572795f696406537472696e672462643762643639332d383736652d343339382d386135302d3464393763353861343135352462643762643639332d383736652d343339382d386135302d346439376335386134313535097468726561645f69640655496e7436348d040000000000008d04000000000000087072696f7269747904496e7438060706736f7572636506537472696e670c6578656375746551756572790d4d656d6f7279547261636b6572047465787406537472696e674a5265616420313220726f77732c203832302e3030204220696e20302e303031343237373720736563";
    byte[] bytes = new byte[bytesStr.length() / 2];
    for (int i = 0; i < bytesStr.length(); i += 2) {
      String s = bytesStr.substring(i, i + 2);
      byte b = (byte)Integer.parseInt(s, 16);
      bytes[i / 2] = b;
    }
    ByteBuf arrayBb = Unpooled.wrappedBuffer(bytes);
    Long sizeWithHeader = 660L;
    long[] oursCityHash = ClickHouseCityHash.cityHash128(arrayBb.array(), arrayBb.arrayOffset(), sizeWithHeader.intValue());
    System.err.println(Arrays.toString(Utils.hex(oursCityHash)));
  }
}

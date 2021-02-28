package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseServerException;
import io.vertx.clickhouse.clickhousenative.impl.ColumnOrientedBlock;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.Map;

public class PacketReader {
  private static final Logger LOG = LoggerFactory.getLogger(PacketReader.class);

  private final ClickhouseNativeDatabaseMetadata md;
  private final String fullClientName;
  private final Map<String, String> properties;

  private ServerPacketType packetType;
  private DatabaseMetadataReader metadataReader;
  private ClickhouseExceptionReader exceptionReader;

  private ColumnOrientedBlockReader columnBlockReader;

  private BlockStreamProfileInfoReader blockStreamProfileReader;
  private QueryProgressInfoReader queryProgressInfoReader;

  private boolean endOfStream;

  public PacketReader(ClickhouseNativeDatabaseMetadata md, String fullClientName, Map<String, String> properties) {
    this.md = md;
    this.fullClientName = fullClientName;
    this.properties = properties;
  }

  public Object receivePacket(ChannelHandlerContext ctx, ByteBuf in) {
    if (packetType == null) {
      Integer packetTypeCode = ByteBufUtils.readULeb128(in);
      if (packetTypeCode == null) {
        return null;
      }
      try {
        packetType = ServerPacketType.fromCode(packetTypeCode);
        LOG.info("packet type: " + packetType);
      } catch (IllegalArgumentException ex) {
        LOG.error("unknown packet type, dump: " + ByteBufUtil.hexDump(in), ex);
      }
    }

    if (packetType == ServerPacketType.HELLO) {
      if (metadataReader == null) {
        metadataReader = new DatabaseMetadataReader(fullClientName, properties);
      }
      ClickhouseNativeDatabaseMetadata md = metadataReader.readFrom(in);
      if (md != null) {
        LOG.info("decoded: HELLO/ClickhouseNativeDatabaseMetadata");
        metadataReader = null;
        packetType = null;
        return md;
      }
    } else if (packetType == ServerPacketType.DATA) {
      if (columnBlockReader == null) {
        columnBlockReader = new ColumnOrientedBlockReader(md);
      }
      ColumnOrientedBlock block = columnBlockReader.readFrom(in);
      if (block != null) {
        LOG.info("decoded: DATA/ColumnOrientedBlock [" + block.numColumns() + "; " + block.numRows() + "]");
        columnBlockReader = null;
        packetType = null;
      }
      return block;
    } else if (packetType == ServerPacketType.EXCEPTION) {
      if (exceptionReader == null) {
        exceptionReader = new ClickhouseExceptionReader();
      }
      ClickhouseServerException exc = exceptionReader.readFrom(in);
      if (exc != null) {
        LOG.info("decoded: EXCEPTION/ClickhouseServerException");
        exceptionReader = null;
        packetType = null;
      }
      return exc;
    } else if (packetType == ServerPacketType.PROGRESS) {
      if (queryProgressInfoReader == null) {
        queryProgressInfoReader = new QueryProgressInfoReader(md);
      }
      QueryProgressInfo queryProgressInfo = queryProgressInfoReader.readFrom(in);
      if (queryProgressInfo != null) {
        LOG.info("decoded: PROGRESS/QueryProgressInfo: " + queryProgressInfo);
        queryProgressInfoReader = null;
        packetType = null;
      }
      return queryProgressInfo;
    } else if (packetType == ServerPacketType.END_OF_STREAM) {
      LOG.info("decoded: END_OF_STREAM");
      packetType = null;
      endOfStream = true;
    } else if (packetType == ServerPacketType.PROFILE_INFO) {
      if (blockStreamProfileReader == null) {
        blockStreamProfileReader = new BlockStreamProfileInfoReader();
      }
      BlockStreamProfileInfo profileInfo = blockStreamProfileReader.readFrom(in);
      if (profileInfo != null) {
        LOG.info("decoded: PROFILE_INFO/BlockStreamProfileInfo " + profileInfo);
        blockStreamProfileReader = null;
        packetType = null;
      }
      return profileInfo;
    } else {
      throw new IllegalStateException("unknown packet type: " + packetType);
    }
    return null;
  }

  public boolean isEndOfStream() {
    return endOfStream;
  }
}

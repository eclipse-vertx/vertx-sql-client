package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.ColumnOrientedBlock;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class PacketReader {
  private static final Logger LOG = LoggerFactory.getLogger(PacketReader.class);

  private final ClickhouseNativeDatabaseMetadata md;

  private Integer packetType;

  private ColumnOrientedBlockReader columnBlockReader;

  private BlockStreamProfileInfo blockStreamProfileInfo;
  private QueryProgressInfo queryProgress;
  private boolean endOfStream;

  public PacketReader(ClickhouseNativeDatabaseMetadata md) {
    this.md = md;
  }

  public ColumnOrientedBlock receivePacket(ChannelHandlerContext ctx, ByteBuf in) {
    if (packetType == null) {
      packetType = ByteBufUtils.readULeb128(in);
      if (packetType == null) {
        return ColumnOrientedBlock.PARTIAL;
      }
      LOG.info("packet type: " + ServerPacketType.fromCode(packetType));
    }
    if (packetType == ServerPacketType.DATA.code()) {
      if (columnBlockReader == null) {
        columnBlockReader = new ColumnOrientedBlockReader(md);
      }
      ColumnOrientedBlock block = columnBlockReader.readFrom(in);
      if (block != ColumnOrientedBlock.PARTIAL) {
        columnBlockReader = null;
        packetType = null;
      }
      return block;
    } else if (packetType == ServerPacketType.PROFILE_INFO.code()) {
      if (blockStreamProfileInfo == null) {
        blockStreamProfileInfo = new BlockStreamProfileInfo();
      }
      blockStreamProfileInfo.readFrom(in);
      if (blockStreamProfileInfo.isComplete()) {
        LOG.info("decoded: BlockStreamProfileInfo: " + blockStreamProfileInfo);
        blockStreamProfileInfo = null;
        packetType = null;
      }
    } else if (packetType == ServerPacketType.PROGRESS.code()) {
      if (queryProgress == null) {
        queryProgress = new QueryProgressInfo(md);
      }
      queryProgress.readFrom(in);
      if (queryProgress.isComplete()) {
        LOG.info("decoded: QueryProgressInfo: " + queryProgress);
        queryProgress = null;
        packetType = null;
      }
    } else if (packetType == ServerPacketType.END_OF_STREAM.code()) {
      LOG.info("reached end of stream");
      packetType = null;
      endOfStream = true;
    }
    return null;
  }

  public boolean isEndOfStream() {
    return endOfStream;
  }
}

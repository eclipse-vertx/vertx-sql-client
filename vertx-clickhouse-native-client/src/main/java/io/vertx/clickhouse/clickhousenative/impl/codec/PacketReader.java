package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseServerException;
import io.vertx.clickhouse.clickhousenative.impl.ColumnOrientedBlock;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import net.jpountz.lz4.LZ4Factory;

import java.util.Map;

public class PacketReader {
  private static final Logger LOG = LoggerFactory.getLogger(PacketReader.class);

  private final ClickhouseNativeDatabaseMetadata md;
  private final String fullClientName;
  private final Map<String, String> properties;
  private final LZ4Factory lz4Factory;

  private ClickhouseStreamDataSource ds;
  private ServerPacketType packetType;
  private DatabaseMetadataReader metadataReader;
  private ClickhouseExceptionReader exceptionReader;

  private String tempTableInfo;
  private ColumnOrientedBlockReader columnBlockReader;

  private BlockStreamProfileInfoReader blockStreamProfileReader;
  private QueryProgressInfoReader queryProgressInfoReader;

  private boolean endOfStream;

  public PacketReader(ClickhouseNativeDatabaseMetadata md, String fullClientName, Map<String, String> properties, LZ4Factory lz4Factory) {
    this.md = md;
    this.fullClientName = fullClientName;
    this.properties = properties;
    this.lz4Factory = lz4Factory;
  }

  public Object receivePacket(ByteBufAllocator alloc, ByteBuf in) {
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
      if (md.getRevision() >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_TEMPORARY_TABLES) {
        if (tempTableInfo == null) {
          tempTableInfo = ByteBufUtils.readPascalString(in);
          LOG.info("tempTableInfo: " + tempTableInfo);
          if (tempTableInfo == null) {
            return null;
          }
        }
      }
      if (columnBlockReader == null) {
        ds = dataSource(alloc);
        columnBlockReader = new ColumnOrientedBlockReader(md);
      }
      ds.moreData(in, alloc);
      ColumnOrientedBlock block = columnBlockReader.readFrom(ds);
      if (block != null) {
        LOG.info("decoded: DATA/ColumnOrientedBlock [" + block.numColumns() + "; " + block.numRows() + "]");
        columnBlockReader = null;
        packetType = null;
        ds = null;
        tempTableInfo = null;
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

  private ClickhouseStreamDataSource dataSource(ByteBufAllocator alloc) {
    if (lz4Factory == null) {
      return new RawClickhouseStreamDataSource();
    } else {
      return new Lz4ClickhouseStreamDataSource(lz4Factory, alloc);
    }
  }

  public boolean isEndOfStream() {
    return endOfStream;
  }
}

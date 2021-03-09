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

import java.util.List;
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
  private MultistringMessageReader multistringReader;
  private List<String> multistringMessage;
  private PacketReader tableColumnsPacketReader;

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
      return readServerHelloBlock(in);
    } else if (packetType == ServerPacketType.DATA) {
      return readDataBlock(alloc, in);
    } else if (packetType == ServerPacketType.EXCEPTION) {
      return readExceptionBlock(in);
    } else if (packetType == ServerPacketType.PROGRESS) {
      return readProgressBlock(in);
    } else if (packetType == ServerPacketType.END_OF_STREAM) {
      LOG.info("decoded: END_OF_STREAM");
      packetType = null;
      endOfStream = true;
    } else if (packetType == ServerPacketType.PROFILE_INFO) {
      return readProfileInfoBlock(in);
    } else if (packetType == ServerPacketType.LOG) {
      ColumnOrientedBlock block = readDataBlock(alloc, in, false);
      if (block != null) {
        traceServerLogs(block);
      }
      return null;
    } else if (packetType == ServerPacketType.TABLE_COLUMNS) {
      return receiveMultistringMessage(alloc, in, packetType);
    } else {
      throw new IllegalStateException("unknown packet type: " + packetType);
    }
    return null;
  }

  private void traceServerLogs(ColumnOrientedBlock block) {
    LOG.info("server log: [" + block.numColumns() + "; " + block.numRows() + "]");
  }

  private List<String> receiveMultistringMessage(ByteBufAllocator alloc, ByteBuf in, ServerPacketType type) {
    if (multistringMessage == null) {
      if (multistringReader == null) {
        multistringReader = new MultistringMessageReader();
      }
      multistringMessage = multistringReader.readFrom(in, type);
    }
    if (multistringMessage == null) {
      return null;
    }
    if (tableColumnsPacketReader == null) {
      tableColumnsPacketReader = new PacketReader(md, fullClientName, properties, lz4Factory);
    }
    ColumnOrientedBlock block = tableColumnsPacketReader.readDataBlock(alloc, in, true);
    if (block != null) {
      LOG.info("decoded: MultistringMessage: " + multistringMessage + "; block: [" + block.numColumns() + "; " + block.numRows() + "]");
      multistringReader = null;
      packetType = null;
      tableColumnsPacketReader = null;
      multistringMessage = null;
    }
    return multistringMessage;
  }

  private ClickhouseNativeDatabaseMetadata readServerHelloBlock(ByteBuf in) {
    if (metadataReader == null) {
      metadataReader = new DatabaseMetadataReader(fullClientName, properties);
    }
    ClickhouseNativeDatabaseMetadata md = metadataReader.readFrom(in);
    if (md != null) {
      LOG.info("decoded: HELLO/ClickhouseNativeDatabaseMetadata");
      metadataReader = null;
      packetType = null;
    }
    return md;
  }

  private BlockStreamProfileInfo readProfileInfoBlock(ByteBuf in) {
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
  }

  private QueryProgressInfo readProgressBlock(ByteBuf in) {
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
  }

  private ClickhouseServerException readExceptionBlock(ByteBuf in) {
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
  }

  private ColumnOrientedBlock readDataBlock(ByteBufAllocator alloc, ByteBuf in) {
    return readDataBlock(alloc, in, true);
  }

  private ColumnOrientedBlock readDataBlock(ByteBufAllocator alloc, ByteBuf in, boolean preferCompressionIfEnabled) {
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
      ds = dataSource(alloc, preferCompressionIfEnabled);
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
  }

  private ClickhouseStreamDataSource dataSource(ByteBufAllocator alloc, boolean preferCompressionIfEnabled) {
    if (lz4Factory == null || !preferCompressionIfEnabled) {
      return new RawClickhouseStreamDataSource();
    } else {
      return new Lz4ClickhouseStreamDataSource(lz4Factory, alloc);
    }
  }

  public boolean isEndOfStream() {
    return endOfStream;
  }
}

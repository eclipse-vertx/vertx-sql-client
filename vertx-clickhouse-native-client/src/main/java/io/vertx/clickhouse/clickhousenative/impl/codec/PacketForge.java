package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class PacketForge {
  private static final Logger LOG = LoggerFactory.getLogger(PacketForge.class);
  private final ClickhouseNativeSocketConnection conn;
  private final ChannelHandlerContext chctx;

  public PacketForge(ClickhouseNativeSocketConnection conn, ChannelHandlerContext chctx) {
    this.conn = conn;
    this.chctx = chctx;
  }

  public void sendQuery(String query, ByteBuf buf) {
    LOG.info("running query: " + query);
    ByteBufUtils.writeULeb128(ClientPacketTypes.QUERY, buf);
    //query id
    ByteBufUtils.writePascalString("", buf);
    ClickhouseNativeDatabaseMetadata meta = conn.getDatabaseMetaData();
    int serverRevision = meta.getRevision();
    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_CLIENT_INFO) {
      ClientInfo clInfo = new ClientInfo(meta);
      clInfo.serializeTo(buf);
    }
    boolean settingsAsStrings = serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_SETTINGS_SERIALIZED_AS_STRINGS;
    writeSettings(settings(), settingsAsStrings, true, buf);
    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_INTERSERVER_SECRET) {
      ByteBufUtils.writePascalString("", buf);
    }
    ByteBufUtils.writeULeb128(QueryProcessingStage.COMPLETE, buf);
    int compressionEnabled = conn.lz4Factory() == null ? Compression.DISABLED : Compression.ENABLED;
    ByteBufUtils.writeULeb128(compressionEnabled, buf);
    ByteBufUtils.writePascalString(query, buf);
  }

  public void writeSettings(Map<String, String> settings, boolean settingsAsStrings, boolean settingsAreImportant, ByteBuf buf) {
    if (settingsAsStrings) {
      for (Map.Entry<String, String> entry : settings.entrySet()) {
        if (!ClickhouseConstants.NON_QUERY_OPTIONS.contains(entry.getKey())) {
          LOG.info("writing query setting: " + entry);
          ByteBufUtils.writePascalString(entry.getKey(), buf);
          buf.writeBoolean(settingsAreImportant);
          ByteBufUtils.writePascalString(entry.getValue(), buf);
        }
      }
    } else {
      //TODO: implement (required for old clickhouse versions)
      throw new IllegalArgumentException("not implemented for settingsAsStrings=false");
    }
    //end of settings
    ByteBufUtils.writePascalString("", buf);
  }

  public void sendExternalTables(ByteBuf buf, Collection<RowOrientedBlock> blocks) {
    ClickhouseNativeDatabaseMetadata md = conn.getDatabaseMetaData();
    for (RowOrientedBlock block : blocks) {
      //TODO implement external tables support
      sendData(buf, block, null);
    }
    sendData(buf, new RowOrientedBlock(ClickhouseNativeRowDesc.EMPTY, Collections.emptyList(), md), "");
  }

  public void sendData(ByteBuf buf, RowOrientedBlock block, String tableName) {
    sendData(buf, block, tableName, 0, block.totalRows());
  }

  public void sendData(ByteBuf buf, RowOrientedBlock block, String tableName, int fromRow, int toRow) {
    ByteBufUtils.writeULeb128(ClientPacketTypes.DATA, buf);
    if (conn.getDatabaseMetaData().getRevision() >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_TEMPORARY_TABLES) {
      ByteBufUtils.writePascalString(tableName, buf);
    }
    ClickhouseStreamDataSink sink = null;
    try {
      sink = dataSink(buf);
      block.serializeAsBlock(sink, fromRow, toRow);
    } finally {
      if (sink != null) {
        sink.finish();
      }
    }
  }

  private ClickhouseStreamDataSink dataSink(ByteBuf buf) {
    return conn.lz4Factory() == null ? new RawClickhouseStreamDataSink(buf) : new Lz4ClickhouseStreamDataSink(buf, conn.lz4Factory(), chctx);
  }

  protected Map<String, String> settings() {
    return conn.getDatabaseMetaData().getProperties();
  }

  public void sendColumns(RowOrientedBlock block, ByteBuf buf, Integer maxInsertBlockSize) {
    int nRows = block.totalRows();
    int blockSize = maxInsertBlockSize == null ? nRows : maxInsertBlockSize;
    int fromRow = 0;
    while (fromRow < nRows) {
      int toRow = Math.min(nRows, fromRow + blockSize);
      sendData(buf, block, "", fromRow, toRow);
      fromRow = toRow;
    }
  }
}

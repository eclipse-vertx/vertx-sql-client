package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.QueryCommandBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SimpleQueryCommandCodec<T> extends ClickhouseNativeQueryCommandBaseCodec<T, QueryCommandBase<T>>{
  private static final Logger LOG = LoggerFactory.getLogger(SimpleQueryCommandCodec.class);
  private final boolean requireUpdates;
  private RowResultDecoder<?, T> rowResultDecoder;
  private PacketReader packetReader;
  private int dataPacketNo;
  protected final ClickhouseNativeSocketConnection conn;

  protected SimpleQueryCommandCodec(QueryCommandBase<T> cmd, ClickhouseNativeSocketConnection conn) {
    this(cmd, conn, false);
  }
  protected SimpleQueryCommandCodec(QueryCommandBase<T> cmd, ClickhouseNativeSocketConnection conn, boolean requireUpdates) {
    super(cmd);
    this.conn = conn;
    this.requireUpdates = requireUpdates;
   }

  @Override
  void encode(ClickhouseNativeEncoder encoder) {
    checkIfBusy();
    super.encode(encoder);
    if (!isSuspended()) {
      ByteBuf buf = allocateBuffer();
      sendQuery(cmd.sql(), buf);
      sendExternalTables(buf, Collections.emptyList());
      encoder.chctx().writeAndFlush(buf, encoder.chctx().voidPromise());
    }
  }

  private void sendExternalTables(ByteBuf buf, Collection<RowOrientedBlock> blocks) {
    ClickhouseNativeDatabaseMetadata md = encoder.getConn().getDatabaseMetaData();
    for (RowOrientedBlock block : blocks) {
      //TODO smagellan
      sendData(buf, block, null);
    }
    sendData(buf, new RowOrientedBlock(null, null, new BlockInfo(), md), "");
  }

  private void sendData(ByteBuf buf, RowOrientedBlock block, String tableName) {
    ByteBufUtils.writeULeb128(ClientPacketTypes.DATA, buf);
    if (encoder.getConn().getDatabaseMetaData().getRevision() >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_TEMPORARY_TABLES) {
      ByteBufUtils.writePascalString(tableName, buf);
    }
    block.serializeTo(buf);
  }

  private void sendQuery(String query, ByteBuf buf) {
    ByteBufUtils.writeULeb128(ClientPacketTypes.QUERY, buf);
    //query id
    ByteBufUtils.writePascalString("", buf);
    ClickhouseNativeDatabaseMetadata meta = encoder.getConn().getDatabaseMetaData();
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
    ByteBufUtils.writeULeb128(Compression.DISABLED, buf);
    ByteBufUtils.writePascalString(query, buf);
  }

  protected Map<String, String> settings() {
    return conn.getDatabaseMetaData().getProperties();
  }

  protected boolean isSuspended() {
    return false;
  }

  protected void checkIfBusy() {
    conn.throwExceptionIfBusy(null);
  }

  private void writeSettings(Map<String, String> settings, boolean settingsAsStrings, boolean settingsAreImportant, ByteBuf buf) {
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
      //TODO smagellan
      throw new IllegalArgumentException("not implemented for settingsAsStrings=false");
    }
    //end of settings
    ByteBufUtils.writePascalString("", buf);
  }

  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
    LOG.info("decode, readable bytes: " + in.readableBytes());
    if (packetReader == null) {
      packetReader = new PacketReader(encoder.getConn().getDatabaseMetaData(), null, null);
    }
    Object packet = packetReader.receivePacket(ctx, in);
    if (packet != null) {
      if (packet.getClass() == ColumnOrientedBlock.class) {
        ColumnOrientedBlock block = (ColumnOrientedBlock)packet;
        LOG.info("decoded packet " + dataPacketNo + ": " + block + " row count " + block.numRows());
        if (dataPacketNo == 0) {
          ClickhouseNativeRowDesc rowDesc = buildRowDescriptor(block);
          rowResultDecoder = new RowResultDecoder<>(cmd.collector(), rowDesc);
        }
        packetReader = null;
        rowResultDecoder.generateRows(block);
        if (requireUpdates && block.numRows() > 0) {
          notifyOperationUpdate(true);
        }
        ++dataPacketNo;
      } else {
        String msg = "unknown packet type: " + packet.getClass();
        LOG.error(msg);
        //completionHandler.handle(CommandResponse.failure(new RuntimeException(msg)));
      }
    } else if (packetReader.isEndOfStream()) {
      notifyOperationUpdate(false);
      packetReader = null;
    }
  }

  private ClickhouseNativeRowDesc buildRowDescriptor(ColumnOrientedBlock block) {
    Map<String, ClickhouseNativeColumnDescriptor> data = block.getColumnsWithTypes();
    List<String> columnNames = new ArrayList<>(data.keySet());
    List<ColumnDescriptor> columnTypes = new ArrayList<>(data.values());
    return new ClickhouseNativeRowDesc(columnNames, columnTypes);
  }

  private void notifyOperationUpdate(boolean hasMoreResults) {
    Throwable failure = null;
    if (rowResultDecoder != null) {
      LOG.info("notifying operation update; has more result = " + hasMoreResults + "; query: ");
      failure = rowResultDecoder.complete();
      if (failure != null) {
        failure = new RuntimeException(failure);
      }
      T result = rowResultDecoder.result();
      int size = rowResultDecoder.size();
      rowResultDecoder.reset();

      cmd.resultHandler().handleResult(0, size, rowResultDecoder.getRowDesc(), result, failure);
    }

    CommandResponse<Boolean> response;
    if (failure == null) {
      response = CommandResponse.success(hasMoreResults);
    } else {
      response = CommandResponse.failure(failure);
    }
    completionHandler.handle(response);
  }
}

package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.impl.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.QueryCommandBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SimpleQueryCommandCodec<T> extends ClickhouseNativeQueryCommandBaseCodec<T, QueryCommandBase<T>>{
  private static final Logger LOG = LoggerFactory.getLogger(SimpleQueryCommandCodec.class);
  private final boolean commandRequiresUpdatesDelivery;
  protected final QueryParsers.QueryType queryType;
  protected final int batchSize;

  private RowResultDecoder<?, T> rowResultDecoder;
  private PacketReader packetReader;
  private int dataPacketNo;
  protected final ClickhouseNativeSocketConnection conn;

  protected SimpleQueryCommandCodec(QueryCommandBase<T> cmd, ClickhouseNativeSocketConnection conn) {
    this(null, 0, cmd, conn, false);
  }
  protected SimpleQueryCommandCodec(QueryParsers.QueryType queryType, int batchSize, QueryCommandBase<T> cmd, ClickhouseNativeSocketConnection conn, boolean requireUpdatesDelivery) {
    super(cmd);
    this.queryType = queryType;
    this.batchSize = batchSize;
    this.conn = conn;
    this.commandRequiresUpdatesDelivery = requireUpdatesDelivery;
   }

  @Override
  void encode(ClickhouseNativeEncoder encoder) {
    checkIfBusy();
    super.encode(encoder);
    if (!isSuspended()) {
      ByteBuf buf = allocateBuffer();
      try {
        PacketForge forge = new PacketForge(conn, encoder.chctx());
        forge.sendQuery(sql(), buf);
        forge.sendExternalTables(buf, Collections.emptyList());
        encoder.chctx().writeAndFlush(buf, encoder.chctx().voidPromise());
      } catch (Throwable t) {
        buf.release();
        throw t;
      }
    }
  }

  protected String sql() {
    return cmd.sql();
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

  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
    LOG.info("decode, readable bytes: " + in.readableBytes());
    if (packetReader == null) {
      packetReader = new PacketReader(encoder.getConn().getDatabaseMetaData(), null, null, encoder.getConn().lz4Factory());
    }
    //TODO smagellan: handle parse Exceptions, if any
    Object packet = packetReader.receivePacket(ctx.alloc(), in);
    if (packet != null) {
      if (packet.getClass() == ColumnOrientedBlock.class) {
        ColumnOrientedBlock block = (ColumnOrientedBlock)packet;
        LOG.info("decoded packet " + dataPacketNo + ": " + block + " row count " + block.numRows());
        if (dataPacketNo == 0) {
          ClickhouseNativeRowDesc rowDesc = buildRowDescriptor(block);
          rowResultDecoder = new RowResultDecoder<>(cmd.collector(), rowDesc, conn.getDatabaseMetaData());
        }
        packetReader = null;
        rowResultDecoder.generateRows(block);
        if (commandRequiresUpdatesDelivery && block.numRows() > 0) {
          notifyOperationUpdate(true, null);
        }
        ++dataPacketNo;
      } else {
        //TODO smagellan: handle connection errors (e.g. table does not exist, wrong password, no column with given name, etc)
        String msg = "unknown packet type: " + packet.getClass();
        LOG.error(msg);
        if (packet instanceof Throwable) {
          Throwable t = (Throwable) packet;
          LOG.error("unknown packet type", t);
          notifyOperationUpdate(false, t);
        }
        //completionHandler.handle(CommandResponse.failure(new RuntimeException(msg)));
      }
    } else if (packetReader.isEndOfStream()) {
      notifyOperationUpdate(false, null);
      packetReader = null;
    }
  }

  private ClickhouseNativeRowDesc buildRowDescriptor(ColumnOrientedBlock block) {
    Map<String, ClickhouseNativeColumnDescriptor> data = block.getColumnsWithTypes();
    List<String> columnNames = new ArrayList<>(data.keySet());
    List<ColumnDescriptor> columnTypes = new ArrayList<>(data.values());
    return new ClickhouseNativeRowDesc(columnNames, columnTypes);
  }

  private void notifyOperationUpdate(boolean hasMoreResults, Throwable t) {
    notifyOperationUpdate(0, hasMoreResults, t);
  }

  private void notifyOperationUpdate(int updateCount, boolean hasMoreResults, Throwable t) {
    Throwable failure = null;
    if (rowResultDecoder != null) {
      LOG.info("notifying operation update; has more result = " + hasMoreResults + "; query: ");
      failure = rowResultDecoder.complete();
      T result = rowResultDecoder.result();
      int size = rowResultDecoder.size();
      rowResultDecoder.reset();
      cmd.resultHandler().handleResult(updateCount, size, rowResultDecoder.getRowDesc(), result, failure);
    } else {
      if (queryType == QueryParsers.QueryType.INSERT) {
        rowResultDecoder = new RowResultDecoder<>(cmd.collector(), ClickhouseNativeRowDesc.EMPTY, conn.getDatabaseMetaData());
        failure = rowResultDecoder.complete();
        cmd.resultHandler().handleResult(batchSize, 0, ClickhouseNativeRowDesc.EMPTY, rowResultDecoder.result(), failure);
      }
    }
    if (t != null) {
      if (failure == null) {
        failure = t;
      } else {
        failure = new RuntimeException(failure);
        failure.addSuppressed(t);
      }
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

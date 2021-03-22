package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeRowDesc;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PrepareStatementCodec extends ClickhouseNativeCommandCodec<PreparedStatement, PrepareStatementCommand>{
  private static final Logger LOG = LoggerFactory.getLogger(PrepareStatementCodec.class);
  private final Map.Entry<String, Integer> queryType;

  private PacketReader packetReader;

  protected PrepareStatementCodec(PrepareStatementCommand cmd, Map.Entry<String, Integer> queryType) {
    super(cmd);
    this.queryType = queryType;
  }

  @Override
  void encode(ClickhouseNativeEncoder encoder) {
    super.encode(encoder);
    LOG.info("handle ready for query");
    String sql = cmd.sql();
    boolean isInsert = queryType != null && "insert".equalsIgnoreCase(queryType.getKey());
    int valuesIndex = 0;
    boolean realInsertBatch = isInsert && (valuesIndex = valuesPos(sql, queryType.getValue())) != -1;
    if (realInsertBatch) {
      String truncatedSql = sql.substring(0, valuesIndex + "values".length());
      ByteBuf buf = allocateBuffer();
      try {
        PacketForge forge = new PacketForge(encoder.getConn(), encoder.chctx());
        forge.sendQuery(truncatedSql, buf);
        forge.sendExternalTables(buf, Collections.emptyList());
        encoder.chctx().writeAndFlush(buf, encoder.chctx().voidPromise());
      } catch (Throwable t) {
        buf.release();
        throw t;
      }
    } else {
      completionHandler.handle(CommandResponse.success(new ClickhouseNativePreparedStatement(sql, new ClickhouseNativeParamDesc(Collections.emptyList()),
        new ClickhouseNativeRowDesc(Collections.emptyList()), queryType, false)));
    }
  }

  //TODO smagellan: move to parsers, introduce "InsertQueryInfo"
  private static int valuesPos(String sql, int fromPos) {
    String sqlLoCase = sql.toLowerCase();
    if (sqlLoCase.endsWith("values")) {
      return sql.length() - "values".length();
    }
    Map.Entry<String, Integer> pos = QueryParsers.findKeyWord(sql, fromPos, Collections.singleton("$"));
    if (pos == null) {
      return -1;
    }
    return sqlLoCase.lastIndexOf("values", pos.getValue());
  }

  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
    if (packetReader == null) {
      packetReader = new PacketReader(encoder.getConn().getDatabaseMetaData(), null, null, encoder.getConn().lz4Factory());
    }
    Object packet = packetReader.receivePacket(ctx.alloc(), in);
    if (packet != null) {
      packetReader = null;
      if (packet.getClass() == TableColumns.class) {
        TableColumns columns = (TableColumns)packet;
        Map<String, ClickhouseNativeColumnDescriptor> data = columns.columnDefinition().getColumnsWithTypes();

        List<String> columnNames = new ArrayList<>(data.keySet());
        List<ColumnDescriptor> columnTypes = new ArrayList<>(data.values());
        ClickhouseNativeRowDesc rowDesc = new ClickhouseNativeRowDesc(columnNames, columnTypes);
        completionHandler.handle(CommandResponse.success(new ClickhouseNativePreparedStatement(cmd.sql(),
          new ClickhouseNativeParamDesc(Collections.emptyList()), rowDesc, queryType, true)));
      } else if (packet instanceof Throwable) {
        cmd.fail((Throwable) packet);
      }
    }
  }
}

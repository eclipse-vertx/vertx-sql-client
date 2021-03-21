package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeRowDesc;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.clickhouse.clickhousenative.impl.RowOrientedBlock;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.vertx.clickhouse.clickhousenative.ClickhouseConstants.OPTION_MAX_BLOCK_SIZE;

public class ExtendedQueryCommandCodec<T> extends SimpleQueryCommandCodec<T> {
  private static final Logger LOG = LoggerFactory.getLogger(SimpleQueryCommandCodec.class);

  public ExtendedQueryCommandCodec(QueryParsers.QueryType queryType, ExtendedQueryCommand<T> cmd, ClickhouseNativeSocketConnection conn) {
    super(queryType, cmd.paramsList() == null ? 0 : cmd.paramsList().size(), cmd, conn, cmd.fetch() > 0);
  }

  @Override
  protected String sql() {
    ExtendedQueryCommand<T> ecmd = ecmd();
    if (queryType != QueryParsers.QueryType.INSERT || !ecmd.isBatch()) {
      return QueryParsers.insertParamValuesIntoQuery(ecmd.sql(), ecmd.params() == null ? ecmd.paramsList().get(0) : ecmd.params());
    }
    //TODO smagellan: handle insert at query prepare stage (PrepareStatementCodec)
    return ecmd.sql();
  }

  @Override
  void encode(ClickhouseNativeEncoder encoder) {
    ExtendedQueryCommand<T> ecmd = ecmd();
    String ourCursorId = ecmd.cursorId();
    //TODO smagellan: introduce lock() method
    if (conn.getPendingCursorId() == null) {
      conn.setPendingCursorId(ourCursorId);
    } else {
      conn.throwExceptionIfBusy(ourCursorId);
    }
    PreparedStatement ps = ecmd.preparedStatement();
    if (ps != null && ((ClickhouseNativePreparedStatement)ps).isSentQuery()) {
      this.encoder = encoder;
      ByteBuf buf = allocateBuffer();
      try {
        ChannelHandlerContext chctx = encoder.chctx();
        PacketForge forge = new PacketForge(encoder.getConn(), chctx);
        ClickhouseNativeDatabaseMetadata md = encoder.getConn().getDatabaseMetaData();
        List<Tuple> paramsList = ecmd.paramsList();
        if (paramsList != null && !paramsList.isEmpty()) {
          RowOrientedBlock block = new RowOrientedBlock(ps.rowDesc(), paramsList, md);
          forge.sendColumns(block, buf, null);
        }
        forge.sendData(buf, new RowOrientedBlock(ClickhouseNativeRowDesc.EMPTY, Collections.emptyList(), md),"");
        chctx.writeAndFlush(buf, chctx.voidPromise());
        LOG.info("sent columns");
      } catch (Throwable t) {
        buf.release();
        throw t;
      }
    } else {
      super.encode(encoder);
    }
  }

  @Override
  protected Map<String, String> settings() {
    String fetchSize = Integer.toString(ecmd().fetch());
    Map<String, String> defaultSettings = super.settings();
    String defaultFetchSize = defaultSettings.get(OPTION_MAX_BLOCK_SIZE);
    if (!"0".equals(fetchSize)) {
      if (!Objects.equals(defaultFetchSize, fetchSize)) {
        if (LOG.isWarnEnabled() && defaultFetchSize != null) {
          LOG.warn("overriding " + OPTION_MAX_BLOCK_SIZE + " option with new value " + fetchSize + ", was " + defaultSettings.get(OPTION_MAX_BLOCK_SIZE));
        }
        defaultSettings = new HashMap<>(defaultSettings);
        defaultSettings.put(OPTION_MAX_BLOCK_SIZE, fetchSize);
      }
    }
    return defaultSettings;
  }

  @Override
  protected void checkIfBusy() {
    conn.throwExceptionIfBusy(ecmd().cursorId());
  }

  @Override
  protected boolean isSuspended() {
    return ecmd().isSuspended();
  }

  private ExtendedQueryCommand<T> ecmd() {
    return (ExtendedQueryCommand<T>)cmd;
  }
}

package io.vertx.clickhouse.clickhousenative.impl;

import io.netty.channel.ChannelPipeline;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeCodec;
import io.vertx.core.Promise;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.command.InitCommand;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class ClickhouseNativeSocketConnection extends SocketConnectionBase {
  private ClickhouseNativeCodec codec;
  private ClickhouseNativeDatabaseMetadata md;
  private String pendingCursorId;

  public ClickhouseNativeSocketConnection(NetSocketInternal socket,
                            boolean cachePreparedStatements,
                            int preparedStatementCacheSize,
                            Predicate<String> preparedStatementCacheSqlFilter,
                            int pipeliningLimit,
                            EventLoopContext context) {
    super(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
  }

  @Override
  public void init() {
    codec = new ClickhouseNativeCodec(this);
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "codec", codec);
    super.init();
  }

  void sendStartupMessage(String username, String password, String database, Map<String, String> properties, Promise<Connection> completionHandler) {
    InitCommand cmd = new InitCommand(this, username, password, database, properties);
    schedule(cmd, completionHandler);
  }

  public void setDatabaseMetadata(ClickhouseNativeDatabaseMetadata md) {
    this.md = md;
  }

  public void setPendingCursorId(String cursorId) {
    this.pendingCursorId = cursorId;
  }

  public String getPendingCursorId() {
    return pendingCursorId;
  }

  public void releasePendingCursor(String cursorId) {
    if (!Objects.equals(this.pendingCursorId, cursorId)) {
      throw new IllegalArgumentException("can't release pending cursor: pending = " + pendingCursorId + "; provided: " + cursorId);
    }
    this.pendingCursorId = null;
  }

  public void throwExceptionIfBusy() {
    if (pendingCursorId != null) {
      throw new IllegalArgumentException("connection is busy with cursor " + pendingCursorId);
    }
  }

  @Override
  public ClickhouseNativeDatabaseMetadata getDatabaseMetaData() {
    return md;
  }
}

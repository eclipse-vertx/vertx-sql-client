package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.impl.command.*;

import java.util.ArrayDeque;

public class ClickhouseNativeEncoder extends ChannelOutboundHandlerAdapter {
  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseNativeEncoder.class);

  private final ArrayDeque<ClickhouseNativeCommandCodec<?, ?>> inflight;
  private final ClickhouseNativeSocketConnection conn;

  private ChannelHandlerContext chctx;

  public ClickhouseNativeEncoder(ArrayDeque<ClickhouseNativeCommandCodec<?, ?>> inflight, ClickhouseNativeSocketConnection conn) {
      this.inflight = inflight;
      this.conn = conn;
  }

  ClickhouseNativeSocketConnection getConn() {
    return conn;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    chctx = ctx;
  }

  ChannelHandlerContext chctx() {
    return chctx;
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof CommandBase<?>) {
      CommandBase<?> cmd = (CommandBase<?>) msg;
      write(cmd);
    } else {
      super.write(ctx, msg, promise);
    }
  }

  void write(CommandBase<?> cmd) {
    LOG.info("got command: " + cmd.getClass());
    ClickhouseNativeCommandCodec<?, ?> codec = wrap(cmd);
    codec.completionHandler = resp -> {
      ClickhouseNativeCommandCodec<?, ?> c = inflight.poll();
      resp.cmd = (CommandBase) c.cmd;
      chctx.fireChannelRead(resp);
    };
    inflight.add(codec);
    codec.encode(this);
  }

  private ClickhouseNativeCommandCodec<?, ?> wrap(CommandBase<?> cmd) {
    if (cmd instanceof InitCommand) {
      return new InitCommandCodec((InitCommand) cmd);
    } else if (cmd instanceof SimpleQueryCommand) {
      return new SimpleQueryCommandCodec<>((SimpleQueryCommand<?>) cmd, conn);
    } else if (cmd instanceof CloseConnectionCommand) {
      return new CloseConnectionCommandCodec((CloseConnectionCommand)cmd);
    } else if (cmd instanceof PrepareStatementCommand) {
      PrepareStatementCommand ps = (PrepareStatementCommand) cmd;
      QueryParsers.QueryType queryType = QueryParsers.queryType(ps.sql());
      return new PrepareStatementCodec(ps, queryType);
    } else if (cmd instanceof ExtendedQueryCommand) {
      ExtendedQueryCommand<?> ecmd = (ExtendedQueryCommand<?>) cmd;
      QueryParsers.QueryType queryType;
      if (ecmd.preparedStatement() != null) {
        queryType = ((ClickhouseNativePreparedStatement) ecmd.preparedStatement()).queryType();
      } else {
        queryType = QueryParsers.queryType(ecmd.sql());
      }
      if (queryType != null && queryType != QueryParsers.QueryType.INSERT && ecmd.isBatch() && ecmd.paramsList() != null && ecmd.paramsList().size() > 1) {
        RuntimeException ex = new UnsupportedOperationException("batch queries are supported for INSERTs only");
        deliverError(cmd, ex);
        throw ex;
      }
      return new ExtendedQueryCommandCodec<>(queryType, ecmd, conn);
    } else if (cmd instanceof CloseCursorCommand) {
      return new CloseCursorCommandCodec((CloseCursorCommand)cmd, conn);
    }
    RuntimeException ex = new UnsupportedOperationException(cmd.getClass().getName());
    deliverError(cmd, ex);
    throw ex;
  }

  private void deliverError(CommandBase cmd, RuntimeException ex) {
    if (cmd instanceof QueryCommandBase) {
      QueryCommandBase ecmd = (QueryCommandBase)cmd;
      ecmd.resultHandler().handleResult(0, 0, null, null, ex);
    }
    CommandResponse<Object> resp = CommandResponse.failure(ex);
    resp.cmd = cmd;
    chctx.fireChannelRead(resp);
  }
}

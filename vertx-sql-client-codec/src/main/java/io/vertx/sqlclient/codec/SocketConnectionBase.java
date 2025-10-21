/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.sqlclient.codec;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.*;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.net.NetSocketInternal;
import io.vertx.sqlclient.ClosedConnectionException;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.codec.impl.PreparedStatementCache;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.internal.PreparedStatement;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.spi.connection.ConnectionContext;
import io.vertx.sqlclient.spi.protocol.CloseConnectionCommand;
import io.vertx.sqlclient.spi.protocol.CloseStatementCommand;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.spi.protocol.CompositeCommand;
import io.vertx.sqlclient.spi.protocol.ExtendedQueryCommand;
import io.vertx.sqlclient.spi.protocol.PrepareStatementCommand;

import java.util.ArrayDeque;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class SocketConnectionBase implements Connection {

  public static final Collector<Row, Void, Void> NULL_COLLECTOR = Collector.of(() -> null, (v, row) -> {}, (v1, v2) -> null, Function.identity());
  private static final Completable<?> NULL_HANDLER = (res, err) -> {};

  public static final Logger logger = LoggerFactory.getLogger(SocketConnectionBase.class);

  public enum Status {

    CLOSED, CONNECTED, CLOSING

  }

  private static final String PENDING_CMD_CONNECTION_CORRUPT_MSG = "Pending requests failed to be sent due to connection has been closed.";

  private final ClientMetrics metrics;
  protected final PreparedStatementCache psCache;
  protected final ContextInternal context;
  private final Predicate<String> preparedStatementCacheSqlFilter;
  private ConnectionContext holder;
  private final int pipeliningLimit;

  // Command pipeline state
  private final ArrayDeque<CommandBase<?>> pending = new ArrayDeque<>();
  private final ArrayDeque<Completable<?>> handlers = new ArrayDeque<>();
  private final ArrayDeque<CommandMessage<?, ?>> inflights = new ArrayDeque<>();

  private boolean executing;
  private int inflight;
  private boolean paused;

  protected final NetSocketInternal socket;
  protected Status status = Status.CONNECTED;

  public SocketConnectionBase(NetSocketInternal socket,
                              ClientMetrics metrics,
                              boolean cachePreparedStatements,
                              int preparedStatementCacheSize,
                              Predicate<String> preparedStatementCacheSqlFilter,
                              int pipeliningLimit,
                              ContextInternal context) {
    this.socket = socket;
    this.context = context;
    this.pipeliningLimit = pipeliningLimit;
    this.metrics = metrics;
    this.paused = false;
    this.psCache = cachePreparedStatements ? new PreparedStatementCache(preparedStatementCacheSize) : null;
    this.preparedStatementCacheSqlFilter = preparedStatementCacheSqlFilter;
  }

  protected abstract SqlConnectOptions connectOptions();

  @Override
  public ClientMetrics metrics() {
    return metrics;
  }

  @Override
  public int pipeliningLimit() {
    return pipeliningLimit;
  }

  @Override
  public TracingPolicy tracingPolicy() {
    return connectOptions().getTracingPolicy();
  }

  @Override
  public String database() {
    return connectOptions().getDatabase();
  }

  @Override
  public String user() {
    return connectOptions().getUser();
  }

  @Override
  public DatabaseMetadata databaseMetadata() {
    return null;
  }

  public io.vertx.core.Context context() {
    return context;
  }

  public void init() {
    socket.closeHandler(this::handleClosed);
    socket.exceptionHandler(this::handleException);
    socket.messageHandler(msg -> {
      try {
        handleMessage(msg);
      } catch (Exception e) {
        handleException(e);
      }
    });
    socket.readCompletionHandler(this::handleReadComplete);
  }

  public NetSocketInternal socket() {
    return socket;
  }

  @Override
  public SocketAddress server() {
    return socket.remoteAddress();
  }

  public boolean isSsl() {
    return socket.isSsl();
  }

  @Override
  public boolean isValid() {
    return status == Status.CONNECTED;
  }

  @Override
  public void init(ConnectionContext holder) {
    ContextInternal context = (ContextInternal) Vertx.currentContext();
    if (context == null || context.nettyEventLoop() != this.context.nettyEventLoop()) {
      throw new IllegalStateException();
    }
    if (status != Status.CONNECTED) {
      throw new IllegalStateException();
    }
    this.holder = holder;
  }

  @Override
  public void close(ConnectionContext holder, Completable<Void> promise) {
    if (Vertx.currentContext() == context) {
      Channel ch = socket.channelHandlerContext().channel();
      if (status == Status.CONNECTED) {
        status = Status.CLOSING;
        // Append directly since schedule checks the status and won't enqueue the command
        pending.add(CloseConnectionCommand.INSTANCE);
        handlers.add(NULL_HANDLER);
        checkPending();
      }
      ch.closeFuture()
        .addListener((ChannelFutureListener) channelFuture -> promise.succeed());
    } else {
      context.runOnContext(v -> close(holder, promise));
    }
  }

  @Override
  public <R> void schedule(CommandBase<R> cmd, Completable<R> handler) {
    this.context.emit(v -> doSchedule(cmd, handler));
  }

  protected <R> void doSchedule(CommandBase<R> cmd, Completable<R> handler) {
    if (handler == null) {
      throw new IllegalArgumentException();
    }
    io.vertx.core.Context context = Vertx.currentContext();
    if (context != this.context) {
      throw new IllegalStateException();
    }
//    cmd.handler = handler;
    if (status == Status.CONNECTED) {
      if (cmd instanceof CompositeCommand) {
        CompositeCommand composite = (CompositeCommand) cmd;
        pending.addAll(composite.commands());
        handlers.addAll(composite.handlers());
        handler.succeed();
      } else {
        pending.add(cmd);
        handlers.add(handler);
      }
      checkPending();
    } else {
      handler.fail(VertxException.noStackTrace("Connection is not active now, current status: " + status));
    }
  }

  private void checkPending() {
    if (executing) {
      return;
    }
    try {
      executing = true;
      ChannelHandlerContext ctx = socket.channelHandlerContext();
      int written = 0;
      CommandBase<?> cmd;
      while (!paused && inflight < pipeliningLimit && (cmd = pending.poll()) != null) {
        inflight++;
        Completable<?> handler = handlers.poll();
        CommandMessage<?, ?> toSend;
        if (cmd instanceof ExtendedQueryCommand) {
          ExtendedQueryCommand queryCmd = (ExtendedQueryCommand) cmd;
          PreparedStatement ps = queryCmd.preparedStatement();
          if (ps == null) {
            if (psCache != null) {
              ps = psCache.get(queryCmd.sql());
            }
          }
          if (ps == null) {
            // Execute prepare
            boolean cache = psCache != null && preparedStatementCacheSqlFilter.test(queryCmd.sql());
            if (cache) {
              CommandMessage<?, ?> closeCmd = evictStatementIfNecessary();
              if (closeCmd != null) {
                inflight++;
                written++;
                fireCommandMessage(ctx, closeCmd);
              }
            }
            CommandMessage<?, ?> prepareCmd = prepareCommand(queryCmd, handler, cache, false);
            paused = true;
            inflight++;
            toSend = prepareCmd;
          } else {
            String msg = queryCmd.prepare(ps);
            if (msg != null) {
              inflight--;
              handler.fail(VertxException.noStackTrace(msg));
              continue;
            } else {
              toSend = createMessage(queryCmd, ps, handler);
            }
          }
        } else {
          toSend = createMessage(cmd, handler);
        }
        written++;
        fireCommandMessage(ctx, toSend);
      }
      if (written > 0) {
        ctx.flush();
      }
    } finally {
      executing = false;
    }
  }

  private void fireCommandMessage(ChannelHandlerContext chctx, CommandMessage<?, ?> msg) {
    inflights.add(msg);
    chctx.write(msg, chctx.voidPromise());
  }

  private CommandMessage<?, ?> createMessage(ExtendedQueryCommand<?> command, PreparedStatement preparedStatement, Completable<?> handler) {
    CommandMessage<?, ?> msg = toMessage(command, preparedStatement);
    msg.handler = (Completable) handler;
    return msg;
  }

  private CommandMessage<?, ?> createMessage(CommandBase<?> command, Completable<?> handler) {
    CommandMessage<?, ?> msg = toMessage(command);
    msg.handler = (Completable) handler;
    return msg;
  }

  protected CommandMessage<?, ?> toMessage(ExtendedQueryCommand<?> command, PreparedStatement preparedStatement) {
    throw new UnsupportedOperationException();
  }

  protected CommandMessage<?, ?> toMessage(CommandBase<?> command) {
    throw new UnsupportedOperationException();
  }

  private CommandMessage<?, ?> prepareCommand(ExtendedQueryCommand<?> queryCmd, Completable<?> handler, boolean cache, boolean sendParameterTypes) {
    PrepareStatementCommand prepareCmd = new PrepareStatementCommand(queryCmd.sql(), null, cache, sendParameterTypes ? queryCmd.parameterTypes() : null);
    CommandMessage<PreparedStatement, ?> prepareMsg = (CommandMessage) toMessage(prepareCmd);
    prepareMsg.handler = (ps, cause) -> {
      paused = false;
      if (cause == null) {
        if (cache) {
          cacheStatement(ps);
        }
        String msg = queryCmd.prepare(ps);
        if (msg != null) {
          inflight--;
          handler.fail(VertxException.noStackTrace(msg));
        } else {
          ChannelHandlerContext ctx = socket.channelHandlerContext();
          fireCommandMessage(ctx, createMessage(queryCmd, ps, handler));
          ctx.flush();
        }
      } else {
        if (queryCmd.autoCommit() && isIndeterminatePreparedStatementError(cause) && !sendParameterTypes) {
          ChannelHandlerContext ctx = socket.channelHandlerContext();
          // We cannot cache this prepared statement because it might be executed with another type
          fireCommandMessage(ctx, prepareCommand(queryCmd, handler, false, true));
          ctx.flush();
        } else {
          inflight--;
          handler.fail(cause);
        }
      }
    };
    return prepareMsg;
  }

  protected void handleMessage(Object msg) {
    if (msg instanceof CommandResponse) {
      inflight--;
      CommandResponse resp =(CommandResponse) msg;
      CommandMessage<?, ?> command = inflights.poll();
      fire(resp, command.handler);
    } else if (msg instanceof InvalidCachedStatementEvent) {
      InvalidCachedStatementEvent event = (InvalidCachedStatementEvent) msg;
      removeCachedStatement(event.sql());
    }
  }

  private static <R> void fire(CommandResponse<R> response, Completable<R> handler) {
    if (handler != null) {
      handler.complete(response.result, response.failure);
    }
  }

  private void handleReadComplete(Void v) {
    checkPending();
  }

  protected void handleEvent(Object event) {
    if (holder != null) {
      holder.handleEvent(event);
    }
  }

  private CommandMessage<?, ?> evictStatementIfNecessary() {
    if (psCache != null && psCache.isFull()) {
      PreparedStatement evicted = psCache.evict();
      CloseStatementCommand closeCmd = new CloseStatementCommand(evicted);
      CommandMessage<?, ?> msg = toMessage(closeCmd);
      msg.handler = (res, err) -> {
        if (err != null) {
          logger.error("Error when closing cached prepared statement", err);
        }
      };
      return msg;
    } else {
      return null;
    }
  }

  private void cacheStatement(PreparedStatement preparedStatement) {
    if (psCache != null) {
      List<PreparedStatement> evictedList = psCache.put(preparedStatement);
      assert evictedList.size() == 0;
    }
  }

  private void removeCachedStatement(String sql) {
    if (this.psCache != null) {
      this.psCache.remove(sql);
    }
  }

  private void handleClosed(Void v) {
    handleClose(null);
  }

  protected void handleException(Throwable t) {
    if (t instanceof DecoderException) {
      DecoderException err = (DecoderException) t;
      t = err.getCause();
    }
    handleClose(t);
  }

  protected void reportException(Throwable t) {
    synchronized (this) {
      if (holder != null) {
        holder.handleException(t);
      }
    }
  }

  protected void handleClose(Throwable t) {
    if (status != Status.CLOSED) {
      status = Status.CLOSED;
      if (metrics != null) {
        metrics.close();
      }
      if (t != null) {
        reportException(t);
      }
      CommandMessage<?, ?> msg;
      while ((msg = inflights.poll()) != null) {
        fail(msg, ClosedConnectionException.INSTANCE);
      }
      Throwable cause = t == null ? VertxException.noStackTrace(PENDING_CMD_CONNECTION_CORRUPT_MSG) : new VertxException(PENDING_CMD_CONNECTION_CORRUPT_MSG, t);
      CommandBase<?> cmd;
      while ((cmd = pending.poll()) != null) {
        CommandBase c = cmd;
        Completable handler = handlers.poll();
        fail(c, handler, cause);
      }
      if (holder != null) {
        holder.handleClosed();
      }
    }
  }

  private <R> void fail(CommandMessage<R, ?> msg, Throwable err) {
    fail(msg.cmd, msg.handler, err);
  }

  protected <R> void fail(CommandBase<R> command, Completable<R> handler, Throwable err) {
    context.runOnContext(v -> handler.fail(err));
  }

  public boolean pipeliningEnabled() {
    return pipeliningLimit > 1;
  }

  public void suspendPipeline() {
    this.paused = true;
  }

  public void resumePipeline() {
    this.paused = false;
  }

  protected void clearCachedStatements() {
    if (this.psCache != null) {
      this.psCache.clear();
    }
  }
}

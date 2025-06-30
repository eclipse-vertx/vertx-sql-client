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

package io.vertx.sqlclient.impl;

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
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.cache.PreparedStatementCache;
import io.vertx.sqlclient.impl.codec.InvalidCachedStatementEvent;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.internal.command.*;
import io.vertx.sqlclient.internal.PreparedStatement;
import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.util.ArrayDeque;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class SocketConnectionBase implements Connection {

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
  private Holder holder;
  private final int pipeliningLimit;

  // Command pipeline state
  private final ArrayDeque<CommandBase<?>> pending = new ArrayDeque<>();
  private final ArrayDeque<Completable<?>> handlers = new ArrayDeque<>();
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
  public DatabaseMetadata getDatabaseMetaData() {
    return null;
  }

  public Context context() {
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
  public void init(Holder holder) {
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
  public int getProcessId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getSecretKey() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close(Holder holder, Completable<Void> promise) {
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
    Context context = Vertx.currentContext();
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
          if (queryCmd.ps == null) {
            if (psCache != null) {
              queryCmd.ps = psCache.get(queryCmd.sql());
            }
          }
          if (queryCmd.ps == null) {
            // Execute prepare
            boolean cache = psCache != null && preparedStatementCacheSqlFilter.test(queryCmd.sql());
            if (cache) {
              CommandMessage<?, ?> closeCmd = evictStatementIfNecessary();
              if (closeCmd != null) {
                inflight++;
                written++;
                ctx.write(closeCmd, ctx.voidPromise());
              }
            }
            CommandMessage<?, ?> prepareCmd = prepareCommand(queryCmd, handler, cache, false);
            paused = true;
            inflight++;
            toSend = prepareCmd;
          } else {
            String msg = queryCmd.prepare();
            if (msg != null) {
              inflight--;
              handler.fail(VertxException.noStackTrace(msg));
              continue;
            } else {
              toSend = createMessage(cmd, handler);
            }
          }
        } else {
          toSend = createMessage(cmd, handler);
        }
        written++;
        ctx.write(toSend, ctx.voidPromise());
      }
      if (written > 0) {
        ctx.flush();
      }
    } finally {
      executing = false;
    }
  }

  private CommandMessage<?, ?> createMessage(CommandBase<?> command, Completable<?> handler) {
    CommandMessage<?, ?> msg = toMessage(command);
    msg.handler = (Completable) handler;
/*
    if (command.handler != null) {
      msg.handler = (Completable) command.handler;
      command.handler = (a, r) -> {
        System.out.println("Handle me ------------------------");
        new Exception().printStackTrace(System.out);
      };
    }
*/
    return msg;
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
        queryCmd.ps = ps;
        String msg = queryCmd.prepare();
        if (msg != null) {
          inflight--;
          handler.fail(VertxException.noStackTrace(msg));
        } else {
          ChannelHandlerContext ctx = socket.channelHandlerContext();
          ctx.write(createMessage(queryCmd, handler), ctx.voidPromise());
          ctx.flush();
        }
      } else {
        if (isIndeterminatePreparedStatementError(cause) && !sendParameterTypes) {
          ChannelHandlerContext ctx = socket.channelHandlerContext();
          // We cannot cache this prepared statement because it might be executed with another type
          ctx.write(prepareCommand(queryCmd, handler, false, true), ctx.voidPromise());
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
      resp.fire();
    } else if (msg instanceof InvalidCachedStatementEvent) {
      InvalidCachedStatementEvent event = (InvalidCachedStatementEvent) msg;
      removeCachedStatement(event.sql());
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
      Throwable cause = t == null ? VertxException.noStackTrace(PENDING_CMD_CONNECTION_CORRUPT_MSG) : new VertxException(PENDING_CMD_CONNECTION_CORRUPT_MSG, t);
      CommandBase<?> cmd;
      while ((cmd = pending.poll()) != null) {
        CommandBase<?> c = cmd;
        Completable<?> handler = handlers.poll();
        context.runOnContext(v -> handler.fail(cause));
      }
      if (holder != null) {
        holder.handleClosed();
      }
    }
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
}

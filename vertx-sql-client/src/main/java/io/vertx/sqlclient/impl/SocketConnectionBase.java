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

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.sqlclient.impl.cache.PreparedStatementCache;
import io.vertx.sqlclient.impl.codec.InvalidCachedStatementEvent;
import io.vertx.sqlclient.impl.command.*;

import java.util.ArrayDeque;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class SocketConnectionBase implements Connection {

  protected static final Logger logger = LoggerFactory.getLogger(SocketConnectionBase.class);

  public enum Status {

    CLOSED, CONNECTED, CLOSING

  }

  private static final String PENDING_CMD_CONNECTION_CORRUPT_MSG = "Pending requests failed to be sent due to connection has been closed.";

  protected final PreparedStatementCache psCache;
  private final Predicate<String> preparedStatementCacheSqlFilter;
  private final EventLoopContext context;
  private Holder holder;
  private final int pipeliningLimit;

  // Command pipeline state
  private final ArrayDeque<CommandBase<?>> pending = new ArrayDeque<>();
  private boolean executing;
  private int inflight;
  private boolean paused;

  protected final NetSocketInternal socket;
  protected Status status = Status.CONNECTED;

  public SocketConnectionBase(NetSocketInternal socket,
                              boolean cachePreparedStatements,
                              int preparedStatementCacheSize,
                              Predicate<String> preparedStatementCacheSqlFilter,
                              int pipeliningLimit,
                              EventLoopContext context) {
    this.socket = socket;
    this.context = context;
    this.pipeliningLimit = pipeliningLimit;
    this.paused = false;
    this.psCache = cachePreparedStatements ? new PreparedStatementCache(preparedStatementCacheSize) : null;
    this.preparedStatementCacheSqlFilter = preparedStatementCacheSqlFilter;
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
  }

  public NetSocketInternal socket() {
    return socket;
  }

  public boolean isSsl() {
    return socket.isSsl();
  }

  @Override
  public void init(Holder holder) {
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
  public void close(Holder holder, Promise<Void> promise) {
    if (Vertx.currentContext() == context) {
      if (status == Status.CONNECTED) {
        status = Status.CLOSING;
        // Append directly since schedule checks the status and won't enqueue the command
        socket.channelHandlerContext()
          .channel()
          .closeFuture()
          .addListener((ChannelFutureListener) channelFuture -> promise.complete());
        pending.add(CloseConnectionCommand.INSTANCE);
        checkPending();
      }
    } else {
      context.runOnContext(v -> close(holder, promise));
    }
  }

  @Override
  public <R> void schedule(CommandBase<R> cmd, Promise<R> promise) {
    context.emit(v -> doSchedule(cmd, promise));
  }

  protected <R> void doSchedule(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
    if (handler == null) {
      throw new IllegalArgumentException();
    }
    Context context = Vertx.currentContext();
    if (context != this.context) {
      throw new IllegalStateException();
    }
    cmd.handler = handler;
    if (status == Status.CONNECTED) {
      pending.add(cmd);
      checkPending();
    } else {
      cmd.fail(new NoStackTraceThrowable("Connection is not active now, current status: " + status));
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
              CloseStatementCommand closeCmd = evictStatementIfNecessary();
              if (closeCmd != null) {
                inflight++;
                written++;
                ctx.write(closeCmd, ctx.voidPromise());
              }
            }
            PrepareStatementCommand prepareCmd = prepareCommand(queryCmd, cache, false);
            paused = true;
            inflight++;
            cmd = prepareCmd;
          } else {
            String msg = queryCmd.prepare();
            if (msg != null) {
              inflight--;
              queryCmd.fail(new NoStackTraceThrowable(msg));
              continue;
            }
          }
        }
        written++;
        ctx.write(cmd, ctx.voidPromise());
      }
      if (written > 0) {
        ctx.flush();
      }
    } finally {
      executing = false;
    }
  }

  private PrepareStatementCommand prepareCommand(ExtendedQueryCommand<?> queryCmd, boolean cache, boolean sendParameterTypes) {
    PrepareStatementCommand prepareCmd = new PrepareStatementCommand(queryCmd.sql(), cache, sendParameterTypes ? queryCmd.parameterTypes() : null);
    prepareCmd.handler = ar -> {
      paused = false;
      if (ar.succeeded()) {
        PreparedStatement ps = ar.result();
        if (cache) {
          cacheStatement(ps);
        }
        queryCmd.ps = ps;
        String msg = queryCmd.prepare();
        if (msg != null) {
          inflight--;
          queryCmd.fail(new NoStackTraceThrowable(msg));
        } else {
          ChannelHandlerContext ctx = socket.channelHandlerContext();
          ctx.write(queryCmd, ctx.voidPromise());
          ctx.flush();
        }
      } else {
        Throwable cause = ar.cause();
        if (isIndeterminatePreparedStatementError(cause) && !sendParameterTypes) {
          ChannelHandlerContext ctx = socket.channelHandlerContext();
          // We cannot cache this prepared statement because it might be executed with another type
          ctx.write(prepareCommand(queryCmd, false, true), ctx.voidPromise());
          ctx.flush();
        } else {
          inflight--;
          queryCmd.fail(cause);
        }
      }
    };
    return prepareCmd;
  }

  protected void handleMessage(Object msg) {
    if (msg instanceof CommandResponse) {
      inflight--;
      CommandResponse resp =(CommandResponse) msg;
      resp.fire();
      checkPending();
    } else if (msg instanceof InvalidCachedStatementEvent) {
      InvalidCachedStatementEvent event = (InvalidCachedStatementEvent) msg;
      removeCachedStatement(event.sql());
    }
  }

  protected void handleEvent(Object event) {
    if (holder != null) {
      holder.handleEvent(event);
    }
  }

  private CloseStatementCommand evictStatementIfNecessary() {
    if (psCache != null && psCache.isFull()) {
      PreparedStatement evicted = psCache.evict();
      CloseStatementCommand closeCmd = new CloseStatementCommand(evicted);
      closeCmd.handler = ar -> {
        if (ar.failed()) {
          logger.error("Error when closing cached prepared statement", ar.cause());
        }
      };
      return closeCmd;
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

  private synchronized void handleException(Throwable t) {
    if (t instanceof DecoderException) {
      DecoderException err = (DecoderException) t;
      t = err.getCause();
    }
    handleClose(t);
  }

  protected void handleClose(Throwable t) {
    if (status != Status.CLOSED) {
      status = Status.CLOSED;
      if (t != null) {
        synchronized (this) {
          if (holder != null) {
            holder.handleException(t);
          }
        }
      }
      Throwable cause = t == null ? new NoStackTraceThrowable(PENDING_CMD_CONNECTION_CORRUPT_MSG) : new VertxException(PENDING_CMD_CONNECTION_CORRUPT_MSG, t);
      CommandBase<?> cmd;
      while ((cmd = pending.poll()) != null) {
        CommandBase<?> c = cmd;
        context.runOnContext(v -> c.fail(cause));
      }
      if (holder != null) {
        holder.handleClosed();
      }
    }
  }
}

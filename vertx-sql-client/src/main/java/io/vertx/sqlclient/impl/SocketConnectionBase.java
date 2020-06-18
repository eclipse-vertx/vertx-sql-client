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
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.sqlclient.impl.codec.InvalidCachedStatementEvent;
import io.vertx.sqlclient.impl.command.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class SocketConnectionBase implements Connection {

  protected static final Logger logger = LoggerFactory.getLogger(SocketConnectionBase.class);

  public enum Status {

    CLOSED, CONNECTED, CLOSING

  }

  protected final Map<String, PreparedStatement> psCache;
  private final int preparedStatementCacheSqlLimit;
  private final ArrayDeque<CommandBase<?>> pending = new ArrayDeque<>();
  private final ContextInternal context;
  private int inflight;
  private boolean paused;
  private Holder holder;
  private final int pipeliningLimit;

  protected final NetSocketInternal socket;
  protected Status status = Status.CONNECTED;

  public SocketConnectionBase(NetSocketInternal socket,
                              boolean cachePreparedStatements,
                              int preparedStatementCacheSize,
                              int preparedStatementCacheSqlLimit,
                              int pipeliningLimit,
                              ContextInternal context) {
    this.socket = socket;
    this.context = context;
    this.pipeliningLimit = pipeliningLimit;
    this.paused = false;
    this.psCache = cachePreparedStatements ? new HashMap<>() : null;
    this.preparedStatementCacheSqlLimit = preparedStatementCacheSqlLimit;
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
    context.dispatch(null, v -> doSchedule(cmd, promise));
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
      cmd.fail(new VertxException("Connection not open " + status));
    }
  }

  private void checkPending() {
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
          PrepareStatementCommand prepareCmd = new PrepareStatementCommand(queryCmd.sql(), true);
          prepareCmd.handler = ar -> {
            paused = false;
            if (ar.succeeded()) {
              PreparedStatement ps = ar.result();
              cacheStatement(ps);
              queryCmd.ps = ps;
              String msg = queryCmd.prepare();
              if (msg != null) {
                queryCmd.fail(new NoStackTraceThrowable(msg));
              } else {
                ctx.write(queryCmd);
                ctx.flush();
              }
            } else {
              queryCmd.fail(ar.cause());
            }
          };
          paused = true;
          inflight++;
          cmd = prepareCmd;
        }
      }
      written++;
      ctx.write(cmd);
    }
    if (written > 0) {
      ctx.flush();
    }
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

  private void cacheStatement(PreparedStatement preparedStatement) {
    if (psCache != null) {
      psCache.put(preparedStatement.sql(), preparedStatement);
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
      Throwable cause = t == null ? new VertxException("closed") : t;
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

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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.*;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.sqlclient.impl.cache.PreparedStatementCache;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.sqlclient.impl.codec.InvalidCachedStatementEvent;
import io.vertx.sqlclient.impl.command.*;

import java.util.ArrayDeque;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class SocketConnectionBase implements Connection {

  private static final Logger logger = LoggerFactory.getLogger(SocketConnectionBase.class);

  public enum Status {

    CLOSED, CONNECTED, CLOSING

  }

  private static final String PENDING_CMD_CONNECTION_CORRUPT_MSG = "Pending requests failed to be sent due to connection has been closed.";

  protected final PreparedStatementCache psCache;
  private final Predicate<String> preparedStatementCacheSqlFilter;
  private final Context context;
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
                              Context context) {
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
  public void close(Holder holder) {
    if (Vertx.currentContext() == context) {
      if (status == Status.CONNECTED) {
        status = Status.CLOSING;
        // Append directly since schedule checks the status and won't enqueue the command
        pending.add(CloseConnectionCommand.INSTANCE);
        checkPending();
      }
    } else {
      context.runOnContext(v -> close(holder));
    }
  }

  public void schedule(CommandBase<?> cmd) {
    if (cmd.handler == null) {
      throw new IllegalArgumentException();
    }
    if (Vertx.currentContext() != context) {
      throw new IllegalStateException();
    }

    //
    if (status == Status.CONNECTED) {
      pending.add(cmd);
      checkPending();
    } else {
      cmd.fail(new VertxException("Connection not open " + status));
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
                ctx.write(closeCmd);
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
        ctx.write(cmd);
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
          ctx.write(queryCmd);
          ctx.flush();
        }
      } else {
        Throwable cause = ar.cause();
        if (isIndeterminatePreparedStatementError(cause) && !sendParameterTypes) {
          ChannelHandlerContext ctx = socket.channelHandlerContext();
          // We cannot cache this prepared statement because it might be executed with another type
          ctx.write(prepareCommand(queryCmd, false, true));
          ctx.flush();
        } else {
          inflight--;
          queryCmd.fail(cause);
        }
      }
    };
    return prepareCmd;
  }

  public void handleMessage(Object msg) {
    if (msg instanceof CommandResponse) {
      inflight--;
      CommandResponse resp =(CommandResponse) msg;
      resp.cmd.handler.handle(msg);
      checkPending();
    } else if (msg instanceof Notification) {
      handleNotification((Notification) msg);
    } else if (msg instanceof Notice) {
      handleNotice((Notice) msg);
    } else if (msg instanceof InvalidCachedStatementEvent) {
      InvalidCachedStatementEvent event = (InvalidCachedStatementEvent) msg;
      removeCachedStatement(event.sql());
    }
  }

  private void handleNotification(Notification response) {
    if (holder != null) {
      holder.handleNotification(response.getProcessId(), response.getChannel(), response.getPayload());
    }
  }

  private void handleNotice(Notice notice) {
    notice.log(logger);
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

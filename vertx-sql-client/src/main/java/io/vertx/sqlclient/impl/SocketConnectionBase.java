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
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.sqlclient.impl.command.*;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class SocketConnectionBase implements Connection {

  private static final Logger logger = LoggerFactory.getLogger(SocketConnectionBase.class);

  public enum Status {

    CLOSED, CONNECTED, CLOSING

  }

  protected final PreparedStatementCache psCache;
  private final int preparedStatementCacheSqlLimit;
  private final StringLongSequence psSeq = new StringLongSequence();
  private final ArrayDeque<CommandBase<?>> pending = new ArrayDeque<>();
  private final ContextInternal context;
  private int inflight;
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
    this.psCache = cachePreparedStatements ? new PreparedStatementCache(preparedStatementCacheSize, this) : null;
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

  @Override
  public <R> void schedule(CommandBase<R> cmd, Promise<R> promise) {
    context.dispatch(null, v -> doSchedule(cmd, promise));
  }

  private <R, T> void doSchedule(BiCommand<T, R> cmd, Handler<AsyncResult<R>> handler) {
    doSchedule(cmd.first, cr -> {
      if (cr.succeeded()) {
        AsyncResult<CommandBase<R>> next = cmd.then.apply(cr.result());
        if (next.succeeded()) {
          doSchedule(next.result(), handler);
        } else {
          handler.handle(Future.failedFuture(next.cause()));
        }
      } else {
        handler.handle(Future.failedFuture(cr.cause()));
      }
    });
  }

  private <R> void doSchedule(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
    if (handler == null) {
      throw new IllegalArgumentException();
    }
    Context context = Vertx.currentContext();
    if (context != this.context) {
      throw new IllegalStateException();
    }

    // Special handling for bi commands
    if (cmd instanceof BiCommand<?, ?>) {
      doSchedule((BiCommand<? ,R>) cmd, handler);
      return;
    }

    // Special handling for cache
    PreparedStatementCache psCache = this.psCache;
    if (psCache != null && cmd instanceof PrepareStatementCommand) {
      PrepareStatementCommand psCmd = (PrepareStatementCommand) cmd;
      if (psCmd.sql().length() > preparedStatementCacheSqlLimit) {
        // do not cache the statements
        return;
      }
      CachedPreparedStatement cached = psCache.get(psCmd.sql());
      Handler<AsyncResult<PreparedStatement>> orig = (Handler) handler;
      if (cached != null) {
        psCmd.handler = orig;
        cached.get(psCmd::complete);
        return;
      } else {
        if (psCache.size() >= psCache.getCapacity() && !psCache.isReady()) {
          // only if the prepared statement is ready then it can be evicted
        } else {
          cached = new CachedPreparedStatement();
          psCmd.statement = psSeq.next();
          cached.get(orig);
          psCache.put(psCmd.sql(), cached);
          handler = (Handler) cached;
        }
      }
    }

    //
    cmd.handler = handler;

    //
    if (status == Status.CONNECTED) {
      pending.add(cmd);
      checkPending();
    } else {
      cmd.fail(new VertxException("Connection not open " + status));
    }
  }

  static class CachedPreparedStatement implements Handler<AsyncResult<PreparedStatement>> {

    private final Deque<Handler<AsyncResult<PreparedStatement>>> waiters = new ArrayDeque<>();
    AsyncResult<PreparedStatement> resp;

    void get(Handler<AsyncResult<PreparedStatement>> handler) {
      if (resp != null) {
        handler.handle(resp);
      } else {
        waiters.add(handler);
      }
    }

    @Override
    public void handle(AsyncResult<PreparedStatement> event) {
      resp = event;
      Handler<AsyncResult<PreparedStatement>> waiter;
      while ((waiter = waiters.poll()) != null) {
        waiter.handle(resp);
      }
    }
  }

  private void checkPending() {
    ChannelHandlerContext ctx = socket.channelHandlerContext();
    if (inflight < pipeliningLimit) {
      CommandBase<?> cmd;
      while (inflight < pipeliningLimit && (cmd = pending.poll()) != null) {
        inflight++;
        ctx.write(cmd);
      }
      ctx.flush();
    }
  }

  private void handleMessage(Object msg) {
    if (msg instanceof CommandResponse) {
      inflight--;
      checkPending();
      CommandResponse resp =(CommandResponse) msg;
      resp.fire();
    } else if (msg instanceof Notification) {
      handleNotification((Notification) msg);
    } else if (msg instanceof Notice) {
      handleNotice((Notice) msg);
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

  private void handleClose(Throwable t) {
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

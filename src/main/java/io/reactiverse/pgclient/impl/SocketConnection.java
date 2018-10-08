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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.impl.codec.decoder.MessageDecoder;
import io.reactiverse.pgclient.impl.codec.decoder.InitiateSslHandler;
import io.reactiverse.pgclient.impl.codec.decoder.NoticeResponse;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.reactiverse.pgclient.impl.codec.decoder.NotificationResponse;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.*;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SocketConnection implements Connection {

  private static final Logger logger = LoggerFactory.getLogger(SocketConnection.class);

  enum Status {

    CLOSED, CONNECTED, CLOSING

  }

  private final NetSocketInternal socket;
  private final ArrayDeque<CommandBase<?>> inflight = new ArrayDeque<>();
  private final ArrayDeque<CommandBase<?>> pending = new ArrayDeque<>();
  private final boolean ssl;
  private final Context context;
  private Status status = Status.CONNECTED;
  private Holder holder;
  private final Map<String, CachedPreparedStatement> psCache;
  private final StringLongSequence psSeq = new StringLongSequence();
  private final int pipeliningLimit;
  private MessageDecoder decoder;
  private MessageEncoder encoder;

  public SocketConnection(NetSocketInternal socket,
                          boolean cachePreparedStatements,
                          int pipeliningLimit,
                          boolean ssl,
                          Context context) {
    this.socket = socket;
    this.ssl = ssl;
    this.context = context;
    this.psCache = cachePreparedStatements ? new ConcurrentHashMap<>() : null;
    this.pipeliningLimit = pipeliningLimit;
  }

  public Context context() {
    return context;
  }

  void initiateProtocolOrSsl(String username, String password, String database, Handler<? super CommandResponse<Connection>> completionHandler) {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    if (ssl) {
      Future<Void> upgradeFuture = Future.future();
      upgradeFuture.setHandler(ar -> {
        if (ar.succeeded()) {
          initiateProtocol(username, password, database, completionHandler);
        } else {
          Throwable cause = ar.cause();
          if (cause instanceof DecoderException) {
            DecoderException err = (DecoderException) cause;
            cause = err.getCause();
          }
          completionHandler.handle(CommandResponse.failure(cause));
        }
      });
      pipeline.addBefore("handler", "initiate-ssl-handler", new InitiateSslHandler(this, upgradeFuture));
    } else {
      initiateProtocol(username, password, database, completionHandler);
    }
  }

  private void initiateProtocol(String username, String password, String database, Handler<? super CommandResponse<Connection>> completionHandler) {
    decoder = new MessageDecoder(inflight, socket.channelHandlerContext().alloc());
    encoder = new MessageEncoder(socket.channelHandlerContext());

    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "decoder", decoder);

    socket.closeHandler(this::handleClosed);
    socket.exceptionHandler(this::handleException);
    socket.messageHandler(msg -> {
      try {
        handleMessage(msg);
      } catch (Exception e) {
        handleException(e);
      }
    });
    InitCommand cmd = new InitCommand(this, username, password, database);
    cmd.handler = completionHandler;
    schedule(cmd);
  }

  static class CachedPreparedStatement implements Handler<CommandResponse<PreparedStatement>> {

    private CommandResponse<PreparedStatement> resp;
    private final ArrayDeque<Handler<? super CommandResponse<PreparedStatement>>> waiters = new ArrayDeque<>();

    void get(Handler<? super CommandResponse<PreparedStatement>> handler) {
      if (resp != null) {
        handler.handle(resp);
      } else {
        waiters.add(handler);
      }
    }

    @Override
    public void handle(CommandResponse<PreparedStatement> event) {
      resp = event;
      Handler<? super CommandResponse<PreparedStatement>> waiter;
      while ((waiter = waiters.poll()) != null) {
        waiter.handle(resp);
      }
    }
  }

  public boolean isSsl() {
    return socket.isSsl();
  }

  public void upgradeToSSL(Handler<Void> handler) {
    socket.upgradeToSsl(v -> {
      handler.handle(null);
    });
  }

  @Override
  public void init(Holder holder) {
    this.holder = holder;
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

    // Special handling for cache
    if (cmd instanceof PrepareStatementCommand) {
      PrepareStatementCommand psCmd = (PrepareStatementCommand) cmd;
      Map<String, SocketConnection.CachedPreparedStatement> psCache = this.psCache;
      if (psCache != null) {
        SocketConnection.CachedPreparedStatement cached = psCache.get(psCmd.sql);
        if (cached != null) {
          Handler<? super CommandResponse<PreparedStatement>> handler = psCmd.handler;
          cached.get(handler);
          return;
        } else {
          psCmd.statement = psSeq.next();
          psCmd.cached = cached = new SocketConnection.CachedPreparedStatement();
          psCache.put(psCmd.sql, cached);
          Handler<? super CommandResponse<PreparedStatement>> a = psCmd.handler;
          psCmd.cached.get(a);
          psCmd.handler = psCmd.cached;
        }
      }
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
    if (inflight.size() < pipeliningLimit) {
      CommandBase<?> cmd;
      while (inflight.size() < pipeliningLimit && (cmd = pending.poll()) != null) {
        inflight.add(cmd);
        decoder.run(cmd);
        cmd.exec(encoder);
      }
      encoder.flush();
    }
  }

  private void handleMessage(Object msg) {
    if (msg instanceof CommandResponse) {
      CommandBase cmd = inflight.poll();
      checkPending();
      cmd.handler.handle(msg);
    } else if (msg instanceof NotificationResponse) {
      handleNotification((NotificationResponse) msg);
    } else if (msg instanceof NoticeResponse) {
      handleNotice((NoticeResponse) msg);
    }
  }

  private void handleNotification(NotificationResponse response) {
    if (holder != null) {
      holder.handleNotification(response.getProcessId(), response.getChannel(), response.getPayload());
    }
  }

  private void handleNotice(NoticeResponse notice) {
    logger.warn("Backend notice: " +
      "severity='" + notice.getSeverity() + "'" +
      ", code='" + notice.getCode() + "'" +
      ", message='" + notice.getMessage() + "'" +
      ", detail='" + notice.getDetail() + "'" +
      ", hint='" + notice.getHint() + "'" +
      ", position='" + notice.getPosition() + "'" +
      ", internalPosition='" + notice.getInternalPosition() + "'" +
      ", internalQuery='" + notice.getInternalQuery() + "'" +
      ", where='" + notice.getWhere() + "'" +
      ", file='" + notice.getFile() + "'" +
      ", line='" + notice.getLine() + "'" +
      ", routine='" + notice.getRoutine() + "'" +
      ", schema='" + notice.getSchema() + "'" +
      ", table='" + notice.getTable() + "'" +
      ", column='" + notice.getColumn() + "'" +
      ", dataType='" + notice.getDataType() + "'" +
      ", constraint='" + notice.getConstraint() + "'");
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
      for (ArrayDeque<CommandBase<?>> q : Arrays.asList(inflight, pending)) {
        CommandBase<?> cmd;
        while ((cmd = q.poll()) != null) {
          CommandBase<?> c = cmd;
          context.runOnContext(v -> c.fail(cause));
        }
      }
      if (holder != null) {
        holder.handleClosed();
      }
    }
  }
}

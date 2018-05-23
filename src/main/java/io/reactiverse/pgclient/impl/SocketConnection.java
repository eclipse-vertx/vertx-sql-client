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

import io.reactiverse.pgclient.impl.codec.decoder.DecodeContext;
import io.reactiverse.pgclient.impl.codec.decoder.InboundMessage;
import io.reactiverse.pgclient.impl.codec.decoder.MessageDecoder;
import io.reactiverse.pgclient.impl.codec.decoder.InitiateSslHandler;
import io.reactiverse.pgclient.impl.codec.encoder.OutboundMessage;
import io.reactiverse.pgclient.impl.codec.decoder.message.NotificationResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.*;
import io.vertx.core.impl.NetSocketInternal;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SocketConnection implements Connection {

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
  final Map<String, CachedPreparedStatement> psCache;
  private final int pipeliningLimit;
  final Deque<DecodeContext> decodeQueue = new ArrayDeque<>();
  final StringLongSequence psSeq = new StringLongSequence();

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
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "decoder", new MessageDecoder(decodeQueue));
    socket.closeHandler(this::handleClosed);
    socket.exceptionHandler(this::handleException);
    socket.messageHandler(this::handleMessage);
    schedule(new InitCommand(username, password, database, completionHandler));
  }

  static class CachedPreparedStatement implements Handler<AsyncResult<PreparedStatement>> {

    final Future<PreparedStatement> fut = Future.<PreparedStatement>future().setHandler(this);
    final ArrayDeque<Handler<AsyncResult<PreparedStatement>>> waiters = new ArrayDeque<>();

    void get(Handler<AsyncResult<PreparedStatement>> handler) {
      if (fut.isComplete()) {
        handler.handle(fut);
      } else {
        waiters.add(handler);
      }
    }

    @Override
    public void handle(AsyncResult<PreparedStatement> event) {
      Handler<AsyncResult<PreparedStatement>> waiter;
      while ((waiter = waiters.poll()) != null) {
        waiter.handle(fut);
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

  private boolean cork = false;
  private ArrayDeque<OutboundMessage> outbound = new ArrayDeque<>();

  void writeMessage(OutboundMessage cmd) {
    if (cork) {
      outbound.add(cmd);
    } else {
      ByteBuf out = null;
      try {
        out = socket.channelHandlerContext().alloc().ioBuffer();
        cmd.encode(out);
        socket.writeMessage(out);
        out = null;
      } finally {
        if (out != null) {
          out.release();
        }
      }
    }
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
          cached.get(psCmd.handler);
          return;
        } else {
          psCmd.statement = psSeq.next();
          psCmd.cached = cached = new SocketConnection.CachedPreparedStatement();
          psCache.put(psCmd.sql, cached);
        }
      }
    }

    //
    if (status == Status.CONNECTED) {
      pending.add(cmd);
      cmd.completionHandler = v -> {
        inflight.poll();
        checkPending();
      };
      checkPending();
    } else {
      cmd.fail(new VertxException("Connection not open " + status));
    }
  }

  private void checkPending() {
    if (inflight.size() < pipeliningLimit) {
      CommandBase<?> cmd;
      while (inflight.size() < pipeliningLimit && (cmd = pending.poll()) != null) {
        cork = true;
        inflight.add(cmd);
        cmd.exec(this);
        if (outbound.size() > 0) {
          ByteBuf out = null;
          try {
            out = socket.channelHandlerContext().alloc().ioBuffer();
            OutboundMessage msg;
            while ((msg = outbound.poll()) != null) {
              msg.encode(out);
            }
            socket.writeMessage(out);
            out = null;
          } finally {
            if (out != null) {
              out.release();
            }
          }
        }
        cork = false;
      }
    }
  }

  private void handleMessage(Object msg) {
    // System.out.println("<-- " + msg);
    if (msg instanceof NotificationResponse) {
      handleNotification((NotificationResponse) msg);
    } else {
      InboundMessage pgMsg = (InboundMessage) msg;
      CommandBase<?> cmd = inflight.peek();
      if (cmd != null) {
        cmd.handleMessage(pgMsg);
      } else {
        System.out.println("Uh oh, no inflight command for " + msg);
      }
    }
  }

  private void handleNotification(NotificationResponse response) {
    if (holder != null) {
      holder.handleNotification(response.getProcessId(), response.getChannel(), response.getPayload());
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

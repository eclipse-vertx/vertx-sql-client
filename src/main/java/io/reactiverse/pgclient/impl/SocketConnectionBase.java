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

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DecoderException;
import io.reactiverse.pgclient.impl.codec.decoder.InitiateSslHandler;
import io.reactiverse.pgclient.impl.codec.decoder.MessageDecoder;
import io.reactiverse.pgclient.impl.codec.decoder.NoticeResponse;
import io.reactiverse.pgclient.impl.codec.decoder.NotificationResponse;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class SocketConnectionBase implements Connection {

  private static final Logger logger = LoggerFactory.getLogger(SocketConnectionBase.class);

  enum Status {

    CLOSED, CONNECTED, CLOSING

  }

  private final ArrayDeque<CommandBase<?>> inflight = new ArrayDeque<>();
  private final ArrayDeque<CommandBase<?>> pending = new ArrayDeque<>();
  private final Context context;
  private Holder holder;
  private final int pipeliningLimit;
  private MessageDecoder decoder;
  private MessageEncoder encoder;

  protected final NetSocketInternal socket;
  protected Status status = Status.CONNECTED;

  public SocketConnectionBase(NetSocketInternal socket,
                              int pipeliningLimit,
                              Context context) {
    this.socket = socket;
    this.context = context;
    this.pipeliningLimit = pipeliningLimit;
  }

  public Context context() {
    return context;
  }

  void upgradeToSSLConnection(Handler<AsyncResult<Void>> completionHandler) {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    Future<Void> upgradeFuture = Future.future();
    upgradeFuture.setHandler(ar->{
      if (ar.succeeded()) {
        completionHandler.handle(Future.succeededFuture());
      } else {
        Throwable cause = ar.cause();
        if (cause instanceof DecoderException) {
          DecoderException err = (DecoderException) cause;
          cause = err.getCause();
        }
        completionHandler.handle(Future.failedFuture(cause));
      }
    });
    pipeline.addBefore("handler", "initiate-ssl-handler", new InitiateSslHandler(this, upgradeFuture));
  }

  void initializeCodec() {
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

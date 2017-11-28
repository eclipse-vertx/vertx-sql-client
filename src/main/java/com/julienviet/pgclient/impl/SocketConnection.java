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

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.decoder.DecodeContext;
import com.julienviet.pgclient.codec.decoder.InboundMessage;
import com.julienviet.pgclient.codec.decoder.MessageDecoder;
import com.julienviet.pgclient.codec.encoder.MessageEncoder;
import com.julienviet.pgclient.codec.encoder.OutboundMessage;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.*;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.NetSocketInternal;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SocketConnection implements Connection {

  enum Status {

    CLOSED, CONNECTED, CLOSING

  }

  private final NetSocketInternal socket;
  private final ArrayDeque<CommandBase> inflight = new ArrayDeque<>();
  private final ArrayDeque<CommandBase> pending = new ArrayDeque<>();
  private final PgClientImpl client;
  final Context context;
  private Status status = Status.CONNECTED;
  private Holder holder;
  private final Map<String, CachedPreparedStatement> psCache;
  private final int pipeliningLimit;
  final Deque<DecodeContext> decodeQueue = new ArrayDeque<>();
  private int psSeq;

  public SocketConnection(PgClientImpl client,
                          NetSocketInternal socket,
                          ContextImpl context) {
    this.socket = socket;
    this.client = client;
    this.context = context;
    this.psCache = client.cachePreparedStatements ? new ConcurrentHashMap<>() : null;
    this.pipeliningLimit = client.pipeliningLimit;
  }

  void init(String username, String password, String database, Handler<AsyncResult<Connection>> completionHandler) {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "decoder", new MessageDecoder(decodeQueue));
    pipeline.addBefore("handler", "encoder", new MessageEncoder());
    socket.closeHandler(this::handleClosed);
    socket.exceptionHandler(this::handleException);
    socket.messageHandler(this::handleMessage);
    schedule(new InitCommand(username, password, database, client.ssl, completionHandler));
  }

  class CachedPreparedStatement implements Handler<AsyncResult<PreparedStatement>> {

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

  void upgradeToSSL(Handler<Void> handler) {
    socket.upgradeToSsl(handler);
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
      socket.writeMessage(cmd);
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

  @Override
  public void schedulePrepared(String sql, Function<AsyncResult<PreparedStatement>, CommandBase> supplier, Handler<Void> completionHandler) {
    Function<AsyncResult<PreparedStatement>, CommandBase> f;
    String statement;
    if (psCache != null) {
      CachedPreparedStatement cached = psCache.get(sql);
      if (cached == null) {
        statement = "ps_" + psSeq++;
        cached = new CachedPreparedStatement();
        Future<PreparedStatement> fut = cached.fut;
        psCache.put(sql, cached);
        f = ar -> {
          fut.handle(ar);
          return supplier.apply(ar);
        };
      } else {
        cached.get(ar -> {
          CommandBase next = supplier.apply(ar);
          if (next != null) {
            schedule(next, completionHandler);
          } else {
            if (completionHandler != null) {
              completionHandler.handle(null);
            }
          }
        });
        return;
      }
    } else {
      statement = null;
      f = supplier;
    }
    schedule(new PrepareStatementCommand(sql, statement, ar -> {
      CommandBase command = f.apply(ar);
      if (command != null) {
        schedule(command, completionHandler);
      } else {
        if (completionHandler != null) {
          completionHandler.handle(null);
        }
      }
    }));
  }

  public void schedule(CommandBase cmd) {
    schedule(cmd, null);
  }

  public void schedule(CommandBase cmd, Handler<Void> completionHandler) {
    if (Vertx.currentContext() != context) {
      throw new IllegalStateException();
    }
    if (status == Status.CONNECTED) {
      pending.add(cmd);
      cmd.completionHandler = v -> {
        inflight.poll();
        if (completionHandler != null) {
          completionHandler.handle(null);
        }
        checkPending();
      };
      checkPending();
    } else {
      cmd.fail(new VertxException("Connection not open " + status));
    }
  }

  private void checkPending() {
    if (inflight.size() < pipeliningLimit) {
      CommandBase cmd;
      while (inflight.size() < pipeliningLimit && (cmd = pending.poll()) != null) {
        cork = true;
        inflight.add(cmd);
        cmd.exec(this);
        switch (outbound.size()) {
          case 0:
            break;
          case 1:
            socket.writeMessage(outbound.poll());
            break;
          default:
            OutboundMessage msg = out -> {
              OutboundMessage msg1;
              while ((msg1 = outbound.poll()) != null) {
                msg1.encode(out);
              }
            };
            socket.writeMessage(msg);
        }
        cork = false;
      }
    }
  }

  private void handleMessage(Object msg) {
    InboundMessage pgMsg = (InboundMessage) msg;
    // System.out.println("<-- " + msg);
    CommandBase cmd = inflight.peek();
    if (cmd != null) {
      cmd.handleMessage(pgMsg);
    } else {
      System.out.println("Uh oh, no inflight command for " + msg);
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
      for (ArrayDeque<CommandBase> q : Arrays.asList(inflight, pending)) {
        CommandBase cmd;
        while ((cmd = q.poll()) != null) {
          CommandBase c = cmd;
          context.runOnContext(v -> c.fail(cause));
        }
      }
      if (holder != null) {
        holder.handleClosed();
      }
    }
  }
}

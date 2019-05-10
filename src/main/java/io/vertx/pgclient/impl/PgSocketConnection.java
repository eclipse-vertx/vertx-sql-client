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

package io.vertx.pgclient.impl;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DecoderException;
import io.vertx.pgclient.impl.codec.PgCodec;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.StringLongSequence;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.InitCommand;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.NetSocketInternal;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgSocketConnection extends SocketConnectionBase {

  private final Map<String, CachedPreparedStatement> psCache;
  private final StringLongSequence psSeq = new StringLongSequence();

  private PgCodec codec;
  public int processId;
  public int secretKey;

  public PgSocketConnection(NetSocketInternal socket,
                            boolean cachePreparedStatements,
                            int pipeliningLimit,
                            Context context) {
    super(socket, pipeliningLimit, context);
    this.psCache = cachePreparedStatements ? new ConcurrentHashMap<>() : null;
  }

  @Override
  public void init() {
    codec = new PgCodec();
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "codec", codec);
    super.init();
  }

  public void sendStartupMessage(String username, String password, String database, Handler<? super CommandResponse<Connection>> completionHandler) {
    InitCommand cmd = new InitCommand(this, username, password, database);
    cmd.handler = completionHandler;
    schedule(cmd);
  }

  void sendCancelRequestMessage(int processId, int secretKey, Handler<AsyncResult<Void>> handler) {
    Buffer buffer = Buffer.buffer(16);
    buffer.appendInt(16);
    // cancel request code
    buffer.appendInt(80877102);
    buffer.appendInt(processId);
    buffer.appendInt(secretKey);

    socket.write(buffer, ar -> {
      if (ar.succeeded()) {
        // directly close this connection
        if (status == Status.CONNECTED) {
          status = Status.CLOSING;
          socket.close();
        }
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  public static class CachedPreparedStatement implements Handler<CommandResponse<PreparedStatement>> {

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

  public NetSocketInternal socket() {
    return socket;
  }

  public boolean isSsl() {
    return socket.isSsl();
  }

  public void schedule(CommandBase<?> cmd) {
    if (cmd.handler == null) {
      throw new IllegalArgumentException();
    }
    // Special handling for cache
    if (cmd instanceof PrepareStatementCommand) {
      PrepareStatementCommand psCmd = (PrepareStatementCommand) cmd;
      Map<String, PgSocketConnection.CachedPreparedStatement> psCache = this.psCache;
      if (psCache != null) {
        PgSocketConnection.CachedPreparedStatement cached = psCache.get(psCmd.sql());
        if (cached != null) {
          Handler<? super CommandResponse<PreparedStatement>> handler = psCmd.handler;
          cached.get(handler);
          return;
        } else {
          psCmd.statement = psSeq.next();
          psCmd.cached = cached = new PgSocketConnection.CachedPreparedStatement();
          psCache.put(psCmd.sql(), cached);
          Handler<? super CommandResponse<PreparedStatement>> a = psCmd.handler;
          psCmd.cached.get(a);
          psCmd.handler = psCmd.cached;
        }
      }
    }
    super.schedule(cmd);
  }

  @Override
  public int getProcessId() {
    return processId;
  }

  @Override
  public int getSecretKey() {
    return secretKey;
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

}

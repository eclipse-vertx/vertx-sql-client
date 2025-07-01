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
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.net.NetSocketInternal;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgException;
import io.vertx.pgclient.impl.codec.ExtendedQueryCommandCodec;
import io.vertx.pgclient.impl.codec.NoticeResponse;
import io.vertx.pgclient.impl.codec.PgCodec;
import io.vertx.pgclient.impl.codec.PgCommandCodec;
import io.vertx.pgclient.impl.codec.TxFailedEvent;
import io.vertx.sqlclient.codec.CommandMessage;
import io.vertx.sqlclient.codec.SocketConnectionBase;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.impl.Notification;
import io.vertx.sqlclient.internal.PreparedStatement;
import io.vertx.sqlclient.internal.QueryResultHandler;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.spi.protocol.ExtendedQueryCommand;
import io.vertx.sqlclient.spi.protocol.InitCommand;
import io.vertx.sqlclient.spi.protocol.SimpleQueryCommand;
import io.vertx.sqlclient.spi.protocol.TxCommand;

import java.util.Map;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgSocketConnection extends SocketConnectionBase {

  private PgCodec codec;
  private final boolean useLayer7Proxy;
  public int processId;
  public int secretKey;
  public PgDatabaseMetadata dbMetaData;
  private PgConnectOptions connectOptions;

  public PgSocketConnection(NetSocketInternal socket,
                            ClientMetrics metrics,
                            PgConnectOptions connectOptions,
                            boolean cachePreparedStatements,
                            int preparedStatementCacheSize,
                            Predicate<String> preparedStatementCacheSqlFilter,
                            int pipeliningLimit,
                            boolean useLayer7Proxy,
                            ContextInternal context) {
    super(socket, metrics, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
    this.connectOptions = connectOptions;
    this.useLayer7Proxy = useLayer7Proxy;
  }

  @Override
  protected PgConnectOptions connectOptions() {
    return connectOptions;
  }

  @Override
  public void init() {
    codec = new PgCodec(useLayer7Proxy);
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "codec", codec);
    super.init();
  }

  // TODO RETURN FUTURE ???
  Future<Connection> sendStartupMessage(String username, String password, String database, Map<String, String> properties) {
    InitCommand cmd = new InitCommand(this, username, password, database, properties);
    Promise<Connection> promise = context.promise();
    schedule(cmd, promise);
    return promise.future();
  }

  Future<Void> sendCancelRequestMessage(int processId, int secretKey) {
    Buffer buffer = Buffer.buffer(16);
    buffer.appendInt(16);
    // cancel request code
    buffer.appendInt(80877102);
    buffer.appendInt(processId);
    buffer.appendInt(secretKey);

    return socket.write(buffer).andThen(ar -> {
      if (ar.succeeded()) {
        // directly close this connection
        if (status == Status.CONNECTED) {
          status = Status.CLOSING;
          socket.close();
        }
      }
    });
  }

  @Override
  protected void handleMessage(Object msg) {
    super.handleMessage(msg);
    if (msg instanceof Notification || msg instanceof TxFailedEvent || msg instanceof NoticeResponse) {
      handleEvent(msg);
    }
  }

  @Override
  protected void handleException(Throwable t) {
    if (t instanceof PgException) {
      reportException(t);
    } else {
      super.handleException(t);
    }
  }

  public int getProcessId() {
    return processId;
  }

  public int getSecretKey() {
    return secretKey;
  }

  @Override
  public DatabaseMetadata databaseMetadata() {
    return dbMetaData;
  }

  void upgradeToSSLConnection(ClientSSLOptions sslOptions, Handler<AsyncResult<Void>> completionHandler) {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    Promise<Void> upgradePromise = Promise.promise();
    upgradePromise.future().onComplete(ar->{
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
    pipeline.addBefore("handler", "initiate-ssl-handler", new InitiateSslHandler(this, sslOptions, upgradePromise));
  }

  @Override
  protected <R> void doSchedule(CommandBase<R> cmd, Completable<R> handler) {
    if (cmd instanceof TxCommand) {
      TxCommand<R> tx = (TxCommand<R>) cmd;
      SimpleQueryCommand<Void> cmd2 = new SimpleQueryCommand<>(
        tx.kind().sql(),
        false,
        false,
        SocketConnectionBase.NULL_COLLECTOR,
        QueryResultHandler.NOOP_HANDLER);
      super.doSchedule(cmd2, (res, err) -> handler.complete(tx.result(), err));
    } else {
      super.doSchedule(cmd, handler);
    }
  }

  @Override
  protected CommandMessage<?, ?> toMessage(ExtendedQueryCommand<?> command, PreparedStatement preparedStatement) {
    return new ExtendedQueryCommandCodec<>((ExtendedQueryCommand<?>) command, preparedStatement);
  }

  @Override
  protected CommandMessage<?, ?> toMessage(CommandBase<?> command) {
    return PgCommandCodec.wrap(command);
  }

  @Override
  public boolean isIndeterminatePreparedStatementError(Throwable error) {
    if (error instanceof PgException) {
      String sqlState = ((PgException) error).getSqlState();
      return "42P18".equals(sqlState) || "42804".equals(sqlState) || "42P08".equals(sqlState);
    }
    return false;
  }

  @Override
  public String system() {
    return "postgresql";
  }
}

/*
 * Copyright (c) 2011-2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.ssl.SslHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.core.internal.tls.SslContextManager;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.HostAndPort;
import io.vertx.core.internal.net.NetSocketInternal;
import io.vertx.core.internal.net.SslChannelProvider;
import io.vertx.core.internal.net.SslHandshakeCompletionHandler;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLInfo;
import io.vertx.mssqlclient.impl.codec.MSSQLCommandCodec;
import io.vertx.mssqlclient.impl.codec.TdsLoginSentCompletionHandler;
import io.vertx.mssqlclient.impl.codec.TdsMessageCodec;
import io.vertx.mssqlclient.impl.codec.TdsPacketDecoder;
import io.vertx.mssqlclient.impl.codec.TdsSslHandshakeCodec;
import io.vertx.mssqlclient.impl.command.PreLoginCommand;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.CommandMessage;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.internal.QueryResultHandler;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.internal.command.*;
import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.util.Map;
import java.util.function.Predicate;

import static io.vertx.sqlclient.internal.command.TxCommand.Kind.BEGIN;

public class MSSQLSocketConnection extends SocketConnectionBase {

  private final MSSQLConnectOptions connectOptions;
  private final SslContextManager SslContextManager;

  private MSSQLDatabaseMetadata databaseMetadata;
  private HostAndPort alternateServer;

  MSSQLSocketConnection(NetSocketInternal socket,
                        SslContextManager SslContextManager,
                        ClientMetrics clientMetrics,
                        MSSQLConnectOptions connectOptions,
                        boolean cachePreparedStatements,
                        int preparedStatementCacheSize,
                        Predicate<String> preparedStatementCacheSqlFilter,
                        int pipeliningLimit,
                        ContextInternal context) {
    super(socket, clientMetrics, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
    this.connectOptions = connectOptions;
    this.SslContextManager = SslContextManager;
  }

  @Override
  protected SqlConnectOptions connectOptions() {
    return connectOptions;
  }

  Future<Byte> sendPreLoginMessage(boolean clientConfigSsl) {
    PreLoginCommand cmd = new PreLoginCommand(clientConfigSsl);
    return schedule(context, cmd).map(resp -> {
      setDatabaseMetadata(resp.metadata());
      return resp.encryptionLevel();
    });
  }

  Future<Void> enableSsl(boolean clientConfigSsl, byte encryptionLevel, MSSQLConnectOptions options) {
    // While handshaking, MS SQL requires to encapsulate SSL traffic in TDS packets
    // So it is not possible to rely on the NetSocket.upgradeToSsl method
    // Instead, we need a custom channel pipeline configuration

    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    PromiseInternal<Void> promise = context.promise();

    // 1. Install the SSL handshake completion handler
    ChannelPromise p = pipeline.newPromise();
    pipeline.addFirst("handshaker", new SslHandshakeCompletionHandler(p));
    p.addListener(future -> {
      if (future.isSuccess()) {
        // Handshaking successful, remove the codec that manages encapsulation of SSL traffic in TDS packets
        pipeline.removeFirst();
        promise.complete();
      } else {
        promise.fail(future.cause());
      }
    });

    ClientSSLOptions sslOptions = options.getSslOptions() == null ? new ClientSSLOptions() : options.getSslOptions().copy();

    // Do not perform hostname validation if the client did not require encryption
    if (!clientConfigSsl) {
      sslOptions.setTrustAll(true);
    }
    sslOptions.setHostnameVerificationAlgorithm("");

    // 2. Create and set up an SSLHelper and SSLHandler
    // options.getApplicationLayerProtocols()
    Future<SslChannelProvider> f = SslContextManager
      .resolveSslContextProvider(sslOptions, "", null, null, context)
      .map(provider -> new SslChannelProvider(context.owner(), provider, false));
    return f.compose(provider -> {
      SslHandler sslHandler = provider.createClientSslHandler(socket.remoteAddress(), null, sslOptions.isUseAlpn(), sslOptions.getSslHandshakeTimeout(), sslOptions.getSslHandshakeTimeoutUnit());

      // 3. TdsSslHandshakeCodec manages SSL payload encapsulated in TDS packets
      TdsSslHandshakeCodec tdsSslHandshakeCodec = new TdsSslHandshakeCodec();

      // 4. TdsLoginSentCompletionHandler removes the SSLHandler after login packet has been sent if full encryption is not required
      TdsLoginSentCompletionHandler tdsLoginSentCompletionHandler = new TdsLoginSentCompletionHandler(sslHandler, encryptionLevel);

      // 5. Add the handlers to the pipeline
      // The SSLHandler must be the last one added because as soon as it is, it starts handshaking
      pipeline.addFirst("tds-ssl-handshake-codec", tdsSslHandshakeCodec);
      pipeline.addAfter("tds-ssl-handshake-codec", "tds-login-sent-handler", tdsLoginSentCompletionHandler);
      pipeline.addAfter("tds-login-sent-handler", "ssl", sslHandler);

      return promise.future();
    });
  }

  Future<Connection> sendLoginMessage(String username, String password, String database, Map<String, String> properties) {
    InitCommand cmd = new InitCommand(this, username, password, database, properties);
    return schedule(context, cmd);
  }

  @Override
  public void init() {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "messageCodec", new TdsMessageCodec(connectOptions.getPacketSize()));
    pipeline.addBefore("messageCodec", "packetDecoder", new TdsPacketDecoder());
    super.init();
  }

  @Override
  protected CommandMessage<?, ?> toMessage(CommandBase<?> command) {
    return MSSQLCommandCodec.wrap(command);
  }

  @Override
  protected <R> void doSchedule(CommandBase<R> cmd, Completable<R> handler) {
    if (cmd instanceof TxCommand) {
      TxCommand<R> tx = (TxCommand<R>) cmd;
      String sql = tx.kind == BEGIN ? "BEGIN TRANSACTION":tx.kind.sql;
      SimpleQueryCommand<Void> cmd2 = new SimpleQueryCommand<>(
        sql,
        false,
        false,
        QueryCommandBase.NULL_COLLECTOR,
        QueryResultHandler.NOOP_HANDLER);
      super.doSchedule(cmd2, (res, err) -> handler.complete(tx.result, err));
    } else {
      super.doSchedule(cmd, handler);
    }
  }

  @Override
  protected void handleMessage(Object msg) {
    if (msg instanceof MSSQLInfo) {
      handleEvent(msg);
    } else {
      super.handleMessage(msg);
    }
  }

  @Override
  public String system() {
    return "mssql";
  }

  @Override
  public DatabaseMetadata getDatabaseMetaData() {
    return databaseMetadata;
  }

  private void setDatabaseMetadata(MSSQLDatabaseMetadata metadata) {
    this.databaseMetadata = metadata;
  }

  public HostAndPort getAlternateServer() {
    return alternateServer;
  }

  public void setAlternateServer(HostAndPort alternateServer) {
    this.alternateServer = alternateServer;
  }
}

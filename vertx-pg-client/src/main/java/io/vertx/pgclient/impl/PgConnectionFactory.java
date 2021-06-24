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

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.TrustOptions;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionFactoryBase;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionFactory extends SqlConnectionFactoryBase {

  private SslMode sslMode;
  private int pipeliningLimit;
  private final PgConnectOptions options;

  public PgConnectionFactory(VertxInternal context, PgConnectOptions options) {
    super(context, options);

    this.options = options; // TEMP
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions connectOptions) {
    PgConnectOptions options = (PgConnectOptions) connectOptions;
    this.pipeliningLimit = options.getPipeliningLimit();
    this.sslMode = options.isUsingDomainSocket() ? SslMode.DISABLE : options.getSslMode();

    // check ssl mode here
    switch (sslMode) {
      case VERIFY_FULL:
        String hostnameVerificationAlgorithm = options.getHostnameVerificationAlgorithm();
        if (hostnameVerificationAlgorithm == null || hostnameVerificationAlgorithm.isEmpty()) {
          throw new IllegalArgumentException("Host verification algorithm must be specified under verify-full sslmode");
        }
      case VERIFY_CA:
        TrustOptions trustOptions = options.getTrustOptions();
        if (trustOptions == null) {
          throw new IllegalArgumentException("Trust options must be specified under verify-full or verify-ca sslmode");
        }
        break;
    }
  }

  @Override
  protected void configureNetClientOptions(NetClientOptions netClientOptions) {
    netClientOptions.setSsl(false);
  }

  @Override
  protected void doConnectInternal(SocketAddress server, String username, String password, String database, Promise<Connection> promise) {
    PromiseInternal<Connection> promiseInternal = (PromiseInternal<Connection>) promise;
    doConnect(server, SqlConnectionFactoryBase.asEventLoopContext(promiseInternal.context())).flatMap(conn -> {
      PgSocketConnection socket = (PgSocketConnection) conn;
      socket.init();
      return Future.<Connection>future(p -> socket.sendStartupMessage(username, password, database, properties, p))
        .map(conn);
    }).onComplete(promise);
  }

  public void cancelRequest(SocketAddress server, int processId, int secretKey, Handler<AsyncResult<Void>> handler) {
    doConnect(server, vertx.createEventLoopContext()).onComplete(ar -> {
      if (ar.succeeded()) {
        PgSocketConnection conn = (PgSocketConnection) ar.result();
        conn.sendCancelRequestMessage(processId, secretKey, handler);
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  private Future<Connection> doConnect(SocketAddress server, EventLoopContext context) {
    Future<Connection> connFuture;
    switch (sslMode) {
      case DISABLE:
        connFuture = doConnect(server, context,false);
        break;
      case ALLOW:
        connFuture = doConnect(server, context,false).recover(err -> doConnect(server, context,true));
        break;
      case PREFER:
        connFuture = doConnect(server, context,true).recover(err -> doConnect(server, context,false));
        break;
      case REQUIRE:
      case VERIFY_CA:
      case VERIFY_FULL:
        connFuture = doConnect(server, context, true);
        break;
      default:
        return context.failedFuture(new IllegalArgumentException("Unsupported SSL mode"));
    }
    return connFuture;
  }

  private Future<Connection> doConnect(SocketAddress server, EventLoopContext context, boolean ssl) {
    Future<NetSocket> soFut;
    try {
      soFut = netClient.connect(server, (String) null);
    } catch (Exception e) {
      // Client is closed
      return context.failedFuture(e);
    }
    Future<Connection> connFut = soFut.map(so -> newSocketConnection(context, (NetSocketInternal) so));
    if (ssl && !server.isDomainSocket()) {
      // upgrade connection to SSL if needed
      connFut = connFut.flatMap(conn -> Future.future(p -> {
        PgSocketConnection socket = (PgSocketConnection) conn;
        socket.upgradeToSSLConnection(ar2 -> {
          if (ar2.succeeded()) {
            p.complete(conn);
          } else {
            p.fail(ar2.cause());
          }
        });
      }));
    }
    return connFut;
  }

  @Override
  public Future<SqlConnection> connect(Context context) {
    ContextInternal contextInternal = (ContextInternal) context;
    PromiseInternal<Connection> promise = contextInternal.promise();
    connect(promise);
    return promise.future()
      .map(conn -> {
        QueryTracer tracer = contextInternal.tracer() == null ? null : new QueryTracer(contextInternal.tracer(), options);
        PgConnectionImpl pgConn = new PgConnectionImpl(this, contextInternal, conn, tracer, null);
        conn.init(pgConn);
        return pgConn;
      });
  }

  private PgSocketConnection newSocketConnection(EventLoopContext context, NetSocketInternal socket) {
    return new PgSocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
  }
}

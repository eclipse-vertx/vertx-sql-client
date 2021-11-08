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
import io.vertx.sqlclient.ServerType;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

import java.util.Collections;
import java.util.stream.Stream;

import static io.vertx.sqlclient.ServerType.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionFactory extends ConnectionFactoryBase {

  private SslMode sslMode;
  private int pipeliningLimit;
  private boolean shouldQueryServerType;
  private ServerType serverType = UNDEFINED;

  public PgConnectionFactory(VertxInternal context, PgConnectOptions options) {
    super(context, options);
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions connectOptions) {
    PgConnectOptions options = (PgConnectOptions) connectOptions;
    this.pipeliningLimit = options.getPipeliningLimit();
    this.sslMode = options.isUsingDomainSocket() ? SslMode.DISABLE : options.getSslMode();
    this.shouldQueryServerType = options.getShouldQueryServerType();

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
  protected Future<Connection> doConnectInternal(SocketAddress server, String username, String password, String database, EventLoopContext context) {
    return doConnect(server, context).flatMap(conn -> {
      PgSocketConnection socket = (PgSocketConnection) conn;
      socket.init();
      return Future.<Connection>future(p -> socket.sendStartupMessage(username, password, database, properties, p))
        .map(conn);
    });
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
    PromiseInternal<SqlConnection> promise = contextInternal.promise();
    connect(asEventLoopContext(contextInternal))
      .flatMap(conn -> {
        QueryTracer tracer = contextInternal.tracer() == null ? null : new QueryTracer(contextInternal.tracer(), options);
        PgConnectionImpl pgConn = new PgConnectionImpl(this, contextInternal, conn, tracer, null);
        conn.init(pgConn);
        serverType = ((PgSocketConnection) conn).serverType;
        if (serverType == UNDEFINED && shouldQueryServerType) {
          String paramStatus = conn.getDatabaseMetaData().majorVersion() >= 14 ? "in_hot_standby" : "transaction_read_only";
          return pgConn.query(String.format("SHOW %s;", paramStatus)).execute().map(pgRowSet -> {
            pgRowSet.forEach(row ->
              serverType = "off".equalsIgnoreCase(row.getString(paramStatus)) ? PRIMARY : REPLICA
            );
            return pgConn;
          });
        } else {
          return Future.succeededFuture((SqlConnection) pgConn);
        }
      })
      .onComplete(promise);
    return promise.future();
  }

  @Override
  public ServerType getServerType() {
    return serverType;
  }

  private PgSocketConnection newSocketConnection(EventLoopContext context, NetSocketInternal socket) {
    return new PgSocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
  }
}

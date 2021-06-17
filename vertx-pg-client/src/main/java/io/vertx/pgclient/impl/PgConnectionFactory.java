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
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
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
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.SqlConnectionFactoryBase;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PgConnectionFactory extends SqlConnectionFactoryBase implements ConnectionFactory {

  private SslMode sslMode;
  private int pipeliningLimit;

  PgConnectionFactory(VertxInternal context, PgConnectOptions options) {
    super(context, options);
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
  protected void doConnectInternal(Promise<Connection> promise, SocketAddress socketAddress) {
    PromiseInternal<Connection> promiseInternal = (PromiseInternal<Connection>) promise;
    doConnect(ConnectionFactory.asEventLoopContext(promiseInternal.context()), socketAddress).flatMap(conn -> {
      PgSocketConnection socket = (PgSocketConnection) conn;
      socket.init();
      return Future.<Connection>future(p -> socket.sendStartupMessage(username, password, database, properties, p))
        .map(conn);
    }).onComplete(promise);
  }

  public void cancelRequest(int processId, int secretKey, Handler<AsyncResult<Void>> handler) {
    // TODO: find proper host
    doConnect(vertx.createEventLoopContext(), socketAddresses.get(0)).onComplete(ar -> {
      if (ar.succeeded()) {
        PgSocketConnection conn = (PgSocketConnection) ar.result();
        conn.sendCancelRequestMessage(processId, secretKey, handler);
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  private Future<Connection> doConnect(EventLoopContext context, SocketAddress socketAddress) {
    Future<Connection> connFuture;
    switch (sslMode) {
      case DISABLE:
        connFuture = doConnect(context,false, socketAddress);
        break;
      case ALLOW:
        connFuture = doConnect(context,false, socketAddress).recover(err -> doConnect(context,true, socketAddress));
        break;
      case PREFER:
        connFuture = doConnect(context,true, socketAddress).recover(err -> doConnect(context,false, socketAddress));
        break;
      case REQUIRE:
      case VERIFY_CA:
      case VERIFY_FULL:
        connFuture = doConnect(context, true, socketAddress);
        break;
      default:
        return context.failedFuture(new IllegalArgumentException("Unsupported SSL mode"));
    }
    return connFuture;
  }

  private Future<Connection> doConnect(EventLoopContext context, boolean ssl, SocketAddress address) {
    Future<NetSocket> soFut;
    try {
      soFut = netClient.connect(address, (String) null);
    } catch (Exception e) {
      // Client is closed, it is meaningless to retry other hosts
      // maybe consider own exception class to filter it in Future<Connection>::recover?
      return context.failedFuture(e);
    }
    Future<Connection> connFut = soFut.map(so -> newSocketConnection(context, (NetSocketInternal) so));
    if (ssl && !address.isDomainSocket()) {
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

  private PgSocketConnection newSocketConnection(EventLoopContext context, NetSocketInternal socket) {
    return new PgSocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
  }
}

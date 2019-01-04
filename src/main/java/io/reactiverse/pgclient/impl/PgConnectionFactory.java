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

import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.SslMode;
import io.vertx.core.*;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.net.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionFactory {

  private final NetClient client;
  private final Context ctx;
  private final boolean registerCloseHook;
  private final String host;
  private final int port;
  private final SslMode sslMode;
  private final TrustOptions trustOptions;
  private final String hostnameVerificationAlgorithm;
  private final String database;
  private final String username;
  private final String password;
  private final boolean cachePreparedStatements;
  private final int pipeliningLimit;
  private final boolean isUsingDomainSocket;
  private final Closeable hook;

  public PgConnectionFactory(Context context,
                             boolean registerCloseHook,
                             PgConnectOptions options) {

    hook = this::close;
    this.registerCloseHook = registerCloseHook;

    ctx = context;
    if (registerCloseHook) {
      ctx.addCloseHook(hook);
    }

    NetClientOptions netClientOptions = new NetClientOptions(options);

    // Make sure ssl=false as we will use STARTLS
    netClientOptions.setSsl(false);

    this.sslMode = options.getSslMode();
    this.hostnameVerificationAlgorithm = netClientOptions.getHostnameVerificationAlgorithm();
    this.trustOptions = netClientOptions.getTrustOptions();
    this.host = options.getHost();
    this.port = options.getPort();
    this.database = options.getDatabase();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.cachePreparedStatements = options.getCachePreparedStatements();
    this.pipeliningLimit = options.getPipeliningLimit();
    this.isUsingDomainSocket = options.isUsingDomainSocket();

    this.client = context.owner().createNetClient(netClientOptions);
  }

  // Called by hook
  private void close(Handler<AsyncResult<Void>> completionHandler) {
    client.close();
    completionHandler.handle(Future.succeededFuture());
  }

  public void close() {
    if (registerCloseHook) {
      ctx.removeCloseHook(hook);
    }
    client.close();
  }

  public void create(Handler<? super CommandResponse<Connection>> completionHandler) {
    connect(ar -> {
      if (ar.succeeded()) {
        SocketConnection conn = ar.result();
        conn.initializeCodec();
        conn.sendStartupMessage(username, password, database, completionHandler);
      } else {
        completionHandler.handle(CommandResponse.failure(ar.cause()));
      }
    });
  }

  public void connect(Handler<AsyncResult<SocketConnection>> handler) {
    switch (sslMode) {
      case DISABLE:
        doConnect(false, handler);
        break;
      case ALLOW:
        doConnect(false, ar -> {
          if (ar.succeeded()) {
            handler.handle(Future.succeededFuture(ar.result()));
          } else {
            doConnect(true, handler);
          }
        });
        break;
      case PREFER:
        doConnect(true, ar -> {
          if (ar.succeeded()) {
            handler.handle(Future.succeededFuture(ar.result()));
          } else {
            doConnect(false, handler);
          }
        });
        break;
      case VERIFY_FULL:
        if (hostnameVerificationAlgorithm == null || hostnameVerificationAlgorithm.isEmpty()) {
          handler.handle(Future.failedFuture(new IllegalArgumentException("Host verification algorithm must be specified under verify-full sslmode")));
          return;
        }
      case VERIFY_CA:
        if (trustOptions == null) {
          handler.handle(Future.failedFuture(new IllegalArgumentException("Trust options must be specified under verify-full or verify-ca sslmode")));
          return;
        }
      case REQUIRE:
        doConnect(true, handler);
        break;
      default:
        throw new IllegalArgumentException("Unsupported SSL mode");
    }
  }

  private void doConnect(boolean ssl, Handler<AsyncResult<SocketConnection>> handler) {
    if (Vertx.currentContext() != ctx) {
      throw new IllegalStateException();
    }
    SocketAddress socketAddress;
    if (!isUsingDomainSocket) {
      socketAddress = SocketAddress.inetSocketAddress(port, host);
    } else {
      socketAddress = SocketAddress.domainSocketAddress(host + "/.s.PGSQL." + port);
    }

    Future<NetSocket> future = Future.<NetSocket>future().setHandler(ar -> {
      if (ar.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar.result();
        SocketConnection conn = newSocketConnection(socket);

        if (ssl && !isUsingDomainSocket) {
          // upgrade connection to SSL if needed
          conn.upgradeToSSLConnection(ar2 -> {
            if (ar2.succeeded()) {
              handler.handle(Future.succeededFuture(conn));
            } else {
              handler.handle(Future.failedFuture(ar2.cause()));
            }
          });
        } else {
          handler.handle(Future.succeededFuture(conn));
        }
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });

    try {
      client.connect(socketAddress, null, future);
    } catch (Exception e) {
      // Client is closed
      future.fail(e);
    }
  }

  private SocketConnection newSocketConnection(NetSocketInternal socket) {
    return new SocketConnection(socket, cachePreparedStatements, pipeliningLimit, ctx);
  }
}

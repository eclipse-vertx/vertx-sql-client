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
import io.vertx.core.*;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionFactory {

  private final NetClient client;
  private final Context ctx;
  private final boolean registerCloseHook;
  private final String host;
  private final int port;
  private final boolean ssl;
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

    this.ssl = options.isSsl();
    this.host = options.getHost();
    this.port = options.getPort();
    this.database = options.getDatabase();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.client = context.owner().createNetClient(netClientOptions);
    this.cachePreparedStatements = options.getCachePreparedStatements();
    this.pipeliningLimit = options.getPipeliningLimit();
    this.isUsingDomainSocket = options.isUsingDomainSocket();
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

  public void connect(Handler<? super CommandResponse<Connection>> completionHandler) {
    if (Vertx.currentContext() != ctx) {
      throw new IllegalStateException();
    }
    SocketAddress socketAddress;
    if (!isUsingDomainSocket) {
      socketAddress = SocketAddress.inetSocketAddress(port, host);
    } else {
      socketAddress = SocketAddress.domainSocketAddress(host + "/.s.PGSQL." + port);
    }
    Future<NetSocket> fut = Future.<NetSocket>future().setHandler(ar -> {
      if (ar.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar.result();
        SocketConnection conn = new SocketConnection(
          socket,
          cachePreparedStatements,
          pipeliningLimit,
          ssl,
          ctx);
        conn.initiateProtocolOrSsl(username, password, database, completionHandler);
      } else {
        completionHandler.handle(CommandResponse.failure(ar.cause()));
      }
    });
    try {
      client.connect(socketAddress, null, fut);
    } catch (Exception e) {
      // Client is closed
      fut.fail(e);
    }
  }
}

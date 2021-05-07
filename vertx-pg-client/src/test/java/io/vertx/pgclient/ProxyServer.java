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

package io.vertx.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

import java.util.function.Function;

/**
 * A proxy server, useful for changing some server behavior
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class ProxyServer {

  static ProxyServer create(Vertx vertx, int pgPort, String pgHost) {
    return new ProxyServer(vertx, pgPort, pgHost);
  }

  static class Connection {

    private final NetSocket clientSocket;
    private final NetSocket serverSocket;
    private Handler<Buffer> clientHandler;
    private Handler<Buffer> serverHandler;
    private Handler<Void> clientCloseHandler;
    private Handler<Void> serverCloseHandler;

    public Connection(NetSocket clientSo, NetSocket serverSo) {
      this.clientSocket = clientSo;
      this.serverSocket = serverSo;
      this.clientHandler = serverSocket::write;
      this.serverHandler = clientSocket::write;
    }

    NetSocket clientSocket() {
      return clientSocket;
    }

    NetSocket serverSocket() {
      return serverSocket;
    }

    Connection clientHandler(Handler<Buffer> handler) {
      clientHandler = handler;
      return this;
    }

    Connection serverHandler(Handler<Buffer> handler) {
      serverHandler = handler;
      return this;
    }

    void connect() {
      clientSocket.handler(clientHandler);
      serverSocket.handler(serverHandler);
      clientSocket.closeHandler(v -> {
        serverSocket.close();
        if (clientCloseHandler != null) {
          clientCloseHandler.handle(null);
        }
      });
      serverSocket.closeHandler(v -> {
        clientSocket.close();
        if (serverCloseHandler != null) {
          serverCloseHandler.handle(null);
        }
      });
      serverSocket.resume();
      clientSocket.resume();
    }

    Connection clientCloseHandler(Handler<Void> handler) {
      clientCloseHandler = handler;
      return this;
    }

    Connection serverCloseHandler(Handler<Void> handler) {
      serverCloseHandler = handler;
      return this;
    }

    void close() {
      clientSocket.close();
      serverSocket.close();
    }
  }

  private final Vertx vertx;
  private final NetServer server;
  private final NetClient client;
  private final int pgPort;
  private final String pgHost;
  private Handler<Connection> proxyHandler;

  private ProxyServer(Vertx vertx, int pgPort, String pgHost) {
    this.pgPort = pgPort;
    this.pgHost = pgHost;
    this.vertx = vertx;
    this.client = vertx.createNetClient();
    this.server = vertx.createNetServer().connectHandler(this::handle);
    this.proxyHandler = Connection::connect;
  }

  public ProxyServer proxyHandler(Handler<Connection> proxyHandler) {
    this.proxyHandler = proxyHandler;
    return this;
  }

  public void listen(int port, String host, Handler<AsyncResult<Void>> completionHandler) {
    server.listen(port, host, ar -> completionHandler.handle(ar.mapEmpty()));
  }

  private void handle(NetSocket clientSocket) {
    clientSocket.pause();
    client.connect(pgPort, pgHost, ar -> {
      if (ar.succeeded()) {
        NetSocket serverSocket = ar.result();
        serverSocket.pause();
        Connection conn = new Connection(clientSocket, serverSocket);
        proxyHandler.handle(conn);
      } else {
        clientSocket.close();
      }
    });
  }
}

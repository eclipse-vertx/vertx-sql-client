package com.julienviet.pgclient;

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
    private Function<Buffer, Buffer> serverSocketFilter = Function.identity();
    private Function<Buffer, Buffer> clientSocketFilter = Function.identity();

    public Connection(NetSocket clientSo, NetSocket serverSo) {
      this.clientSocket = clientSo;
      this.serverSocket = serverSo;
    }

    NetSocket clientSocket() {
      return clientSocket;
    }

    NetSocket serverSocket() {
      return serverSocket;
    }

    Connection serverSocketFilter(Function<Buffer, Buffer> filter) {
      serverSocketFilter = filter;
      return this;
    }

    Connection clientSocketFilter(Function<Buffer, Buffer> filter) {
      clientSocketFilter = filter;
      return this;
    }

    void connect() {
      clientSocket.handler(buff -> serverSocket.write(serverSocketFilter.apply(buff)));
      serverSocket.handler(buff -> clientSocket.write(clientSocketFilter.apply(buff)));
      clientSocket.closeHandler(v -> serverSocket.close());
      serverSocket.closeHandler(v -> clientSocket.close());
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
      clientSocket.resume();
      if (ar.succeeded()) {
        NetSocket serverSocket = ar.result();
        Connection conn = new Connection(clientSocket, serverSocket);
        proxyHandler.handle(conn);
      } else {
        clientSocket.close();
      }
    });
  }
}

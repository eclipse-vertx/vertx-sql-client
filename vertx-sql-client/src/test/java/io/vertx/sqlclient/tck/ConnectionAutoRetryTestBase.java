/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.tck;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(VertxUnitRunner.class)
public abstract class ConnectionAutoRetryTestBase {

  protected Vertx vertx;
  protected Connector<SqlConnection> connectionConnector;
  protected Connector<SqlConnection> poolConnector;

  protected SqlConnectOptions options;

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  protected abstract void initialConnector(int... proxyPorts);

  @Test
  public void testConnSuccessWithoutRetry(TestContext ctx) {
    options.setReconnectAttempts(3);
    options.setReconnectInterval(1000);
    UnstableProxyServer unstableProxyServer = new UnstableProxyServer(0);
    unstableProxyServer.initialize(options, ctx.asyncAssertSuccess(v -> {
      initialConnector(unstableProxyServer.port());
      connectionConnector.connect(ctx.asyncAssertSuccess(conn -> {
        conn.close();
      }));
    }));
  }


  @Test
  public void testPoolSuccessWithoutRetry(TestContext ctx) {
    options.setReconnectAttempts(3);
    options.setReconnectInterval(1000);
    UnstableProxyServer unstableProxyServer = new UnstableProxyServer(0);
    unstableProxyServer.initialize(options, ctx.asyncAssertSuccess(v -> {
      initialConnector(unstableProxyServer.port());
      poolConnector.connect(ctx.asyncAssertSuccess(conn -> {
        conn.close();
      }));
    }));
  }

  @Test
  public void testConnExceedingRetryLimit(TestContext ctx) {
    options.setReconnectAttempts(1);
    options.setReconnectInterval(1000);
    UnstableProxyServer unstableProxyServer = new UnstableProxyServer(2);
    unstableProxyServer.initialize(options, ctx.asyncAssertSuccess(v -> {
      initialConnector(unstableProxyServer.port());
      connectionConnector.connect(ctx.asyncAssertFailure(throwable -> {
      }));
    }));
  }

  @Test
  public void testPoolExceedingRetryLimit(TestContext ctx) {
    options.setReconnectAttempts(1);
    options.setReconnectInterval(1000);
    UnstableProxyServer unstableProxyServer = new UnstableProxyServer(2);
    unstableProxyServer.initialize(options, ctx.asyncAssertSuccess(v -> {
      initialConnector(unstableProxyServer.port());
      poolConnector.connect(ctx.asyncAssertFailure(throwable -> {
      }));
    }));
  }

  @Test
  public void testConnRetrySuccess(TestContext ctx) {
    options.setReconnectAttempts(1);
    options.setReconnectInterval(1000);
    UnstableProxyServer unstableProxyServer = new UnstableProxyServer(1);
    unstableProxyServer.initialize(options, ctx.asyncAssertSuccess(v -> {
      initialConnector(unstableProxyServer.port());
      connectionConnector.connect(ctx.asyncAssertSuccess(connection -> {
        connection.close();
      }));
    }));
  }

  @Test
  public void testPoolRetrySuccess(TestContext ctx) {
    options.setReconnectAttempts(1);
    options.setReconnectInterval(1000);
    UnstableProxyServer unstableProxyServer = new UnstableProxyServer(1);
    unstableProxyServer.initialize(options, ctx.asyncAssertSuccess(v -> {
      initialConnector(unstableProxyServer.port());
      poolConnector.connect(ctx.asyncAssertSuccess(conn -> {
        conn.close();
      }));
    }));
  }

  @Test
  public void testConnMultipleHostsRetrySuccess(TestContext ctx) {
    options.setReconnectAttempts(0);
    options.setReconnectInterval(1);
    UnstableProxyServer unstableProxyServer1 = new UnstableProxyServer(1);
    UnstableProxyServer unstableProxyServer2 = new UnstableProxyServer(0);
    unstableProxyServer1.initialize(options, ctx.asyncAssertSuccess(v1 -> {
      unstableProxyServer2.initialize(options, ctx.asyncAssertSuccess(v2 -> {
        initialConnector(unstableProxyServer1.port(), unstableProxyServer2.port());
        connectionConnector.connect(ctx.asyncAssertSuccess(conn -> {
          // how to verify that we connected through second proxy?
          conn.close();
        }));
      }));
    }));
  }

  @Test
  public void testPoolMultipleHostsRetrySuccess(TestContext ctx) {
    options.setReconnectAttempts(0);
    options.setReconnectInterval(1);
    UnstableProxyServer unstableProxyServer1 = new UnstableProxyServer(1);
    UnstableProxyServer unstableProxyServer2 = new UnstableProxyServer(0);
    unstableProxyServer1.initialize(options, ctx.asyncAssertSuccess(v1 -> {
      unstableProxyServer2.initialize(options, ctx.asyncAssertSuccess(v2 -> {
        initialConnector(unstableProxyServer1.port(), unstableProxyServer2.port());
        poolConnector.connect(ctx.asyncAssertSuccess(conn -> {
          // how to verify that we connected through second proxy?
          conn.close();
        }));
      }));
    }));
  }

  public class UnstableProxyServer {

    private final Logger LOGGER = LoggerFactory.getLogger(UnstableProxyServer.class);

    private NetServer netServer;
    private NetClient netClient;
    private int retryTimes;

    private AtomicInteger counter;

    private Map<NetSocket, Queue<Buffer>> bufferedOutboundFrontendRequest = new HashMap<>();
    private Map<NetSocket, NetSocket> frontendSocketToBackendSocket = new HashMap<>();

    public UnstableProxyServer(int retryTimes) {
      this.retryTimes = retryTimes;
      this.counter = new AtomicInteger(retryTimes);
    }

    public void initialize(SqlConnectOptions targetOptions, Handler<AsyncResult<Void>> resultHandler) {
      this.netClient = vertx.createNetClient();
      this.netServer = vertx.createNetServer()
        .connectHandler(frontendSocket -> {
          LOGGER.info("Proxy: frontend socket connected");
          frontendSocket.handler(outbound -> {
            NetSocket backendSocket = frontendSocketToBackendSocket.get(frontendSocket);
            if (backendSocket == null) {
              // might not connected yet, buffer the request first
              bufferFrontendRequest(frontendSocket, outbound);
            } else {
              // push the buffered outbound bytes first
              sendBufferedFrontendRequest(frontendSocket, backendSocket);
              backendSocket.write(outbound);
            }
          });
          if (counter.getAndDecrement() > 0) {
            // close the connection directly to let the client retry
            LOGGER.info("Proxy: frontend socket closed by proxy");
            frontendSocket.close();
          } else {
            // pipe the stream to the database otherwise
            netClient.connect(targetOptions.getPort(), targetOptions.getHost())
              .onSuccess(backendSocket -> {
                LOGGER.info("Proxy: backend socket connected");
                frontendSocketToBackendSocket.put(frontendSocket, backendSocket);

                backendSocket.handler(in -> {
                  frontendSocket.write(in);
                });

                // send the buffered request once connected
                sendBufferedFrontendRequest(frontendSocket, backendSocket);
              })
              .onFailure(t -> {
                LOGGER.error("Proxy: backend socket connect failure");
                netClient.close();
              });
          }
        });

      this.netServer.listen()
        .onComplete(ar -> {
          if (ar.succeeded()) {
            resultHandler.handle(Future.succeededFuture());
          } else {
            resultHandler.handle(Future.failedFuture(ar.cause()));
          }
        });
    }

    public int port() {
      return netServer.actualPort();
    }

    public int token() {
      return counter.get();
    }

    public void reset() {
      counter.set(retryTimes);
    }

    private void bufferFrontendRequest(NetSocket frontendSocket, Buffer request) {
      // proxy socket not connected, buffer the request
      Queue<Buffer> bufferQueue = bufferedOutboundFrontendRequest.get(frontendSocket);
      if (bufferQueue == null) {
        bufferQueue = new ArrayDeque<>();
        bufferQueue.add(request);
        bufferedOutboundFrontendRequest.put(frontendSocket, bufferQueue);
      } else {
        bufferQueue.add(request);
      }
    }

    private void sendBufferedFrontendRequest(NetSocket frontendSocket, NetSocket backendSocket) {
      Queue<Buffer> bufferQueue = bufferedOutboundFrontendRequest.get(frontendSocket);
      if (bufferQueue != null) {
        Buffer bufferedOutbound;
        while ((bufferedOutbound = bufferQueue.poll()) != null) {
          frontendSocketToBackendSocket.get(frontendSocket).write(bufferedOutbound);
        }
      }
    }
  }
}

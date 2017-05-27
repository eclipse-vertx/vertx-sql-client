package com.julienviet.pgclient.impl;


import com.julienviet.pgclient.PostgresConnection;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.encoder.message.Terminate;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.metrics.impl.DummyVertxMetrics;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.spi.metrics.NetworkMetrics;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DbConnection extends ConnectionBase {

  enum Status {

    CLOSED, CONNECTED, CLOSING

  }

  private final ArrayDeque<CommandBase> inflight = new ArrayDeque<>();
  private final ArrayDeque<CommandBase> pending = new ArrayDeque<>();
  final PostgresClientImpl client;
  private Status status = Status.CONNECTED;

  public DbConnection(PostgresClientImpl client, VertxInternal vertx, Channel channel, ContextImpl context) {
    super(vertx, channel, context);

    this.client = client;
  }

  final PostgresConnection conn = new PostgresConnectionImpl(this);

  void init(String username, String password, String database, Handler<AsyncResult<DbConnection>> completionHandler) {
    schedule(new StartupCommand(username, password, database, completionHandler));
  }

  void doClose() {
    if (Vertx.currentContext() == context) {
      if (status == Status.CONNECTED) {
        status = Status.CLOSING;
        writeToChannel(Terminate.INSTANCE);
      }
    } else {
      context.runOnContext(v -> doClose());
    }
  }

  void schedule(CommandBase cmd) {
    if (Vertx.currentContext() == context) {
      if (status == Status.CONNECTED) {
        pending.add(cmd);
        checkPending();
      } else {
        cmd.fail(new VertxException("Connection not open " + status));
      }
    } else {
      context.runOnContext(v -> schedule(cmd));
    }
  }

  private void checkPending() {
    CommandBase cmd;
    while (inflight.size() < client.pipeliningLimit && (cmd = pending.poll()) != null) {
      if (cmd.exec(this)) {
        inflight.add(cmd);
      }
    }
  }

  void handleMessage(Message msg) {
    CommandBase cmd = inflight.peek();
    if (cmd != null) {
      if (cmd.handleMessage(msg)) {
        inflight.poll();
        checkPending();
      }
    } else {
      System.out.println("Uh oh, no inflight command for " + msg);
    }
  }

  @Override
  protected void handleClosed() {
    status = Status.CLOSED;
    for (ArrayDeque<CommandBase> q : Arrays.asList(inflight, pending)) {
      CommandBase cmd;
      while ((cmd = q.poll()) != null) {
        CommandBase c = cmd;
        context.runOnContext(v -> c.fail(new VertxException("closed")));
      }
    }
    super.handleClosed();
  }

  @Override
  protected synchronized void handleException(Throwable t) {
    super.handleException(t);
    close();
  }

  @Override
  public NetworkMetrics metrics() {
    return new DummyVertxMetrics.DummyDatagramMetrics();
  }

  @Override
  protected void handleInterestedOpsChanged() {

  }

/*
  @Override
  public synchronized ChannelFuture writeToChannel(Object obj) {
    System.out.println("Sending " + obj);
    return super.writeToChannel(obj);
  }
*/
}

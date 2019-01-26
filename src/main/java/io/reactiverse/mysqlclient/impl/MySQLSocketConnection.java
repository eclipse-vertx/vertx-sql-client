package io.reactiverse.mysqlclient.impl;

import io.netty.channel.ChannelPipeline;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandBase;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandResponse;
import io.reactiverse.mysqlclient.impl.codec.MySQLPacketEncoder;
import io.reactiverse.mysqlclient.impl.codec.decoder.InitialHandshakeHandler;
import io.reactiverse.mysqlclient.impl.codec.decoder.MySQLCommandHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.NetSocketInternal;

import java.nio.charset.Charset;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class MySQLSocketConnection {
  private final NetSocketInternal socket;
  private final Context context;
  private final Charset charset;

  private State connectionState = State.INIT;

  // non-blocking unbounded deque
  private Deque<MySQLCommandBase<?>> pending = new ConcurrentLinkedDeque<>();
  private ExecutingCommand executingCmd = new ExecutingCommand(null);

  private MySQLPacketEncoder encoder;
  private MySQLCommandHandler commandHandler;
  private AtomicInteger sequenceIdCounter;

  public MySQLSocketConnection(NetSocketInternal socket, Charset charset, Context context) {
    this.socket = socket;
    this.context = context;
    this.charset = charset;

    sequenceIdCounter = new AtomicInteger();
    encoder = new MySQLPacketEncoder(charset, sequenceIdCounter, socket.channelHandlerContext());
    commandHandler = new MySQLCommandHandler(charset, this);

    //TODO socket exception handling
//    socket.closeHandler();
    socket.exceptionHandler(error-> {
      error.printStackTrace();
    });

    socket.messageHandler(message -> {
      // handle response
      handleMessage(message);
    });
  }

  public void initProtocol(String username, String password, String database, boolean ssl, Handler<AsyncResult<MySQLSocketConnection>> handler) {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    Future<Void> future = Future.future();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture(this));
        switchToState(MySQLSocketConnection.State.COMMANDING);
        pipeline.remove("authenticationHandler");
        pipeline.addBefore("handler", "commandHandler", commandHandler);
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
    InitialHandshakeHandler initialHandshakeHandler = new InitialHandshakeHandler(charset, this, username, password, database, ssl, future);
    pipeline.addBefore("handler", "handshakeHandler", initialHandshakeHandler);
    switchToState(State.CONNECTING);
  }

  public void schedule(MySQLCommandBase<?> command) {
    resetSequenceId();
    pending.add(command);
    checkPending();
  }

  public ExecutingCommand getExecutingCmd() {
    return this.executingCmd;
  }

  public void resetSequenceId() {
    sequenceIdCounter.getAndSet(0);
  }

  public void modifySequenceId(int currentSequenceId, int newSequenceId) {
    sequenceIdCounter.compareAndSet(currentSequenceId, newSequenceId);
  }

  public void upgradeToSsl(Handler<AsyncResult<Void>> completionHandler) {
    throw new UnsupportedOperationException();
  }

  public MySQLPacketEncoder packetEncoder() {
    return this.encoder;
  }

  public State currentState() {
    return this.connectionState;
  }

  public void switchToState(State state) {
    this.connectionState = state;
  }

  private void handleMessage(Object msg) {
    if (msg instanceof MySQLCommandResponse) {
      MySQLCommandBase cmd = executingCmd.getCmd();
      executingCmd.setCmd(null);
      checkPending();
      cmd.getHandler().handle(msg);
    }
  }

  private void checkPending() {
    MySQLCommandBase cmd;
    if (executingCmd.getCmd() == null && (cmd = pending.poll()) != null) {
      executingCmd.setCmd(cmd);
      // we do a flush for every command
      cmd.exec(encoder);
    }
  }

  /*
    State machine to know which phase we are in and which kind of command we're handling.
   */
  public enum State {
    INIT, CONNECTING, AUTHENTICATING, COMMANDING, CLOSING, CLOSED
  }

  public static final class ExecutingCommand {
    private MySQLCommandBase<?> cmd;

    public ExecutingCommand(MySQLCommandBase<?> cmd) {
      this.cmd = cmd;
    }

    public MySQLCommandBase<?> getCmd() {
      return cmd;
    }

    public void setCmd(MySQLCommandBase<?> cmd) {
      this.cmd = cmd;
    }
  }
}

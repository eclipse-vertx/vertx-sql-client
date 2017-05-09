package io.vertx.pgclient.impl;

import io.netty.channel.Channel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.metrics.impl.DummyVertxMetrics;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.spi.metrics.NetworkMetrics;
import io.vertx.pgclient.PostgresConnection;
import io.vertx.pgclient.Result;
import io.vertx.pgclient.Row;
import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.decoder.Column;
import io.vertx.pgclient.codec.decoder.ColumnType;
import io.vertx.pgclient.codec.decoder.TransactionStatus;
import io.vertx.pgclient.codec.decoder.message.AuthenticationClearTextPasswordMessage;
import io.vertx.pgclient.codec.decoder.message.AuthenticationMD5PasswordMessage;
import io.vertx.pgclient.codec.decoder.message.AuthenticationOkMessage;
import io.vertx.pgclient.codec.decoder.message.CommandCompleteMessage;
import io.vertx.pgclient.codec.decoder.message.DataRowMessage;
import io.vertx.pgclient.codec.decoder.message.ErrorResponseMessage;
import io.vertx.pgclient.codec.decoder.message.ReadyForQueryMessage;
import io.vertx.pgclient.codec.decoder.message.RowDescriptionMessage;
import io.vertx.pgclient.codec.encoder.message.PasswordMessage;
import io.vertx.pgclient.codec.encoder.message.QueryMessage;
import io.vertx.pgclient.codec.encoder.message.TerminateMessage;

import java.util.ArrayDeque;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DbConnection extends ConnectionBase {

  enum Status {

    CLOSED, CONNECTED, CLOSING

  }

  private final ArrayDeque<Command> inflight = new ArrayDeque<>();
  private final ArrayDeque<Command> pending = new ArrayDeque<>();
  final PostgresClientImpl client;
  Handler<AsyncResult<PostgresConnection>> handler;
  private RowDescriptionMessage rowDesc;
  private Result result;
  private Status status = Status.CONNECTED;

  public DbConnection(PostgresClientImpl client, VertxInternal vertx, Channel channel, ContextImpl context) {
    super(vertx, channel, context);

    this.client = client;
  }

  final PostgresConnection conn = new PostgresConnection() {

    public void execute(String sql, Handler<AsyncResult<Result>> handler) {
      Command cmd = new Command(sql, handler);
      if (Vertx.currentContext() == context) {
        doExecute(cmd);
      } else {
        context.runOnContext(v -> doExecute(cmd));
      }
    }
    @Override
    public void closeHandler(Handler<Void> handler) {
      DbConnection.this.closeHandler(handler);
    }
    @Override
    public void exceptionHandler(Handler<Throwable> handler) {
      DbConnection.this.exceptionHandler(handler);
    }
    @Override
    public void close() {
      if (Vertx.currentContext() == context) {
        doClose();
      } else {
        context.runOnContext(v -> doClose());
      }
    }
  };

  private void doClose() {
    if (status == Status.CONNECTED) {
      status = Status.CLOSING;
      check();
    }
  }

  void doExecute(Command cmd) {
    if (status == Status.CONNECTED) {
      if (inflight.size() < client.pipeliningLimit) {
        inflight.add(cmd);
        writeToChannel(new QueryMessage(cmd.sql));
      } else {
        pending.add(cmd);
      }
    } else {
      cmd.onError("Connection not open " + status);
    }
  }

  private int toInt(byte[] data) {
    int value = 0;
    for (int i = data.length - 1;i >= 0;i--) {
      value = 10 * value + data[i] - '0';
    }
    return value;
  }

  void handleMessage(Message msg) {

    if (msg.getClass() == AuthenticationMD5PasswordMessage.class) {
      AuthenticationMD5PasswordMessage authMD5 = (AuthenticationMD5PasswordMessage) msg;
      writeToChannel(new PasswordMessage(client.username, client.password, authMD5.getSalt()));
    } else if (msg.getClass() == AuthenticationClearTextPasswordMessage.class) {
      writeToChannel(new PasswordMessage(client.username, client.password, null));
    } else if (msg.getClass() == AuthenticationOkMessage.class) {
      handler.handle(Future.succeededFuture(conn));
      handler = null;
    } else if (msg.getClass() == ReadyForQueryMessage.class) {
      // Ready for query
      TransactionStatus status = ((ReadyForQueryMessage) msg).getTransactionStatus();
    } else if (msg.getClass() == RowDescriptionMessage.class) {
      rowDesc = (RowDescriptionMessage) msg;
      result = new Result();
    } else if (msg.getClass() == DataRowMessage.class) {
      DataRowMessage dataRow = (DataRowMessage) msg;
      Column[] columns = rowDesc.getColumns();
      Row row = new Row();
      for (int i = 0; i < columns.length; i++) {
        Column columnDesc = columns[i];
        ColumnType type = columnDesc.getType();
        byte[] data = dataRow.getValue(i);
        switch (type) {
          case INT4:
            row.add(toInt(data));
            break;
          case NAME:
            row.add(new String(data));
            break;
          case VARCHAR:
            row.add(new String(data, UTF_8));
            break;
          case TEXT:
            row.add(new String(data, UTF_8));
            break;
          default:
            System.out.println("unsupported " + type);
            break;
        }
      }
      result.add(row);
    } else if (msg.getClass() == CommandCompleteMessage.class) {
      CommandCompleteMessage complete = (CommandCompleteMessage) msg;
      Result r = result;
      result = null;
      rowDesc = null;
      if (r == null) {
        r = new Result();
      }
      r.setUpdatedRows(complete.getRowsAffected());
      inflight.poll().onSuccess(r);
      check();
    } else if (msg.getClass() == ErrorResponseMessage.class) {
      ErrorResponseMessage error = (ErrorResponseMessage) msg;
      if (handler != null) {
        handler.handle(Future.failedFuture(error.getMessage()));
        handler = null;
        close();
        return;
      }
      result = null;
      rowDesc = null;
      inflight.poll().onError(error.getMessage());
      check();
    } else {
      System.out.println("Unhandled message " + msg);
    }
  }

  @Override
  protected void handleClosed() {
    status = Status.CLOSED;
    for (ArrayDeque<Command> q : Arrays.asList(inflight, pending)) {
      Command cmd;
      while ((cmd = q.poll()) != null) {
        Command t = cmd;
        context.runOnContext(v -> t.onError("closed"));
      }
    }
    super.handleClosed();
  }

  @Override
  protected synchronized void handleException(Throwable t) {
    super.handleException(t);
    close();
  }

  private void check() {
    switch (status) {
      case CLOSING:
        if (inflight.isEmpty()) {
          writeToChannel(new TerminateMessage());
        }
        break;
      case CONNECTED:
        Command cmd = pending.poll();
        if (cmd != null) {
          inflight.add(cmd);
          writeToChannel(new QueryMessage(cmd.sql));
        }
        break;
    }
  }

  @Override
  public NetworkMetrics metrics() {
    return new DummyVertxMetrics.DummyDatagramMetrics();
  }

  @Override
  protected void handleInterestedOpsChanged() {

  }
}

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
import io.vertx.pgclient.codec.decoder.message.Column;
import io.vertx.pgclient.codec.decoder.message.ColumnType;
import io.vertx.pgclient.codec.decoder.message.TransactionStatus;
import io.vertx.pgclient.codec.decoder.message.AuthenticationClearTextPassword;
import io.vertx.pgclient.codec.decoder.message.AuthenticationMD5Password;
import io.vertx.pgclient.codec.decoder.message.AuthenticationOk;
import io.vertx.pgclient.codec.decoder.message.CommandComplete;
import io.vertx.pgclient.codec.decoder.message.DataRow;
import io.vertx.pgclient.codec.decoder.message.ErrorResponse;
import io.vertx.pgclient.codec.decoder.message.ReadyForQuery;
import io.vertx.pgclient.codec.decoder.message.RowDescription;
import io.vertx.pgclient.codec.encoder.message.PasswordMessage;
import io.vertx.pgclient.codec.encoder.message.Query;
import io.vertx.pgclient.codec.encoder.message.Terminate;

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
  private RowDescription rowDesc;
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
        writeToChannel(new Query(cmd.sql));
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

    if (msg.getClass() == AuthenticationMD5Password.class) {
      AuthenticationMD5Password authMD5 = (AuthenticationMD5Password) msg;
      writeToChannel(new PasswordMessage(client.username, client.password, authMD5.getSalt()));
    } else if (msg.getClass() == AuthenticationClearTextPassword.class) {
      writeToChannel(new PasswordMessage(client.username, client.password, null));
    } else if (msg.getClass() == AuthenticationOk.class) {
      handler.handle(Future.succeededFuture(conn));
      handler = null;
    } else if (msg.getClass() == ReadyForQuery.class) {
      // Ready for query
      TransactionStatus status = ((ReadyForQuery) msg).getTransactionStatus();
    } else if (msg.getClass() == RowDescription.class) {
      rowDesc = (RowDescription) msg;
      result = new Result();
    } else if (msg.getClass() == DataRow.class) {
      DataRow dataRow = (DataRow) msg;
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
    } else if (msg.getClass() == CommandComplete.class) {
      CommandComplete complete = (CommandComplete) msg;
      Result r = result;
      result = null;
      rowDesc = null;
      if (r == null) {
        r = new Result();
      }
      r.setUpdatedRows(complete.getRowsAffected());
      inflight.poll().onSuccess(r);
      check();
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
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
          writeToChannel(new Terminate());
        }
        break;
      case CONNECTED:
        Command cmd = pending.poll();
        if (cmd != null) {
          inflight.add(cmd);
          writeToChannel(new Query(cmd.sql));
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

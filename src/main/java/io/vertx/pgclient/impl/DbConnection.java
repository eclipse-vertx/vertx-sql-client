package io.vertx.pgclient.impl;

import com.github.pgasync.impl.Oid;
import com.github.pgasync.impl.message.Authentication;
import com.github.pgasync.impl.message.CommandComplete;
import com.github.pgasync.impl.message.DataRow;
import com.github.pgasync.impl.message.ErrorResponse;
import com.github.pgasync.impl.message.Message;
import com.github.pgasync.impl.message.PasswordMessage;
import com.github.pgasync.impl.message.Query;
import com.github.pgasync.impl.message.ReadyForQuery;
import com.github.pgasync.impl.message.RowDescription;
import com.github.pgasync.impl.message.Terminate;
import io.netty.channel.Channel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.spi.metrics.NetworkMetrics;
import io.vertx.pgclient.PostgresConnection;
import io.vertx.pgclient.Result;
import io.vertx.pgclient.Row;

import java.util.ArrayDeque;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

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

    public void execute(String sql, Handler<AsyncResult<Result>> resultHandler) {
      Command cmd = new Command(sql, resultHandler);
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
    if (msg.getClass() == Authentication.class) {
      Authentication auth = (Authentication) msg;
      if (auth.isAuthenticationOk()) {
        handler.handle(Future.succeededFuture(conn));
        handler = null;
      } else {
        writeToChannel(new PasswordMessage(client.username, client.password, auth.getMd5Salt()));
      }
    } else if (msg.getClass() == ReadyForQuery.class) {
      // Ready for query
    } else if (msg.getClass() == RowDescription.class) {
      rowDesc = (RowDescription) msg;
      result = new Result();
    } else if (msg.getClass() == DataRow.class) {
      DataRow dataRow = (DataRow) msg;
      RowDescription.ColumnDescription[] columns = rowDesc.getColumns();
      Row row = new Row();
      for (int i = 0; i < columns.length; i++) {
        RowDescription.ColumnDescription columnDesc = columns[i];
        Oid type = columnDesc.getType();
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
      r.setUpdatedRows(complete.getUpdatedRows());
      inflight.poll().onSuccess(r);
      check();
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      result = null;
      rowDesc = null;
      inflight.poll().onError(error.getMessage());
      check();
    } else {
      System.out.println("got message " + msg);
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

  private void check() {
    switch (status) {
      case CLOSING:
        if (inflight.isEmpty()) {
          writeToChannel(Terminate.INSTANCE);
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
    return null;
  }

  @Override
  protected void handleInterestedOpsChanged() {

  }
}

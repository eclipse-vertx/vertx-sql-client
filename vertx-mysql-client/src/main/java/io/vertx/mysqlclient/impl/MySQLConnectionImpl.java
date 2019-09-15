package io.vertx.mysqlclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.MySQLSetOption;
import io.vertx.mysqlclient.impl.command.ChangeUserCommand;
import io.vertx.mysqlclient.impl.command.DebugCommand;
import io.vertx.mysqlclient.impl.command.InitDbCommand;
import io.vertx.mysqlclient.impl.command.PingCommand;
import io.vertx.mysqlclient.impl.command.ResetConnectionCommand;
import io.vertx.mysqlclient.impl.command.SetOptionCommand;
import io.vertx.mysqlclient.impl.command.StatisticsCommand;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

public class MySQLConnectionImpl extends SqlConnectionImpl<MySQLConnectionImpl> implements MySQLConnection {

  public static void connect(Vertx vertx, MySQLConnectOptions options, Handler<AsyncResult<MySQLConnection>> handler) {
    Context ctx = Vertx.currentContext();
    if (ctx != null) {
      MySQLConnectionFactory client;
      try {
        client = new MySQLConnectionFactory(ctx, false, options);
      } catch (Exception e) {
        handler.handle(Future.failedFuture(e));
        return;
      }
      client.connect(ar-> {
        if (ar.succeeded()) {
          Connection conn = ar.result();
          MySQLConnectionImpl p = new MySQLConnectionImpl(client, ctx, conn);
          conn.init(p);
          handler.handle(Future.succeededFuture(p));
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
        }
      });
    } else {
      vertx.runOnContext(v -> {
        connect(vertx, options, handler);
      });
    }
  }

  private final MySQLConnectionFactory factory;

  public MySQLConnectionImpl(MySQLConnectionFactory factory, Context context, Connection conn) {
    super(context, conn);

    this.factory = factory;
  }

  @Override
  public void handleNotification(int processId, String channel, String payload) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Transaction begin() {
    throw new UnsupportedOperationException("Transaction is not supported for now");
  }

  @Override
  public Transaction begin(boolean closeOnEnd) {
    throw new UnsupportedOperationException("Transaction is not supported for now");
  }

  @Override
  public MySQLConnection ping(Handler<AsyncResult<Void>> handler) {
    PingCommand cmd = new PingCommand();
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection specifySchema(String schemaName, Handler<AsyncResult<Void>> handler) {
    InitDbCommand cmd = new InitDbCommand(schemaName);
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection getInternalStatistics(Handler<AsyncResult<String>> handler) {
    StatisticsCommand cmd = new StatisticsCommand();
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection setOption(MySQLSetOption option, Handler<AsyncResult<Void>> handler) {
    SetOptionCommand cmd = new SetOptionCommand(option);
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection resetConnection(Handler<AsyncResult<Void>> handler) {
    ResetConnectionCommand cmd = new ResetConnectionCommand();
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection debug(Handler<AsyncResult<Void>> handler) {
    DebugCommand cmd = new DebugCommand();
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection changeUser(MySQLConnectOptions options, Handler<AsyncResult<Void>> handler) {
    String collationName = options.getCollation();
    MySQLCollation collation;
    if (collationName == null) {
      // override the collation if configured
      String charset = options.getCharset();
      try {
        collationName = MySQLCollation.getDefaultCollationFromCharsetName(charset);
      } catch (IllegalArgumentException e) {
        handler.handle(Future.failedFuture(e));
        return this;
      }
    }
    try {
      collation = MySQLCollation.valueOfName(collationName);
    } catch (IllegalArgumentException e) {
      handler.handle(Future.failedFuture(e));
      return this;
    }
    ChangeUserCommand cmd = new ChangeUserCommand(options.getUser(), options.getPassword(), options.getDatabase(), collation, options.getProperties());
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }
}

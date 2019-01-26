package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.MySQLConnection;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandBase;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandResponse;
import io.reactiverse.mysqlclient.impl.codec.encoder.PingCommand;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;

public class MySQLConnectionImpl extends MySQLClientBase<MySQLConnectionImpl> implements MySQLConnection {
  private Context context;
  private MySQLSocketConnection connection;

  public MySQLConnectionImpl(Context context, MySQLSocketConnection connection) {
    this.context = context;
    this.connection = connection;
  }

  @Override
  public <R> void schedule(MySQLCommandBase<R> cmd, Handler<? super MySQLCommandResponse<R>> handler) {
    cmd.setHandler(handler);
    connection.schedule(cmd);
  }

  @Override
  public MySQLConnection closeHandler(Handler<Void> closeHandler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MySQLConnection ping(Handler<AsyncResult<Void>> handler) {
    schedule(new PingCommand(), handler);
    return this;
  }
}

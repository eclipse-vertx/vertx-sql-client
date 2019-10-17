package io.vertx.mssqlclient.impl;

import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLPool;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

public class MSSQLPoolImpl extends PoolBase<MSSQLPoolImpl> implements MSSQLPool {
  private final MSSQLConnectionFactory connectionFactory;

  public MSSQLPoolImpl(Context context, boolean closeVertx, MSSQLConnectOptions connectOptions, PoolOptions poolOptions) {
    super(context, closeVertx, poolOptions);
    this.connectionFactory = new MSSQLConnectionFactory(context, Vertx.currentContext() != null, connectOptions);
  }

  @Override
  public void connect(Handler<AsyncResult<Connection>> handler) {
    connectionFactory.create(handler);
  }

  @Override
  protected SqlConnectionImpl wrap(Context context, Connection connection) {
    return new MSSQLConnectionImpl(connectionFactory, context, connection);
  }

  @Override
  public void begin(Handler<AsyncResult<Transaction>> handler) {
    throw new UnsupportedOperationException("Transaction is not supported for now");
  }

  @Override
  protected void doClose() {
    connectionFactory.close();
    super.doClose();
  }
}

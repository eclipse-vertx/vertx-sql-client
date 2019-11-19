package io.vertx.mssqlclient.impl;

import io.vertx.core.impl.ContextInternal;
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

  public MSSQLPoolImpl(ContextInternal context, boolean closeVertx, MSSQLConnectOptions connectOptions, PoolOptions poolOptions) {
    super(context, closeVertx, poolOptions);
    this.connectionFactory = new MSSQLConnectionFactory(context.owner(), connectOptions);
  }

  @Override
  public void connect(ContextInternal context, Handler<AsyncResult<Connection>> completionHandler) {
    connectionFactory.create(context).setHandler(completionHandler);
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection connection) {
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

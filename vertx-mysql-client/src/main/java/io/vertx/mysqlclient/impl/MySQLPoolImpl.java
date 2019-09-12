package io.vertx.mysqlclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

public class MySQLPoolImpl extends PoolBase<MySQLPoolImpl> implements MySQLPool {
  private final MySQLConnectionFactory factory;

  public MySQLPoolImpl(Context context, boolean closeVertx, MySQLConnectOptions connectOptions, PoolOptions poolOptions) {
    super(context, closeVertx, poolOptions);
    this.factory = new MySQLConnectionFactory(context, Vertx.currentContext() != null, connectOptions);
  }

  @Override
  public void connect(Handler<AsyncResult<Connection>> completionHandler) {
    factory.connect(completionHandler);
  }

  @Override
  protected SqlConnectionImpl wrap(Context context, Connection conn) {
    return new MySQLConnectionImpl(factory, context, conn);
  }

  @Override
  protected void doClose() {
    factory.close();
    super.doClose();
  }
}

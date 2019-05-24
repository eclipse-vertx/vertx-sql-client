package io.vertx.mysqlclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.pgclient.PgPoolOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

public class MySQLPoolImpl extends PoolBase<MySQLPoolImpl> implements MySQLPool {
  private final MySQLConnectionFactory factory;

  public MySQLPoolImpl(Context context, boolean closeVertx, PgPoolOptions options) {
    super(context, closeVertx, options);
    this.factory = new MySQLConnectionFactory(context, options);
  }

  @Override
  public void connect(Handler<AsyncResult<Connection>> completionHandler) {
    factory.connect(completionHandler);
  }

  @Override
  protected SqlConnectionImpl wrap(Context context, Connection conn) {
    return new MySQLConnectionImpl(factory, context, conn);
  }
}

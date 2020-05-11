package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.sqlclient.Driver;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;

public class MySQLDriver implements Driver {

  @Override
  public Pool createPool(SqlConnectOptions options, PoolOptions poolOptions) {
    return MySQLPool.pool(new MySQLConnectOptions(options), poolOptions);
  }

  @Override
  public Pool createPool(Vertx vertx, SqlConnectOptions options, PoolOptions poolOptions) {
    return MySQLPool.pool(vertx, new MySQLConnectOptions(options), poolOptions);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof MySQLConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

}

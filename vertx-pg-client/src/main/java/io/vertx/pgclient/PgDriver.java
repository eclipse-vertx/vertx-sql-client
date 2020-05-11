package io.vertx.pgclient;

import io.vertx.core.Vertx;
import io.vertx.sqlclient.Driver;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;

public class PgDriver implements Driver {

  @Override
  public Pool createPool(SqlConnectOptions options, PoolOptions poolOptions) {
    return PgPool.pool(new PgConnectOptions(options), poolOptions);
  }

  @Override
  public Pool createPool(Vertx vertx, SqlConnectOptions options, PoolOptions poolOptions) {
    return PgPool.pool(vertx, new PgConnectOptions(options), poolOptions);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof PgConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

}

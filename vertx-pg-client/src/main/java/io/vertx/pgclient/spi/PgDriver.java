package io.vertx.pgclient.spi;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolConfig;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.Driver;

public class PgDriver implements Driver {

  @Override
  public Pool createPool(PoolConfig config) {
    return PgPool.pool(PgConnectOptions.wrap(config.determineConnectOptions()), config.options());
  }

  @Override
  public Pool createPool(Vertx vertx, PoolConfig config) {
    return PgPool.pool(vertx, PgConnectOptions.wrap(config.determineConnectOptions()), config.options());
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof PgConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }
}

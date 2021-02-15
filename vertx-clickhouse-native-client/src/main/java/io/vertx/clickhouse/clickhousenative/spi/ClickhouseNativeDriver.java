package io.vertx.clickhouse.clikhousenative.spi;

import io.vertx.clickhouse.clikhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clikhousenative.ClickhouseNativePool;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.Driver;

public class ClickhouseNativeDriver implements Driver {
  @Override
  public Pool createPool(SqlConnectOptions options, PoolOptions poolOptions) {
    return ClickhouseNativePool.pool(wrap(options), poolOptions);
  }

  @Override
  public Pool createPool(Vertx vertx, SqlConnectOptions options, PoolOptions poolOptions) {
    return ClickhouseNativePool.pool(vertx, wrap(options), poolOptions);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof ClickhouseNativeConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  private static ClickhouseNativeConnectOptions wrap(SqlConnectOptions options) {
    if (options instanceof ClickhouseNativeConnectOptions) {
      return (ClickhouseNativeConnectOptions) options;
    } else {
      return new ClickhouseNativeConnectOptions(options);
    }
  }
}

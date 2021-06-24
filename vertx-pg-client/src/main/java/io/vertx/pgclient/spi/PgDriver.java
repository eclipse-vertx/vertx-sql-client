package io.vertx.pgclient.spi;

import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.impl.PgConnectionFactory;
import io.vertx.pgclient.impl.PgPoolImpl;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.Driver;
import io.vertx.sqlclient.spi.ConnectionFactory;

import java.util.List;

public class PgDriver implements Driver {

  @Override
  public PgPool createPool(Vertx vertx, List<? extends SqlConnectOptions> databases, PoolOptions options) {
    return PgPoolImpl.create((VertxInternal) vertx, false, databases, options);
  }

  public PgPool createClient(Vertx vertx, List<? extends SqlConnectOptions> servers, PoolOptions options) {
    return PgPoolImpl.create((VertxInternal) vertx, true, servers, options);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof PgConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  @Override
  public ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database) {
    return new PgConnectionFactory((VertxInternal) vertx, PgConnectOptions.wrap(database));
  }
}

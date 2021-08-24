package io.vertx.pgclient.spi;

import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.impl.PgConnectionFactory;
import io.vertx.pgclient.impl.PgPoolImpl;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

public class PgDriver implements Driver {

  private static final String ACCEPT_URI_REGEX = "postgre(s|sql)://.*";

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

  @Override
  public boolean acceptsUri(String connectionUri) {
    return connectionUri.matches(ACCEPT_URI_REGEX);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends SqlConnectOptions> T getConnectOptions(String connectionUri) {
    return (T) PgConnectOptions.fromUri(connectionUri);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends SqlConnectOptions> T getConnectOptions(String connectionUri, JsonObject json) {
	  PgConnectOptions fromUri = PgConnectOptions.fromUri(connectionUri);
    return (T) new PgConnectOptions(fromUri.toJson().mergeIn(json));
  }
}

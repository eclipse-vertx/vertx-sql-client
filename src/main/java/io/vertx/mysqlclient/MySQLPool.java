package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.mysqlclient.impl.MySQLPoolImpl;
import io.vertx.pgclient.PgPoolOptions;
import io.vertx.sqlclient.Pool;

public interface MySQLPool extends Pool {
  static MySQLPool pool(PgPoolOptions options) {
    if (Vertx.currentContext() != null) {
      throw new IllegalStateException("Running in a Vertx context => use MySQLPool#pool(Vertx, PgPoolOptions) instead");
    }
    VertxOptions vertxOptions = new VertxOptions();
    if (options.isUsingDomainSocket()) {
      vertxOptions.setPreferNativeTransport(true);
    }
    Vertx vertx = Vertx.vertx(vertxOptions);
    return new MySQLPoolImpl(vertx.getOrCreateContext(), true, options);
  }

  /**
   * Like {@link #pool(PgPoolOptions)} with a specific {@link Vertx} instance.
   */
  static MySQLPool pool(Vertx vertx, PgPoolOptions options) {
    return new MySQLPoolImpl(vertx.getOrCreateContext(), false, options);
  }
}

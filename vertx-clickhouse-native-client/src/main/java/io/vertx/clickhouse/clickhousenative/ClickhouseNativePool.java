package io.vertx.clickhouse.clickhousenative;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativePoolImpl;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.VertxInternal;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

@VertxGen
public interface ClickhouseNativePool extends Pool {
  static ClickhouseNativePool pool(ClickhouseNativeConnectOptions connectOptions, PoolOptions poolOptions) {
    if (Vertx.currentContext() != null) {
      throw new IllegalStateException("Running in a Vertx context => use ClickhouseNativePool#pool(Vertx, PgConnectOptions, PoolOptions) instead");
    }
    VertxOptions vertxOptions = new VertxOptions();
    VertxInternal vertx = (VertxInternal) Vertx.vertx(vertxOptions);
    return ClickhouseNativePoolImpl.create(vertx.getOrCreateContext(), true, connectOptions, poolOptions);
  }


  static ClickhouseNativePool pool(Vertx vertx, ClickhouseNativeConnectOptions connectOptions, PoolOptions poolOptions) {
    return ClickhouseNativePoolImpl.create(((VertxInternal)vertx).getOrCreateContext(), false, connectOptions, poolOptions);
  }
}

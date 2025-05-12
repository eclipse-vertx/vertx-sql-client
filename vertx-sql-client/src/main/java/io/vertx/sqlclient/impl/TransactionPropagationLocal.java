package io.vertx.sqlclient.impl;

import io.vertx.core.internal.VertxBootstrap;
import io.vertx.core.spi.VertxServiceProvider;
import io.vertx.core.spi.context.storage.ContextLocal;
import io.vertx.sqlclient.SqlConnection;

public class TransactionPropagationLocal implements VertxServiceProvider {

  public static final ContextLocal<SqlConnection> KEY = ContextLocal.registerLocal(SqlConnection.class);

  @Override
  public void init(VertxBootstrap builder) {
  }
}

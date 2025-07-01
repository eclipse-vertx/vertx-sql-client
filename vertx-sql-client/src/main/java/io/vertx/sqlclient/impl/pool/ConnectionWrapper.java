package io.vertx.sqlclient.impl.pool;

import io.vertx.core.internal.ContextInternal;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import io.vertx.sqlclient.spi.connection.ConnectionFactory;

public interface ConnectionWrapper<O extends SqlConnectOptions> {

  SqlConnectionInternal wrap(ContextInternal context, ConnectionFactory<O> factory, Connection conn);
}

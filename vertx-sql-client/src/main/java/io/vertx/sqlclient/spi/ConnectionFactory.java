package io.vertx.sqlclient.spi;

import io.vertx.core.Closeable;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.*;
import io.vertx.core.internal.ContextInternal;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;

/**
 * A connection factory, can be obtained from {@link Driver#createConnectionFactory}
 */
public interface ConnectionFactory<C extends SqlConnectOptions> extends Closeable {

  default Future<SqlConnection> connect(Context context, Future<C> fut) {
    // The future might be on any context or context-less
    // So we need to use a specific context promise
    Promise<C> promise = ((ContextInternal) context).promise();
    fut.onComplete(promise);
    return promise
      .future()
      .compose(connectOptions -> connect(context, connectOptions));

  }

  /**
   * Create a connection using the given {@code context}.
   *
   * @param context the context
   * @return the future connection
   */
  Future<SqlConnection> connect(Context context, C options);

}

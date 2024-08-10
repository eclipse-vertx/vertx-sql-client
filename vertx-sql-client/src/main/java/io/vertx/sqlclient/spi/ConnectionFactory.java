package io.vertx.sqlclient.spi;

import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;

/**
 * A connection factory, can be obtained from {@link Driver#createConnectionFactory}
 */
public interface ConnectionFactory extends Closeable {

  default Future<SqlConnection> connect(Context context, Future<? extends SqlConnectOptions> fut) {
    // The future might be on any context or context-less
    // So we need to use a specific context promise
    Promise<SqlConnectOptions> promise = ((ContextInternal) context).promise();
    fut.onComplete(ar -> {
      if (ar.succeeded()) {
        promise.complete(ar.result());
      } else {
        promise.fail(ar.cause());
      }
    });
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
  Future<SqlConnection> connect(Context context);

  /**
   * Create a connection using the given {@code context}.
   *
   * @param context the context
   * @return the future connection
   */
  Future<SqlConnection> connect(Context context, SqlConnectOptions options);

}

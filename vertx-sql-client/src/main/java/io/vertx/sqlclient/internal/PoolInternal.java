package io.vertx.sqlclient.internal;

import io.vertx.core.Future;
import io.vertx.core.internal.Closeable;
import io.vertx.sqlclient.Pool;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PoolInternal extends Pool, Closeable {

  @Override
  default Future<Void> close() {
    return Closeable.super.close();
  }
}

package io.vertx.sqlclient.internal;

import io.vertx.sqlclient.Row;

/**
 * Row internal API
 */
public interface RowInternal extends Row {

  /**
   * Try to recycle the row, this shall be called by the row decoder to check whether the row
   * instance can be reused.
   *
   * @return whether the row can be reused safely
   */
  default boolean tryRecycle() {
    return false;
  }
}

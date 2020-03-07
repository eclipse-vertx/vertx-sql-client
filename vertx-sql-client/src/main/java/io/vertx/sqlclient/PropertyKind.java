package io.vertx.sqlclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

/**
 * The kind of the property, this can be used to fetch some specific property of the {@link SqlResult execution result}.
 */
@VertxGen
public interface PropertyKind<T> {
  /**
   * Get the type of the value of this kind of property.
   *
   * @return the type
   */
  @GenIgnore
  Class<T> type();
}

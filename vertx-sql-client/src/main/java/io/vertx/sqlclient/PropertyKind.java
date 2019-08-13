package io.vertx.sqlclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Represents which kind the property is.
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

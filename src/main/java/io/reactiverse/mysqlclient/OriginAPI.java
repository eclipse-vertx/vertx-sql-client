package io.reactiverse.mysqlclient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * API from reactive-pg-client original API
 */
@Target(ElementType.TYPE)
public @interface OriginAPI {
  //TODO see if we can design a new common API adapted to all sql clients?
}

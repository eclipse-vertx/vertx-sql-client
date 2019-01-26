package io.reactiverse.mysqlclient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Similar or same to implementation of reactive-pg-client
 */
@Target(ElementType.TYPE)
public @interface ImplReusable {
  //TODO See if we can extract them to a common module?
}

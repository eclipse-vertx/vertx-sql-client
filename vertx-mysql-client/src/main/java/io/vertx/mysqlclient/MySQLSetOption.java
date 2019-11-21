package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;

/**
 * MySQL set options which can be used by {@link MySQLConnection#setOption(MySQLSetOption, Handler)}.
 */
@VertxGen
public enum MySQLSetOption {
  MYSQL_OPTION_MULTI_STATEMENTS_ON, MYSQL_OPTION_MULTI_STATEMENTS_OFF
}

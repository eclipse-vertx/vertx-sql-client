package io.vertx.mysqlclient;

import io.vertx.core.Handler;

/**
 * MySQL set options which can be used by {@link MySQLConnection#setOption(MySQLSetOption, Handler)}.
 */
public enum MySQLSetOption {
  MYSQL_OPTION_MULTI_STATEMENTS_ON, MYSQL_OPTION_MULTI_STATEMENTS_OFF
}

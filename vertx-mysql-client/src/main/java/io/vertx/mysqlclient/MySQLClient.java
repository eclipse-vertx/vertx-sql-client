package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.sqlclient.PropertyKind;

/**
 * An interface to define MySQL specific constants or behaviors.
 */
@VertxGen
public interface MySQLClient {
  /**
   * SqlResult Property for last_insert_id
   */
  PropertyKind<Long> LAST_INSERTED_ID = () -> Long.class;
}

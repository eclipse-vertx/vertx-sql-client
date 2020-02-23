package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.sqlclient.PropertyKind;

/**
 * An interface to define MySQL specific constants or behaviors.
 */
@VertxGen
public interface MySQLClient {
  /**
   * SqlResult {@link PropertyKind property kind} for MySQL last_insert_id.<br>
   * The property kind can be used to fetch the auto incremented id of the last row when executing inserting or updating operations.
   */
  PropertyKind<Long> LAST_INSERTED_ID = () -> Long.class;
}

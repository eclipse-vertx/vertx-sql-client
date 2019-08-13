package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlResultProperty;

/**
 *
 */
@VertxGen
public interface MySQLResult {
  SqlResultProperty<Integer> LAST_INSERTED_ID = new SqlResultProperty<Integer>() {
    // SqlResult Property for last_insert_id
  };
}

package com.julienviet.pgclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface Query {

  @Fluent
  Query limit(int limit);

  void execute(Handler<AsyncResult<PgResultSet>> handler);

}

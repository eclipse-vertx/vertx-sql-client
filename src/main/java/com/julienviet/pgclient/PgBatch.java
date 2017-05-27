package com.julienviet.pgclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgBatch {

  @Fluent
  PgBatch add(Object param1);

  @Fluent
  PgBatch add(Object param1, Object param2);

  @Fluent
  PgBatch add(Object param1, Object param2, Object param3);

  @Fluent
  PgBatch add(Object param1, Object param2, Object param3, Object param4);

  @Fluent
  PgBatch add(Object param1, Object param2, Object param3, Object param4, Object param5);

  @Fluent
  PgBatch add(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6);

  @Fluent
  PgBatch add(List<Object> params);

  void execute(Handler<AsyncResult<List<UpdateResult>>> resultHandler);

}

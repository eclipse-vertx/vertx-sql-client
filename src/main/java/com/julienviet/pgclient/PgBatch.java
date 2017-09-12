package com.julienviet.pgclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgBatch {

  @Fluent
  default PgBatch add(Object param1) {
    return add(Collections.singletonList(param1));
  }

  @Fluent
  default PgBatch add(Object param1, Object param2) {
    return add(Arrays.asList(param1, param2));
  }

  @Fluent
  default PgBatch add(Object param1, Object param2, Object param3) {
    return add(Arrays.asList(param1, param2, param3));
  }

  @Fluent
  default PgBatch add(Object param1, Object param2, Object param3, Object param4) {
    return add(Arrays.asList(param1, param2, param3, param4));
  }

  @Fluent
  default PgBatch add(Object param1, Object param2, Object param3, Object param4, Object param5) {
    return add(Arrays.asList(param1, param2, param3, param4, param5));
  }

  @Fluent
  default PgBatch add(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
    return add(Arrays.asList(param1, param2, param3, param4, param5, param6));
  }

  @Fluent
  PgBatch add(List<Object> params);

  void execute(Handler<AsyncResult<List<UpdateResult>>> resultHandler);

}

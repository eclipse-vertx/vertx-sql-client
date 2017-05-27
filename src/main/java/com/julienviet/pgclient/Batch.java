package com.julienviet.pgclient;

import com.julienviet.pgclient.impl.BatchImpl;
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
public interface Batch {

  @Fluent
  Batch add(Object param1);

  @Fluent
  Batch add(Object param1, Object param2);

  @Fluent
  Batch add(Object param1, Object param2, Object param3);

  @Fluent
  Batch add(Object param1, Object param2, Object param3, Object param4);

  @Fluent
  Batch add(Object param1, Object param2, Object param3, Object param4, Object param5);

  @Fluent
  Batch add(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6);

  @Fluent
  Batch add(List<Object> params);

  void execute(Handler<AsyncResult<List<UpdateResult>>> resultHandler);

}

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
 * A batch execution.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgBatch {

  /**
   * Add a command with a no arguments to this batch
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default PgBatch add() {
    return add(Collections.emptyList());
  }

  /**
   * Add a command with a single argument to this batch
   *
   * @param param1 the first argument of the command
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default PgBatch add(Object param1) {
    return add(Collections.singletonList(param1));
  }

  /**
   * Add a command with two arguments to this batch
   *
   * @param param1 the first argument of the command
   * @param param2 the second argument of the command
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default PgBatch add(Object param1, Object param2) {
    return add(Arrays.asList(param1, param2));
  }

  /**
   * Add a command with three arguments to this batch
   *
   * @param param1 the first argument of the command
   * @param param2 the second argument of the command
   * @param param3 the third argument of the command
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default PgBatch add(Object param1, Object param2, Object param3) {
    return add(Arrays.asList(param1, param2, param3));
  }

  /**
   * Add a command with four arguments to this batch
   *
   * @param param1 the first argument of the command
   * @param param2 the second argument of the command
   * @param param3 the third argument of the command
   * @param param4 the fourth argument of the command
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default PgBatch add(Object param1, Object param2, Object param3, Object param4) {
    return add(Arrays.asList(param1, param2, param3, param4));
  }

  /**
   * Add a command with five arguments to this batch
   *
   * @param param1 the first argument of the command
   * @param param2 the second argument of the command
   * @param param3 the third argument of the command
   * @param param4 the fourth argument of the command
   * @param param5 the fifth argument of the command
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default PgBatch add(Object param1, Object param2, Object param3, Object param4, Object param5) {
    return add(Arrays.asList(param1, param2, param3, param4, param5));
  }

  /**
   * Add a command with six arguments to this batch
   *
   * @param param1 the first argument of the command
   * @param param2 the second argument of the command
   * @param param3 the third argument of the command
   * @param param4 the fourth argument of the command
   * @param param5 the fifth argument of the command
   * @param param6 the sixth argument of the command
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default PgBatch add(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
    return add(Arrays.asList(param1, param2, param3, param4, param5, param6));
  }

  /**
   * Add a command with a variable number of arguments to this batch
   *
   * @param params the arguments of the command
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgBatch add(List<Object> params);

  /**
   * Execute the batch and notifies {@code resultHandler} of the result.
   *
   * @param resultHandler the handler of the result
   */
  void execute(Handler<AsyncResult<List<UpdateResult>>> resultHandler);

}

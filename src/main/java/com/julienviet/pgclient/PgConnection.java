package com.julienviet.pgclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A connection to Postgres.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public interface PgConnection {

  // for multiple sql statements
  @Fluent
  PgConnection execute(String sql, Handler<AsyncResult<ResultSet>> handler);

  // for "reading" such as SELECT probably the internal command will be ReadCommand instead of QueryCommand
  @Fluent
  PgConnection query(String sql, Handler<AsyncResult<ResultSet>> handler);

  // for "writing" such as INSERT, UPDATE and DELETE probably the internal command will be WriteCommand instead of UpdateCommand
  @Fluent
  PgConnection update(String sql, Handler<AsyncResult<UpdateResult>> handler);

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param, Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Collections.singletonList(param), handler);
  }

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param1, Object param2, Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Arrays.asList(param1, param2), handler);
  }

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Arrays.asList(param1, param2, param3), handler);
  }

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4,
                       Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Arrays.asList(param1, param2, param3, param4), handler);
  }

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                       Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Arrays.asList(param1, param2, param3, param4, param5), handler);
  }

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                       Object param6, Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Arrays.asList(param1, param2, param3, param4, param5, param6), handler);
  }

  @Fluent
  PgConnection prepareAndQuery(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler);

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Collections.singletonList(param), handler);
  }

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param1, Object param2, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Arrays.asList(param1, param2), handler);
  }

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Arrays.asList(param1, param2, param3), handler);
  }

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4), handler);
  }

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4, param5), handler);
  }

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4, param5, param6), handler);
  }

  @Fluent
  PgConnection prepareAndExecute(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler);

  @Fluent
  PgPreparedStatement prepare(String sql);

  @Fluent
  PgConnection exceptionHandler(Handler<Throwable> handler);

  @Fluent
  PgConnection closeHandler(Handler<Void> handler);

  void close();

}

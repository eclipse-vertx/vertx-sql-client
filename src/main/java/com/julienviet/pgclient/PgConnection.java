package com.julienviet.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public interface PgConnection {

  void exceptionHandler(Handler<Throwable> handler);

  // for multiple sql statements
  void execute(String sql, Handler<AsyncResult<ResultSet>> handler);

  // for "reading" such as SELECT probably the internal command will be ReadCommand instead of QueryCommand
  void query(String sql, Handler<AsyncResult<ResultSet>> handler);

  // for "writing" such as INSERT, UPDATE and DELETE probably the internal command will be WriteCommand instead of UpdateCommand
  void update(String sql, Handler<AsyncResult<UpdateResult>> handler);

  void prepareAndQuery(String sql, Object param, Handler<AsyncResult<ResultSet>> handler);

  void prepareAndQuery(String sql, Object param1, Object param2, Handler<AsyncResult<ResultSet>> handler);

  void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<ResultSet>> handler);

  void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4,
                       Handler<AsyncResult<ResultSet>> handler);

  void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                       Handler<AsyncResult<ResultSet>> handler);

  void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                       Object param6, Handler<AsyncResult<ResultSet>> handler);

  void prepareAndQuery(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler);

  void prepareAndExecute(String sql, Object param, Handler<AsyncResult<UpdateResult>> handler);

  void prepareAndExecute(String sql, Object param1, Object param2, Handler<AsyncResult<UpdateResult>> handler);

  void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<UpdateResult>> handler);

  void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Handler<AsyncResult<UpdateResult>> handler);

  void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Handler<AsyncResult<UpdateResult>> handler);

  void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6, Handler<AsyncResult<UpdateResult>> handler);

  void prepareAndExecute(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler);

  PgPreparedStatement prepare(String sql);

  void closeHandler(Handler<Void> handler);

  void close();

}

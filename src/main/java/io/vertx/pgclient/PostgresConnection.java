package io.vertx.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public interface PostgresConnection {

  void exceptionHandler(Handler<Throwable> handler);

  void execute(String sql, Handler<AsyncResult<Result>> handler);

  void prepareAndExecute(String sql, Object param, Handler<AsyncResult<Result>> handler);

  void prepareAndExecute(String sql, Object param1, Object param2, Handler<AsyncResult<Result>> handler);

  void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<Result>> handler);

  void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4,
                         Handler<AsyncResult<Result>> handler);

  void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                         Handler<AsyncResult<Result>> handler);

  void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                         Object param6, Handler<AsyncResult<Result>> handler);

  void prepareAndExecute(String sql, List<Object> params, Handler<AsyncResult<Result>> handler);

  void closeHandler(Handler<Void> handler);

  void close();

}

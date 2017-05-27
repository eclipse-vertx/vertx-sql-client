package com.julienviet.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PgPreparedStatement {

  PgQuery query();

  PgQuery query(Object param1);

  PgQuery query(Object param1, Object param2);

  PgQuery query(Object param1, Object param2, Object param3);

  PgQuery query(Object param1, Object param2, Object param3, Object param4);

  PgQuery query(Object param1, Object param2, Object param3, Object param4, Object param5);

  PgQuery query(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6);

  PgQuery query(List<Object> params);

  PgBatch batch();

  void close();

  void close(Handler<AsyncResult<Void>> completionHandler);

}

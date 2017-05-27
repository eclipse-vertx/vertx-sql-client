package com.julienviet.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PreparedStatement {

  Query query();

  Query query(Object param1);

  Query query(Object param1, Object param2);

  Query query(Object param1, Object param2, Object param3);

  Query query(Object param1, Object param2, Object param3, Object param4);

  Query query(Object param1, Object param2, Object param3, Object param4, Object param5);

  Query query(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6);

  Query query(List<Object> params);

  Batch batch();

  void close();

  void close(Handler<AsyncResult<Void>> completionHandler);

}

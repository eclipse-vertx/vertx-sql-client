package com.julienviet.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PgPreparedStatement {

  default PgQuery query() {
    return query(Collections.emptyList());
  }

  default PgQuery query(Object param1) {
    return query(Collections.singletonList(param1));
  }

  default PgQuery query(Object param1, Object param2) {
    return query(Arrays.asList(param1, param2));
  }

  default PgQuery query(Object param1, Object param2, Object param3) {
    return query(Arrays.asList(param1, param2, param3));
  }

  default PgQuery query(Object param1, Object param2, Object param3, Object param4) {
    return query(Arrays.asList(param1, param2, param3, param4));
  }

  default PgQuery query(Object param1, Object param2, Object param3, Object param4, Object param5) {
    return query(Arrays.asList(param1, param2, param3, param4, param5));
  }

  default PgQuery query(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
    return query(Arrays.asList(param1, param2, param3, param4, param5, param6));
  }

  PgQuery query(List<Object> params);

/*
  default PgRowStream queryStream() {
    return queryStream(Collections.emptyList());
  }

  default PgRowStream queryStream(Object param1) {
    return queryStream(Collections.singletonList(param1));
  }

  default PgRowStream queryStream(Object param1, Object param2) {
    return queryStream(Arrays.asList(param1, param2));
  }

  default PgRowStream queryStream(Object param1, Object param2, Object param3) {
    return queryStream(Arrays.asList(param1, param2, param3));
  }

  default PgRowStream queryStream(Object param1, Object param2, Object param3, Object param4) {
    return queryStream(Arrays.asList(param1, param2, param3, param4));
  }

  default PgRowStream queryStream(Object param1, Object param2, Object param3, Object param4, Object param5) {
    return queryStream(Arrays.asList(param1, param2, param3, param4, param5));
  }

  default PgRowStream queryStream(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
    return queryStream(Arrays.asList(param1, param2, param3, param4, param5, param6));
  }

  PgRowStream queryStream(List<Object> params);
*/

  PgBatch batch();

  void close();

  void close(Handler<AsyncResult<Void>> completionHandler);

}

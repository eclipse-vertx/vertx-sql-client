package com.julienviet.pgclient;

import com.julienviet.pgclient.impl.BatchImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PostgresBatch {

  static PostgresBatch batch() {
    return new BatchImpl();
  }

  @Fluent
  PostgresBatch add(Object param1);

  @Fluent
  PostgresBatch add(Object param1, Object param2);

  @Fluent
  PostgresBatch add(Object param1, Object param2, Object param3);

  @Fluent
  PostgresBatch add(Object param1, Object param2, Object param3, Object param4);

  @Fluent
  PostgresBatch add(Object param1, Object param2, Object param3, Object param4, Object param5);

  @Fluent
  PostgresBatch add(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6);

  @Fluent
  PostgresBatch add(List<Object> params);

}

package com.julienviet.pgclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLRowStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgRowStream extends SQLRowStream {

  @Fluent
  PgRowStream fetch(int size);

  @Override
  PgRowStream resultSetClosedHandler(Handler<Void> handler);

  @Override
  PgRowStream exceptionHandler(Handler<Throwable> handler);

  @Override
  PgRowStream handler(Handler<JsonArray> handler);

  @Override
  PgRowStream pause();

  @Override
  PgRowStream resume();

  @Override
  PgRowStream endHandler(Handler<Void> handler);

  void execute();
}

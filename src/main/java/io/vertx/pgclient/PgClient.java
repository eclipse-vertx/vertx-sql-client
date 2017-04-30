package io.vertx.pgclient;

import com.github.pgasync.ResultSet;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.pgclient.impl.PgClientImpl;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgClient {

  static PgClient create(Vertx vertx, PgClientOptions options) {
    PgClientImpl client = new PgClientImpl(vertx, options);
    client.start();
    return client;
  }

  @GenIgnore
  void query(String sql, Handler<AsyncResult<ResultSet>> completionHandler);

}

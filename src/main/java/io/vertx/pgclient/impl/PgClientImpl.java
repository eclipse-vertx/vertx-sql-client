package io.vertx.pgclient.impl;

import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.ResultSet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgClient;
import io.vertx.pgclient.PgClientOptions;

import java.lang.reflect.Field;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgClientImpl implements PgClient {

  private final Vertx vertx;
  private final PgClientOptions options;
  private volatile VertxPool pool;

  public PgClientImpl(Vertx vertx, PgClientOptions options) {
    this.vertx = vertx;
    this.options = options;
  }

  public PgClient start() {

    ConnectionPoolBuilder builder = new ConnectionPoolBuilder();
    builder.hostname(options.getHost());
    builder.port(options.getPort());
    builder.database(options.getDatabase());
    builder.username(options.getUsername());
    builder.password(options.getPassword());
    builder.poolSize(options.getPoolsize());
    builder.pipeline(true);

    ConnectionPoolBuilder.PoolProperties props;
    try {
      Field field = ConnectionPoolBuilder.class.getDeclaredField("properties");
      field.setAccessible(true);
      props = (ConnectionPoolBuilder.PoolProperties) field.get(builder);
    } catch (Exception e) {
      throw new AssertionError(e);
    }

    Context ctx = vertx.getOrCreateContext();
    ctx.runOnContext(v -> {
    });
    if (pool != null) {
      throw new IllegalStateException("Already started");
    } else {
      pool = new VertxPool(ctx, props);
    }

    return this;
  }

  public void query(String sql, Handler<AsyncResult<ResultSet>> completionHandler) {
    pool.query(sql, rs -> {
      completionHandler.handle(Future.succeededFuture(rs));
    }, err -> {
      completionHandler.handle(Future.failedFuture(err));
    });
  }
}

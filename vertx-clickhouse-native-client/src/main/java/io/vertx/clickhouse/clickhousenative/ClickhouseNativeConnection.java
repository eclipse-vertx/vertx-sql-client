package io.vertx.clickhouse.clikhousenative;

import io.vertx.clickhouse.clikhousenative.impl.ClickhouseNativeConnectionImpl;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.sqlclient.SqlConnection;

@VertxGen
public interface ClickhouseNativeConnection extends SqlConnection {
  static void connect(Vertx vertx, ClickhouseNativeConnectOptions connectOptions, Handler<AsyncResult<ClickhouseNativeConnection>> handler) {
    Future<ClickhouseNativeConnection> fut = connect(vertx, connectOptions);
    if (handler != null) {
      fut.onComplete(handler);
    }
  }

  static Future<ClickhouseNativeConnection> connect(Vertx vertx, ClickhouseNativeConnectOptions connectOptions) {
    return ClickhouseNativeConnectionImpl.connect((ContextInternal) vertx.getOrCreateContext(), connectOptions);
  }
}

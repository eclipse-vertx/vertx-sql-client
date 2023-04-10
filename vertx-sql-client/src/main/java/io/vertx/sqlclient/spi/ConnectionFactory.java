package io.vertx.sqlclient.spi;

import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A connection factory, can be obtained from {@link Driver#createConnectionFactory}
 */
public interface ConnectionFactory extends Closeable {

  static <T> Supplier<Future<T>> roundRobinSupplier(List<T> factories) {
    return new Supplier<Future<T>>() {
      AtomicLong idx = new AtomicLong();
      @Override
      public Future<T> get() {
        long val = idx.getAndIncrement();
        T f = factories.get((int)val % factories.size());
        return Future.succeededFuture(f);
      }
    };
  }

  /**
   * @return a connection factory that connects with a round-robin policy
   */
  static ConnectionFactory roundRobinSelector(List<ConnectionFactory> factories) {
    if (factories.size() == 1) {
      return factories.get(0);
    } else {
      return new ConnectionFactory() {
        int idx = 0;
        @Override
        public Future<SqlConnection> connect(Context context) {
          ConnectionFactory f = factories.get(idx);
          idx = (idx + 1) % factories.size();
          return f.connect(context);
        }
        @Override
        public Future<SqlConnection> connect(Context context, SqlConnectOptions options) {
          ConnectionFactory f = factories.get(idx);
          idx = (idx + 1) % factories.size();
          return f.connect(context, options);
        }
        @Override
        public void close(Promise<Void> promise) {
          List<Future> list = new ArrayList<>(factories.size());
          for (ConnectionFactory factory : factories) {
            Promise<Void> p = Promise.promise();
            factory.close(p);
            list.add(p.future());
          }
          CompositeFuture.all(list)
            .<Void>mapEmpty()
            .onComplete(promise);
        }
      };
    }
  }

  default Future<SqlConnection> connect(Context context, Future<? extends SqlConnectOptions> fut) {
    // The future might be on any context or context-less
    // So we need to use a specific context promise
    Promise<SqlConnectOptions> promise = ((ContextInternal) context).promise();
    fut.onComplete(ar -> {
      if (ar.succeeded()) {
        promise.complete(ar.result());
      } else {
        promise.fail(ar.cause());
      }
    });
    return promise
      .future()
      .compose(connectOptions -> connect(context, connectOptions));
  }

  /**
   * Create a connection using the given {@code context}.
   *
   * @param context the context
   * @return the future connection
   */
  Future<SqlConnection> connect(Context context);

  /**
   * Create a connection using the given {@code context}.
   *
   * @param context the context
   * @return the future connection
   */
  Future<SqlConnection> connect(Context context, SqlConnectOptions options);

}

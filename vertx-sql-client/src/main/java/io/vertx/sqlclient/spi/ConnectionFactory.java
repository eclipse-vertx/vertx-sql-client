package io.vertx.sqlclient.spi;

import io.vertx.core.Closeable;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A connection factory, can be obtained from {@link Driver#createConnectionFactory}
 */
public interface ConnectionFactory<C extends SqlConnectOptions> extends Closeable {

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
  static <C extends SqlConnectOptions> ConnectionFactory<C> roundRobinSelector(List<ConnectionFactory<C>> factories) {
    if (factories.size() == 1) {
      return factories.get(0);
    } else {
      return new ConnectionFactory<C>() {
        int idx = 0;
        @Override
        public Future<SqlConnection> connect(Context context, C options) {
          ConnectionFactory<C> f = factories.get(idx);
          idx = (idx + 1) % factories.size();
          return f.connect(context, options);
        }
        @Override
        public void close(Promise<Void> promise) {
          List<Future> list = new ArrayList<>(factories.size());
          for (ConnectionFactory<C> factory : factories) {
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

  /**
   * Create a connection using the given {@code context}.
   *
   * @param context the context
   * @return the future connection
   */
  Future<SqlConnection> connect(Context context, C options);

}

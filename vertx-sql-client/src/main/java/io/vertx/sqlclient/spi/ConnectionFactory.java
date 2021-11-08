package io.vertx.sqlclient.spi;

import io.vertx.core.*;
import io.vertx.sqlclient.ServerRequirement;
import io.vertx.sqlclient.ServerType;
import io.vertx.sqlclient.SqlConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A connection factory, can be obtained from {@link Driver#createConnectionFactory}
 */
public interface ConnectionFactory extends Closeable {

  default void close(Promise<Void> promise, List<ConnectionFactory> factories) {
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
        public void close(Promise<Void> promise) {
          close(promise, factories);
        }
      };
    }
  }

  /**
   * @return a connection factory that connects with respect to server requirement
   */
  static ConnectionFactory selector(List<ConnectionFactory> factories, ServerRequirement serverRequirement) {
    switch (serverRequirement) {
      case ANY:
        return roundRobinSelector(factories);
      case PRIMARY:
        return constrainedSelector(factories, ServerType.PRIMARY);
      case REPLICA:
        return constrainedSelector(factories, ServerType.REPLICA);
      case PREFER_REPLICA:
        return prioritySelector(factories, ServerType.REPLICA);
      default:
        throw new IllegalStateException("Unknown server requirement: " + serverRequirement);
    }
  }

  static ConnectionFactory constrainedSelector(List<ConnectionFactory> factories, ServerType serverType) {
    return new ConnectionFactory() {
      private int idx = 0;

      @Override
      public Future<SqlConnection> connect(Context context) {
        int oldIdx = idx;
        idx = (idx + 1) % factories.size();
        return connectingRound(context, oldIdx, factories.size());
      }

      private Future<SqlConnection> connectingRound(Context context, int roundIdx, int attemptsLeft) {
        if (attemptsLeft == 0) {
          throw new IllegalStateException(String.format("No suitable server of type %s was found", serverType));
        }
        ConnectionFactory f = factories.get(roundIdx);
        if (f.getServerType() == serverType) {
          return f.connect(context);
        } else if (f.getServerType() == ServerType.UNDEFINED) {
          return f.connect(context).flatMap(conn -> {
            if (f.getServerType() == serverType) {
              return Future.succeededFuture(conn);
            } else {
              return conn.close().flatMap(__ ->
                connectingRound(context, (roundIdx + 1) % factories.size(), attemptsLeft - 1)
              );
            }
          });
        } else {
          return connectingRound(context, (roundIdx + 1) % factories.size(), attemptsLeft - 1);
        }
      }

      @Override
      public void close(Promise<Void> promise) {
        close(promise, factories);
      }
    };
  }

  static ConnectionFactory prioritySelector(List<ConnectionFactory> factories, ServerType serverType) {
    return new ConnectionFactory() {
      private int idx = 0;
      private CopyOnWriteArrayList<ConnectionFactory> prioritized = new CopyOnWriteArrayList<>();
      private CopyOnWriteArrayList<ConnectionFactory> fallback = new CopyOnWriteArrayList<>();

      @Override
      public Future<SqlConnection> connect(Context context) {
        if (prioritized.size() + fallback.size() != factories.size()) {
          return connectAndPrioritize(context);
        } else {
          return connectByPriority(context);
        }
      }

      private Future<SqlConnection> connectAndPrioritize(Context context) {
        ConnectionFactory f = factories.get(idx);
        idx = (idx + 1) % factories.size();
        if (f.getServerType() == serverType) {
          prioritized.addIfAbsent(f);
          return f.connect(context).recover(__ -> connect(context));
        } else if (f.getServerType() == ServerType.UNDEFINED) {
          // this factory will be added to prioritized/fallback during some later invocation of this method
          return f.connect(context).recover(__ -> connect(context));
        } else {
          fallback.addIfAbsent(f);
          return f.connect(context).recover(__ -> connect(context));
        }
      }

      private Future<SqlConnection> connectByPriority(Context context) {
        ConnectionFactory pf = prioritized.get(idx % prioritized.size());
        ConnectionFactory ff = fallback.get(idx % fallback.size());
        idx = (idx + 1) % factories.size();
        return pf.connect(context).recover(__ -> ff.connect(context)).recover(__ -> connect(context));
      }

      @Override
      public void close(Promise<Void> promise) {
        close(promise, factories);
      }
    };
  }

  /**
   * Create a connection using the given {@code context}.
   *
   * @param context the context
   * @return the future connection
   */
  Future<SqlConnection> connect(Context context);


  /**
   * Server type could be updated asynchronously for instance, in Postgres case
   * server type can be reported as ParamStatus message in response to explicit 'SHOW xxx' command
   * as well as in response to Simple/Extended query in case of GUC_REPORT.
   * Given that, there could be situations when concurrent write to particular factory's serverType is happening
   * when this method is being called or just after this method has returned.
   * Users should be aware that serverType nature is dynamic.
   *
   * @return server type of host this factory connected to.
   */
  default ServerType getServerType() {
    return ServerType.UNDEFINED;
  }
}

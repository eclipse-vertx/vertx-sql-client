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

  /**
   * {@link ServerType} of a host to which particular factory opens connections is usually not known
   * upon factory construction. First opportunity to reveal such server property is available when
   * first {@link io.vertx.sqlclient.impl.SocketConnectionBase} is made. For instance Postgres 14 and newer
   * has dedicated `GUC_REPORT` mechanism that reports a property `in_hot_standby` when connection being established.
   * If there is no server-sent status mechanism available implementation specific `SHOW xxx` command could be used.
   *
   * If connection was established and its server type appeared to be mot compatible with requested one
   * then the connection is closed right away and next factory is probed. If all factories are probed and no
   * connection with desired server type found, IllegalStateException is thrown.
   *
   * @param factories list of factories to filter based on server type requirement
   * @param serverType requirement for the host being connected through a factory
   * @return a connection factory that load balances connections
   * to a subset of hosts which satisfying requested server type
   * @throws IllegalStateException when no suitable connection could be established
   */
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

  /**
   * {@link ServerType} of a host to which particular factory opens connections is usually not known
   * upon factory construction. First opportunity to reveal such server property is available when
   * first {@link io.vertx.sqlclient.impl.SocketConnectionBase} is made. For instance Postgres 14 and newer
   * has dedicated `GUC_REPORT` mechanism that reports a property `in_hot_standby` when connection being established.
   * If there is no server-sent status mechanism available implementation specific `SHOW xxx` command could be used.
   *
   * Upon first connection to host its server type is examinated and corresponding factory is marked either
   * prioritized one or fallback. Connection factory is returned no matter of underlying host type.
   * After all factories were probed, subsequent requests are optimistically fulfilled by the subset of prioritized
   * factories. If prioritized factory could not establish the connection, fallback is made to a "fallback" factory.
   * If a "fallback" factory fails as well, next pair of factories is chosen in round-robin manner.
   *
   * @param factories list of factories to filter based on server type requirement
   * @param serverType preferred property of the host being connected through a factory
   * @return a connection factory that load balances connections
   * to a subset of hosts with bias towards requested server type
   */
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
   * Server type could be updated asynchronously: for instance, in Postgres case
   * server type can be reported as ParamStatus message in response to explicit 'SHOW xxx' command
   * as well as in response to Simple/Extended query if dedicated GUC_REPORT is supported.
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

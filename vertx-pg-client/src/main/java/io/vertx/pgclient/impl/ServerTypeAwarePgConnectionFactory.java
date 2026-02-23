/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.pgclient.impl;

import io.vertx.core.Completable;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.TargetServerType;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.internal.QueryResultHandler;
import io.vertx.sqlclient.internal.RowDescriptorBase;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.spi.connection.ConnectionFactory;
import io.vertx.sqlclient.spi.protocol.SimpleQueryCommand;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;

/**
 * A {@link ConnectionFactory} wrapper that selects connections based on the PostgreSQL server type
 * (primary, secondary). It wraps a {@link PgConnectionFactory} delegate and implements
 * round-robin connection attempts with server type detection.
 */
public class ServerTypeAwarePgConnectionFactory implements ConnectionFactory<PgConnectOptions> {

  private final PgConnectionFactory delegate;
  private final List<PgConnectOptions> servers;
  private final TargetServerType targetServerType;
  private final ServerType[] cachedTypes;
  private final AtomicInteger roundRobinIdx;

  public ServerTypeAwarePgConnectionFactory(PgConnectionFactory delegate,
                                            List<PgConnectOptions> servers,
                                            TargetServerType targetServerType) {
    this.delegate = delegate;
    this.servers = servers;
    this.targetServerType = targetServerType;
    this.cachedTypes = new ServerType[servers.size()];
    Arrays.fill(cachedTypes, ServerType.UNDEFINED);
    this.roundRobinIdx = new AtomicInteger(0);
  }

  @Override
  public Future<Connection> connect(Context context, Future<PgConnectOptions> fut) {
    // Ignore the pool's options supplier; we manage our own server list
    return connectWithServerType(context);
  }

  @Override
  public Future<Connection> connect(Context context, PgConnectOptions options) {
    return connectWithServerType(context);
  }

  private Future<Connection> connectWithServerType(Context context) {
    int startIdx = roundRobinIdx.getAndUpdate(i -> (i + 1) % servers.size());
    switch (targetServerType) {
      case PRIMARY:
        return connectingRound(context, startIdx, 0, ServerType.PRIMARY);
      case SECONDARY:
        return connectingRound(context, startIdx, 0, ServerType.SECONDARY);
      case PREFER_PRIMARY:
        // First pass: try to find a primary; second pass: accept any server
        return connectingRound(context, startIdx, 0, ServerType.PRIMARY)
          .recover(err -> connectToAny(context, startIdx, 0));
      case PREFER_SECONDARY:
        // First pass: try to find a secondary; second pass: accept any server
        return connectingRound(context, startIdx, 0, ServerType.SECONDARY)
          .recover(err -> connectToAny(context, startIdx, 0));
      case ANY:
      default:
        return connectToAny(context, startIdx, 0);
    }
  }

  /**
   * Attempts to connect to any reachable server, starting at {@code startIdx} and trying
   * each server in round-robin order exactly once. Returns the first successful connection
   * regardless of server type. Fails if all servers are unreachable.
   */
  private Future<Connection> connectToAny(Context context, int startIdx, int attempt) {
    if (attempt >= servers.size()) {
      return Future.failedFuture("Could not connect to any server");
    }
    int idx = (startIdx + attempt) % servers.size();
    PgConnectOptions opts = servers.get(idx);
    return delegate.connect(context, opts)
      .recover(err -> connectToAny(context, startIdx, attempt + 1));
  }

  /**
   * Recursively attempts connections to servers in round-robin order, trying each server
   * in the list exactly once (at most {@code servers.size()} attempts total).
   *
   * <p>For each attempt, the method looks up the server at index
   * {@code (startIdx + attempt) % servers.size()} and first checks the type cache:
   * <ul>
   *   <li><b>Cached as different type than desired:</b> skip this server entirely,
   *       no TCP connection is made.</li>
   *   <li><b>Cached as desired type, or not cached:</b> connect and verify.</li>
   * </ul>
   *
   * <p>After connecting and detecting the actual server type, the method decides:
   * <ul>
   *   <li><b>Match:</b> return the connection immediately.</li>
   *   <li><b>Mismatch:</b> close this connection and continue to the next server.</li>
   *   <li><b>Connection failure:</b> skip this server and continue to the next.</li>
   * </ul>
   *
   * <p>When all servers have been tried without finding the desired type, fail with
   * "Could not find a server of type ...". For PREFER_* modes, the caller recovers from
   * this failure by falling back to {@link #connectToAny}.
   *
   * <p><b>Cache staleness after failover:</b> The cache may become stale when a primary is
   * demoted to replica or a replica is promoted to primary. This is safe because the cache
   * is only used to skip TCP connections, never to trust the server type without verification.
   * Specifically:
   * <ul>
   *   <li><b>Cached as desired type, actually changed:</b> always re-verified via
   *       {@link #detectServerType} after connecting; if the actual type changed, the cache
   *       is updated and the connection is treated as a mismatch.</li>
   *   <li><b>Cached as different type than desired, actually promoted:</b> skipped with no
   *       TCP connection. In the worst case this causes one extra round of connection attempts
   *       before the cache self-corrects: the first call after failover connects to the
   *       now-stale "desired" server, discovers the mismatch, updates the cache, and tries
   *       the next server which is the newly promoted one.</li>
   *   <li><b>All servers cached as different type than desired:</b> all servers are skipped
   *       by cache, the method retries from {@code startIdx} with cache disabled to force
   *       re-verification. This guarantees progress after a failover where all cached types
   *       become stale.</li>
   * </ul>
   *
   * @param context  the Vert.x context
   * @param startIdx the starting index in the server list (from round-robin counter)
   * @param attempt  current attempt number (0-based), incremented on each recursive call
   * @param desired  the desired server type (PRIMARY or SECONDARY)
   * @return a future resolved with a matching connection, or failed
   */
  private Future<Connection> connectingRound(Context context, int startIdx, int attempt,
                                             ServerType desired) {
    return connectingRound(context, startIdx, attempt, desired, true);
  }

  private Future<Connection> connectingRound(Context context, int startIdx, int attempt,
                                             ServerType desired, boolean useCache) {
    if (attempt >= servers.size()) {
      if (useCache) {
        // All servers may have been skipped by stale cache; retry ignoring cache
        return connectingRound(context, startIdx, 0, desired, false);
      }
      return Future.failedFuture("Could not find a server of type " + desired);
    }
    int idx = (startIdx + attempt) % servers.size();
    PgConnectOptions opts = servers.get(idx);
    if (useCache) {
      ServerType cachedType = cachedTypes[idx];
      if (cachedType != ServerType.UNDEFINED && cachedType != desired) {
        return connectingRound(context, startIdx, attempt + 1, desired, true);
      }
    }
    return delegate.connect(context, opts).compose(
      conn -> detectServerType(conn).compose(
        detectedType -> {
          cachedTypes[idx] = detectedType;
          if (detectedType == desired) {
            return Future.succeededFuture(conn);
          } else {
            return closeConnection(conn)
              .transform(v -> connectingRound(context, startIdx, attempt + 1, desired, useCache));
          }
        },
        err -> closeConnection(conn)
          .transform(v -> connectingRound(context, startIdx, attempt + 1, desired, useCache))),
      err -> connectingRound(context, startIdx, attempt + 1, desired, useCache)
    );
  }

  /**
   * Detects the server type of an established connection.
   * For PG 14+, the {@code in_hot_standby} parameter is reported during startup.
   * For older versions, we fall back to querying {@code SHOW transaction_read_only}.
   */
  private Future<ServerType> detectServerType(Connection conn) {
    PgSocketConnection pgConn = (PgSocketConnection) conn;
    if (pgConn.serverType != ServerType.UNDEFINED) {
      return Future.succeededFuture(pgConn.serverType);
    }
    // PG < 14: query transaction_read_only
    return queryTransactionReadOnly(pgConn);
  }

  /**
   * Fallback for PG < 14 where {@code in_hot_standby} is not reported as a GUC_REPORT parameter
   * during the startup handshake. PG 14+ sends it automatically, which is handled in
   * {@link io.vertx.pgclient.impl.codec.InitPgCommandMessage#handleParameterStatus} and stored on
   * {@link PgSocketConnection#serverType} before this method is ever reached. This method is only
   * called when {@code serverType} is still {@link ServerType#UNDEFINED} after connect.
   */
  private Future<ServerType> queryTransactionReadOnly(PgSocketConnection conn) {
    Promise<ServerType> promise = Promise.promise();
    Collector<Row, String[], String> collector = Collector.of(
      () -> new String[1],
      (acc, row) -> acc[0] = row.getString(0),
      (a, b) -> a,
      acc -> acc[0]
    );
    QueryResultHandler<String> resultHandler = new QueryResultHandler<>() {
      @Override
      public <V> void addProperty(PropertyKind<V> property, V v) {
      }
      @Override
      public void handleResult(int updatedCount, int size, RowDescriptorBase desc, String result, Throwable failure) {
        if (failure != null) {
          promise.tryFail(failure);
        } else {
          ServerType type = "on".equalsIgnoreCase(result) ? ServerType.SECONDARY : ServerType.PRIMARY;
          conn.serverType = type;
          promise.tryComplete(type);
        }
      }
    };
    SimpleQueryCommand<String> cmd = new SimpleQueryCommand<>(
      "SHOW transaction_read_only",
      true,
      false,
      collector,
      resultHandler
    );
    conn.schedule(cmd, (res, err) -> {
      if (err != null) {
        promise.tryFail(err);
      }
      // On success, handleResult was already called before this completable fires
    });
    return promise.future();
  }

  private Future<Void> closeConnection(Connection conn) {
    Promise<Void> promise = Promise.promise();
    conn.close(null, promise);
    return promise.future();
  }

  @Override
  public void close(Completable<Void> promise) {
    delegate.close(promise);
  }

  public PgConnectionFactory getDelegate() {
    return delegate;
  }
}

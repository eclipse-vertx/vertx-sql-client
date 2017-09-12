package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgConnection;
import com.julienviet.pgclient.PgConnectionPool;
import com.julienviet.pgclient.PgPreparedStatement;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Todo : handle timeout when borrowing a connection
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
class PostgresConnectionPoolImpl implements PgConnectionPool {

  private final PostgresClientImpl client;
  private final Context context;
  private final Bilto available;

  private interface Bilto {
    void acquire(Context current, Handler<AsyncResult<Proxy>> handler);
    void release(Proxy holder);
  }

  private class Exclusive implements Bilto {

    private final ArrayDeque<Waiter> waiters = new ArrayDeque<>();
    private final int maxSize;
    private final ArrayDeque<PgConnection> available = new ArrayDeque<>();
    private int connCount;

    public Exclusive(int maxSize) {
      this.maxSize = maxSize;
    }

    @Override
    public void acquire(Context current, Handler<AsyncResult<Proxy>> handler) {
      waiters.add(new Waiter(handler, current));
      check();
    }

    private void doAcq(Handler<AsyncResult<Proxy>> handler) {
      if (available.size() > 0) {
        PgConnection conn = available.poll();
        Proxy proxy = new Proxy(conn);
        conn.exceptionHandler(proxy::handleException);
        conn.closeHandler(v -> {
          connCount--;
          check();
          proxy.handleClosed();
        });
        handler.handle(Future.succeededFuture(proxy));
      } else {
        if (connCount < maxSize) {
          connCount++;
          client.connect(ar -> {
            if (ar.succeeded()) {
              available.add(ar.result());
              doAcq(handler);
            } else {
              handler.handle(Future.failedFuture(ar.cause()));
            }
          });
        }
      }
    }

    private void check() {
      if (waiters.size() > 0) {
        doAcq(ar -> {
          if (ar.succeeded()) {
            Waiter waiter = waiters.poll();
            waiter.use(ar.result());
          } else {
            Waiter waiter;
            while ((waiter = waiters.poll()) != null) {
              waiter.fail(ar.cause());
            }
          }
        });
      }
    }

    public void release(Proxy proxy) {
      proxy.conn.closeHandler(null);
      proxy.conn.exceptionHandler(null);
      available.add(proxy.conn);
      check();
    }
  }

  private class Multiplexed implements Bilto {

    final Set<Proxy> proxies = new HashSet<>();
    private PgConnection the;
    private boolean connecting;
    private ArrayDeque<Waiter> waiters = new ArrayDeque<>();

    @Override
    public void acquire(Context current, Handler<AsyncResult<Proxy>> handler) {
      if (the != null) {
        Proxy proxy = new Proxy(the);
        proxies.add(proxy);
        handler.handle(Future.succeededFuture(proxy));
      } else {
        waiters.add(new Waiter(handler, current));
        if (!connecting) {
          connecting = true;
          client.connect(ar -> {
            connecting = false;
            if (ar.succeeded()) {
              PgConnection conn = ar.result();
              conn.exceptionHandler(err -> {
                for (Proxy proxy : new ArrayList<>(proxies)) {
                  proxy.handleException(err);
                }
              });
              conn.closeHandler(v -> {
                conn.exceptionHandler(null);
                conn.closeHandler(null);
                ArrayList<Proxy> list = new ArrayList<>(proxies);
                proxies.clear();
                for (Proxy proxy : list) {
                  proxy.handleClosed();
                }
              });
              Waiter waiter;
              while ((waiter = waiters.poll()) != null) {
                Proxy proxy = new Proxy(conn);
                proxies.add(proxy);
                waiter.use(proxy);
              }
            } else {
              Waiter waiter;
              while ((waiter = waiters.poll()) != null) {
                waiter.fail(ar.cause());
              }
            }
          });
        }
      }
    }

    public void release(Proxy proxy) {
      proxies.remove(proxy);
    }
  }

  private static class Waiter {

    private final Handler<AsyncResult<Proxy>> handler;
    private final Context context;

    Waiter(Handler<AsyncResult<Proxy>> handler, Context context) {
      this.handler = handler;
      this.context = context;
    }

    void use(Proxy conn) {
      Context current = Vertx.currentContext();
      if (current == context) {
        handler.handle(Future.succeededFuture(conn));
      } else {
        context.runOnContext(v -> {
          use(conn);
        });
      }
    }

    void fail(Throwable err) {
      Context current = Vertx.currentContext();
      if (current == context) {
        handler.handle(Future.failedFuture(err));
      } else {
        context.runOnContext(v -> {
          fail(err);
        });
      }
    }
  }

  PostgresConnectionPoolImpl(PostgresClientImpl client, int maxSize, boolean multiplexed) {
    if (maxSize < 1) {
      throw new IllegalArgumentException("Pool max size must be > 0");
    }
    if (multiplexed && maxSize > 1) {
      throw new IllegalArgumentException();
    }
    this.context = client.vertx.getOrCreateContext();
    this.client = client;
    this.available = multiplexed ? new Multiplexed() : new Exclusive(maxSize);
  }

  @Override
  public void getConnection(Handler<AsyncResult<PgConnection>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      available.acquire(current, ar -> {
        if (ar.succeeded()) {
          handler.handle(Future.succeededFuture(ar.result()));
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
        }
      });
    } else {
      context.runOnContext(v -> getConnection(handler));
    }
  }

  private class Proxy implements PgConnection {

    final PgConnection conn;
    final AtomicBoolean closed = new AtomicBoolean();
    private volatile Handler<Throwable> exceptionHandler;
    private volatile Handler<Void> closeHandler;

    private Proxy(PgConnection conn) {
      this.conn = conn;
    }

    void handleException(Throwable err) {
      Handler<Throwable> handler = this.exceptionHandler;
      if (!closed.get() && handler != null) {
        handler.handle(err);
      }
    }

    void handleClosed() {
      Handler<Void> handler = this.closeHandler;
      if (closed.compareAndSet(false, true) && handler != null) {
        handler.handle(null);
      }
    }

    @Override
    public void execute(String sql, Handler<AsyncResult<ResultSet>> handler) {
      if (!closed.get()) {
        conn.execute(sql, handler);
      }
    }

    @Override
    public void update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
      if (!closed.get()) {
        conn.update(sql, handler);
      }
    }

    @Override
    public void query(String sql, Handler<AsyncResult<ResultSet>> handler) {
      if (!closed.get()) {
        conn.query(sql, handler);
      } else {
        handler.handle(Future.failedFuture("Connection closed"));
      }
    }

    @Override
    public void prepareAndQuery(String sql, Object param, Handler<AsyncResult<ResultSet>> handler) {
      if (!closed.get()) {
        conn.prepareAndQuery(sql, param, handler);
      }
    }

    @Override
    public void prepareAndQuery(String sql, Object param1, Object param2, Handler<AsyncResult<ResultSet>> handler) {
      if (!closed.get()) {
        conn.prepareAndQuery(sql, param1, param2, handler);
      }
    }

    @Override
    public void prepareAndQuery(String sql, Object param1, Object param2, Object param3,
                                Handler<AsyncResult<ResultSet>> handler) {
      if (!closed.get()) {
        conn.prepareAndQuery(sql, param1, param2, param3, handler);
      }
    }

    @Override
    public void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4,
                                Handler<AsyncResult<ResultSet>> handler) {
      if (!closed.get()) {
        conn.prepareAndQuery(sql, param1, param2, param3, param4, handler);
      }
    }

    @Override
    public void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4,
                                Object param5, Handler<AsyncResult<ResultSet>> handler) {
      if (!closed.get()) {
        conn.prepareAndQuery(sql, param1, param2, param3, param4, param5, handler);
      }
    }

    @Override
    public void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4,
                                Object param5, Object param6, Handler<AsyncResult<ResultSet>> handler) {
      if (!closed.get()) {
        conn.prepareAndQuery(sql, param1, param2, param3, param4, param5, param6, handler);
      }
    }

    @Override
    public void prepareAndQuery(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler) {
      if (!closed.get()) {
        conn.prepareAndQuery(sql, params, handler);
      }
    }

    @Override
    public void prepareAndExecute(String sql, Object param, Handler<AsyncResult<UpdateResult>> handler) {
      if (!closed.get()) {
        conn.prepareAndExecute(sql, param, handler);
      }
    }

    @Override
    public void prepareAndExecute(String sql, Object param1, Object param2, Handler<AsyncResult<UpdateResult>> handler) {
      if (!closed.get()) {
        conn.prepareAndExecute(sql, param1, param2, handler);
      }
    }

    @Override
    public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<UpdateResult>> handler) {
      if (!closed.get()) {
        conn.prepareAndExecute(sql, param1, param2, param3, handler);
      }
    }

    @Override
    public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Handler<AsyncResult<UpdateResult>> handler) {
      if (!closed.get()) {
        conn.prepareAndExecute(sql, param1, param2, param3, param4, handler);
      }
    }

    @Override
    public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Handler<AsyncResult<UpdateResult>> handler) {
      if (!closed.get()) {
        conn.prepareAndExecute(sql, param1, param2, param3, param4, param5, handler);
      }
    }

    @Override
    public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6, Handler<AsyncResult<UpdateResult>> handler) {
      if (!closed.get()) {
        conn.prepareAndExecute(sql, param1, param2, param3, param4, param5, param6, handler);
      }
    }

    @Override
    public void prepareAndExecute(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler) {
      if (!closed.get()) {
        conn.prepareAndExecute(sql, params, handler);
      }
    }

    @Override
    public void exceptionHandler(Handler<Throwable> handler) {
      if (!closed.get()) {
        exceptionHandler = handler;
      }
    }

    @Override
    public void closeHandler(Handler<Void> handler) {
      if (!closed.get()) {
        closeHandler = handler;
      }
    }

    @Override
    public PgPreparedStatement prepare(String sql) {
      throw new UnsupportedOperationException("Implement me");
    }

    @Override
    public void close() {
      if (closed.compareAndSet(false, true)) {
        available.release(this);
      }
    }
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException();
  }
}

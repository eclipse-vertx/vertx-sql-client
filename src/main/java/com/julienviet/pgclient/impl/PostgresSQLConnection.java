package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgBatch;
import com.julienviet.pgclient.PgPreparedStatement;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLRowStream;
import io.vertx.ext.sql.TransactionIsolation;
import io.vertx.ext.sql.UpdateResult;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class PostgresSQLConnection implements SQLConnection {

  private static final Logger log = LoggerFactory.getLogger(PostgresSQLConnection.class);

  private AtomicBoolean inTransaction = new AtomicBoolean(false);
  private AtomicBoolean inAutoCommit = new AtomicBoolean(true);

  private final DbConnection conn;

  public PostgresSQLConnection(DbConnection conn) {
    this.conn = conn;
  }

  @Override
  public SQLConnection setAutoCommit(boolean autoCommit, Handler<AsyncResult<Void>> handler) {
    inAutoCommit.set(autoCommit);
    if (inTransaction.compareAndSet(true, false) && autoCommit) {
      conn.schedule(new QueryCommand("COMMIT", new ResultSetBuilder(re -> {
        if(re.failed()) {
          handler.handle(Future.failedFuture(re.cause()));
        } else {
          handler.handle(Future.succeededFuture());
        }
      })));
    } else {
      handler.handle(Future.succeededFuture());
    }
    return this;
  }

  @Override
  public SQLConnection execute(String sql, Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection query(String sql, Handler<AsyncResult<ResultSet>> handler) {
    beginTxIfNeeded(tx -> {
      if(tx.failed()) {
        handler.handle(Future.failedFuture(tx.cause()));
      } else {
        conn.schedule(new QueryCommand(sql, new ResultSetBuilder(handler)));
      }
    });
    return this;
  }

  @Override
  public SQLConnection queryWithParams(String sql, JsonArray jsonArray, Handler<AsyncResult<ResultSet>> handler) {
    beginTxIfNeeded(tx -> {
      if(tx.failed()) {
        handler.handle(Future.failedFuture(tx.cause()));
      } else {
        conn.schedule(new PreparedQueryCommand(sql, jsonArray.getList(), new PreparedQueryResultHandler(ar -> {
          handler.handle(ar.map(results -> results));
        })));
      }
    });
    return this;
  }

  @Override
  public SQLConnection queryStream(String sql, Handler<AsyncResult<SQLRowStream>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection queryStreamWithParams(String sql, JsonArray jsonArray, Handler<AsyncResult<SQLRowStream>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    beginTxIfNeeded(tx -> {
      if(tx.failed()) {
        handler.handle(Future.failedFuture(tx.cause()));
      } else {
        conn.schedule(new UpdateCommand(sql, handler));
      }
    });
    return this;
  }

  @Override
  public SQLConnection updateWithParams(String sql, JsonArray jsonArray, Handler<AsyncResult<UpdateResult>> handler) {
    beginTxIfNeeded(tx -> {
      if(tx.failed()) {
        handler.handle(Future.failedFuture(tx.cause()));
      } else {
        conn.schedule(new PreparedUpdateCommand(sql, Collections.singletonList(jsonArray.getList()), ar -> {
          handler.handle(ar.map(results -> results.get(0)));
        }));
      }
    });
    return this;
  }

  @Override
  public SQLConnection call(String sql, Handler<AsyncResult<ResultSet>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection callWithParams(String sql, JsonArray jsonArray, JsonArray out, Handler<AsyncResult<ResultSet>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection commit(Handler<AsyncResult<Void>> handler) {
    endAndBeginTx("COMMIT", handler);
    return this;
  }

  @Override
  public SQLConnection rollback(Handler<AsyncResult<Void>> handler) {
    endAndBeginTx("ROLLBACK", handler);
    return this;
  }

  @Override
  public SQLConnection setQueryTimeout(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection batch(List<String> sqlStatements, Handler<AsyncResult<List<Integer>>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection batchWithParams(String sql, List<JsonArray> paramsList, Handler<AsyncResult<List<Integer>>> handler) {
    beginTxIfNeeded(tx -> {
      if(tx.failed()) {
        handler.handle(Future.failedFuture(tx.cause()));
      } else {
        PgPreparedStatement ps = new PgPreparedStatementImpl(conn, sql, java.util.UUID.randomUUID().toString());
        PgBatch batch = ps.batch();
        for (JsonArray params: paramsList) {
          batch.add(params.getList());
        }
        batch.execute(re -> {
          if (re.failed()) {
            handler.handle(Future.failedFuture(re.cause()));
          } else {
            handler.handle(Future.succeededFuture(re.result()
              .stream()
              .map(UpdateResult::getUpdated)
              .collect(Collectors.toList())));
          }
        });
      }
    });
    return this;
  }

  @Override
  public SQLConnection batchCallableWithParams(String s, List<JsonArray> list, List<JsonArray> list1, Handler<AsyncResult<List<Integer>>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection setTransactionIsolation(TransactionIsolation isolation, Handler<AsyncResult<Void>> handler) {
    if(isolation == TransactionIsolation.NONE) {
      handler.handle(Future.failedFuture("None transaction isolation is not supported"));
    } else {
      conn.schedule(new PreparedTxUpdateCommand(isolation, handler));
    }
    return this;
  }

  @Override
  public SQLConnection getTransactionIsolation(Handler<AsyncResult<TransactionIsolation>> handler) {
    conn.schedule(new PreparedTxQueryCommand(handler));
    return this;
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    inAutoCommit.set(true);
    if (inTransaction.compareAndSet(true, false)) {
      conn.schedule(new QueryCommand("COMMIT", new ResultSetBuilder(re -> {
        if(re.failed()) {
          handler.handle(Future.failedFuture(re.cause()));
        } else {
          // should we use close command for open portal if any ?
          conn.doClose();
          handler.handle(Future.succeededFuture());
        }
      })));
    } else {
      conn.doClose();
      handler.handle(Future.succeededFuture());
    }
  }

  @Override
  public void close() {
    close(c -> {
    });
  }

  private void beginTxIfNeeded(Handler<AsyncResult<Void>> handler) {
    if (!inAutoCommit.get() && inTransaction.compareAndSet(false, true)) {
      conn.schedule(new QueryCommand("BEGIN", new ResultSetBuilder(re -> {
        if(re.failed()) {
          handler.handle(Future.failedFuture(re.cause()));
        } else {
          handler.handle(Future.succeededFuture());
        }
      })));
    } else {
      handler.handle(Future.succeededFuture());
    }
  }

  private void endAndBeginTx(String command, Handler<AsyncResult<Void>> handler) {
    if (inTransaction.compareAndSet(true, false)) {
      conn.schedule(new QueryCommand(command, new ResultSetBuilder(re -> {
        if(re.failed()) {
          handler.handle(Future.failedFuture(re.cause()));
        } else {
          conn.schedule(new QueryCommand("BEGIN", new ResultSetBuilder(rs -> {
            if(rs.failed()) {
              handler.handle(Future.failedFuture(rs.cause()));
            } else {
              inTransaction.compareAndSet(false, true);
              handler.handle(Future.succeededFuture());
            }
          })));
        }
      })));
    } else {
      handler.handle(Future.failedFuture(new IllegalStateException("Not in transaction currently")));
    }
  }
}

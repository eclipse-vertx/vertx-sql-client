package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PostgresBatch;
import com.julienviet.pgclient.PreparedStatement;
import com.julienviet.pgclient.Result;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PreparedStatementImpl implements PreparedStatement {

  private final DbConnection conn;
  final String sql;
  final AtomicBoolean closed = new AtomicBoolean();
  boolean parsed;
  final String stmt;

  PreparedStatementImpl(DbConnection conn, String sql, String stmt) {
    this.conn = conn;
    this.sql = sql;
    this.stmt = stmt;
  }

  @Override
  public void execute(PostgresBatch batch, Handler<AsyncResult<List<Result>>> resultHandler) {
    BatchImpl batchImpl = (BatchImpl) batch;
    ArrayList<Result> results = new ArrayList<>();
    for (int i = 0; i < batchImpl.values.size(); i++) {
      conn.schedule(new BatchExecuteCommand(this, i + 1 == batchImpl.values.size(), batchImpl.values.get(i)) {
        @Override
        public void onSuccess(Result result) {
          results.add(result);
          if (results.size() == batchImpl.values.size()) {
            resultHandler.handle(Future.succeededFuture(results));
          }
        }

        @Override
        public void onError(String message) {
          throw new UnsupportedOperationException("Not yet implemented");
        }
      });
    }
  }

  @Override
  public void close() {
    close(ar -> {
    });
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    if (closed.compareAndSet(false, true)) {
      conn.schedule(new CloseStatementCommand(this, completionHandler));
    } else {
      completionHandler.handle(Future.failedFuture("Already closed"));
    }
  }
}

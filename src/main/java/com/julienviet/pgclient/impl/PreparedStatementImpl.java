package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.Batch;
import com.julienviet.pgclient.PreparedStatement;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PreparedStatementImpl implements PreparedStatement {

  final DbConnection conn;
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
  public Batch batch() {
    return new BatchImpl(this);
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

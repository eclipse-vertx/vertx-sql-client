package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.ImplReusable;
import io.reactiverse.pgclient.PgResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.function.Function;

@ImplReusable
public class MySQLResultBuilder<T, R extends MySQLResultBase<T, R>, L extends PgResult<T>> implements QueryResultHandler<T>, Handler<AsyncResult<Boolean>> {

  private final Handler<AsyncResult<L>> handler;
  private final Function<T, R> factory;
  private R first;
  private boolean suspended;

  MySQLResultBuilder(Function<T, R> factory, Handler<AsyncResult<L>> handler) {
    this.factory = factory;
    this.handler = handler;
  }

  @Override
  public void handleResult(int updatedCount, int size, ColumnMetadata columnMetadata, T result) {
    R r = factory.apply(result);
    r.updated = updatedCount;
    r.size = size;
    r.columnNames = columnMetadata != null ? columnMetadata.getColumnNames() : null;
    handleResult(r);
  }

  private void handleResult(R result) {
    if (first == null) {
      first = result;
    } else {
      R h = first;
      while (h.next != null) {
        h = h.next;
      }
      h.next = result;
    }
  }

  @Override
  public void handle(AsyncResult<Boolean> res) {
    suspended = res.succeeded() && res.result();
    handler.handle((AsyncResult<L>) res.map(first));
  }

  public boolean isSuspended() {
    return suspended;
  }
}

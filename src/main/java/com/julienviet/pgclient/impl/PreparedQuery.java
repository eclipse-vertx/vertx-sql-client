package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgResultSet;
import com.julienviet.pgclient.PgQuery;
import com.julienviet.pgclient.PgRowStream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PreparedQuery implements PgQuery, PgRowStream {

  private static final int READY = 0, IN_PROGRESS = 1, SUSPENDED = 2;

  final PgPreparedStatementImpl ps;
  final List<Object> params;
  private int fetch;
  private int status;
  private String portal;
  private Handler<JsonArray> rowHandler;
  private Handler<Void> endHandler;
  private Handler<Throwable> exceptionHandler;
  private Handler<Void> resultSetClosedHandler;


  PreparedQuery(PgPreparedStatementImpl ps, List<Object> params) {
    this.ps = ps;
    this.params = params;
  }

  @Override
  public PreparedQuery fetch(int size) {
    this.fetch = size;
    return this;
  }

  @Override
  public void execute(Handler<AsyncResult<PgResultSet>> handler) {
    execute(new PreparedQueryResultHandler(handler));
  }

  private void execute(QueryResultHandler handler) {
    if (status == IN_PROGRESS) {
      throw new IllegalStateException();
    }
    QueryResultHandler adapter = new QueryResultHandler() {
      @Override
      public void beginResult(List<String> columnNames) {
        handler.beginResult(columnNames);
      }
      @Override
      public void handleRow(JsonArray row) {
        handler.handleRow(row);
      }
      @Override
      public void endResult(boolean suspended) {
        status = suspended ? SUSPENDED : READY;
        handler.endResult(suspended);
      }
      @Override
      public void fail(Throwable cause) {
        status = READY;
        handler.fail(cause);
      }
      @Override
      public void end() {
        handler.end();
      }
    };
    if (status == READY) {
      status = IN_PROGRESS;
      portal = fetch > 0 ? UUID.randomUUID().toString() : "";
      ps.execute(params, fetch, portal, false, adapter);
    } else {
      status = IN_PROGRESS;
      ps.execute(params, fetch, portal, true, adapter);
    }
  }

  @Override
  public int column(String s) {
    return 0;
  }

  @Override
  public List<String> columns() {
    return null;
  }

  @Override
  public PgRowStream resultSetClosedHandler(Handler<Void> handler) {
    resultSetClosedHandler = handler;
    return this;
  }

  @Override
  public void moreResults() {
    Handler<JsonArray> _rowHandler = rowHandler;
    Handler<Void> _endHandler = endHandler;
    Handler<Throwable> _exceptionHandler = exceptionHandler;
    Handler<Void> _resultSetClosedHandler = resultSetClosedHandler;
    PreparedQuery.this.execute(new QueryResultHandler() {
      @Override
      public void beginResult(List<String> columnNames) {
      }
      @Override
      public void handleRow(JsonArray row) {
        if (_rowHandler != null) {
          _rowHandler.handle(row);
        }
      }
      @Override
      public void endResult(boolean suspended) {
        if (suspended) {
          if (_resultSetClosedHandler != null) {
            _resultSetClosedHandler.handle(null);
          }
        } else {
          if (_endHandler != null) {
            _endHandler.handle(null);
          }
        }
      }
      @Override
      public void fail(Throwable cause) {
        if (_exceptionHandler != null) {
          _exceptionHandler.handle(cause);
        }
      }
      @Override
      public void end() {
      }
    });
  }

  @Override
  public void close() {
    close(ar -> {});
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    if (status == IN_PROGRESS) {
      ps.closePortal(portal, completionHandler);
    } else {
      completionHandler.handle(Future.succeededFuture());
    }
  }

  @Override
  public PgRowStream exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @Override
  public PgRowStream handler(Handler<JsonArray> handler) {
    rowHandler = handler;
    return this;
  }

  @Override
  public PgRowStream pause() {
    return this;
  }

  @Override
  public PgRowStream resume() {
    return this;
  }

  @Override
  public PgRowStream endHandler(Handler<Void> handler) {
    endHandler = handler;
    return this;
  }

  @Override
  public void execute() {
    moreResults();
  }
}

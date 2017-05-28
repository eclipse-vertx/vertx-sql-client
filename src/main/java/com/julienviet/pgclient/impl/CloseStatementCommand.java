package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CloseComplete;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.encoder.message.Close;
import com.julienviet.pgclient.codec.encoder.message.Sync;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class CloseStatementCommand extends CommandBase {

  final String stmt;
  final Handler<AsyncResult<Void>> handler;

  public CloseStatementCommand(String stmt, Handler<AsyncResult<Void>> handler) {
    this.stmt = stmt;
    this.handler = handler;
  }

  @Override
  boolean exec(DbConnection conn) {
    conn.writeToChannel(new Close().setStatement(stmt));
    conn.writeToChannel(Sync.INSTANCE);
    return true;
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.getClass() == CloseComplete.class) {
      handler.handle(Future.succeededFuture());
      return false;
    } else if (msg.getClass() == ReadyForQuery.class) {
      return true;
    } else {
      return super.handleMessage(msg);
    }
  }

  @Override
  void fail(Throwable err) {
    handler.handle(Future.failedFuture(err));
  }
}

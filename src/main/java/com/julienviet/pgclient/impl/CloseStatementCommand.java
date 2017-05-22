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

  final PreparedStatementImpl ps;
  final Handler<AsyncResult<Void>> handler;

  public CloseStatementCommand(PreparedStatementImpl ps, Handler<AsyncResult<Void>> handler) {
    this.ps = ps;
    this.handler = handler;
  }

  @Override
  boolean exec(DbConnection conn) {
    if (ps.parsed) {
      conn.writeToChannel(new Close().setStatement(ps.stmt));
      conn.writeToChannel(Sync.INSTANCE);
    } else {
      handler.handle(Future.succeededFuture());
    }
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

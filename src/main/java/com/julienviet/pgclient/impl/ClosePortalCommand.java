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
class ClosePortalCommand extends CommandBase {

  final String portal;
  final Handler<AsyncResult<Void>> handler;
  private Handler<Void> doneHandler;

  public ClosePortalCommand(String portal, Handler<AsyncResult<Void>> handler) {
    this.portal = portal;
    this.handler = handler;
  }

  @Override
  void exec(DbConnection conn, Handler<Void> handler) {
    doneHandler = handler;
    conn.writeMessage(new Close().setPortal(portal));
    conn.writeMessage(Sync.INSTANCE);
  }

  @Override
  public void handleMessage(Message msg) {
    if (msg.getClass() == CloseComplete.class) {
      handler.handle(Future.succeededFuture());
    } else if (msg.getClass() == ReadyForQuery.class) {
      doneHandler.handle(null);
    } else {
      super.handleMessage(msg);
    }
  }

  @Override
  void fail(Throwable err) {
    handler.handle(Future.failedFuture(err));
  }
}

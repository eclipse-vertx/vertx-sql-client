package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.NoData;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.encoder.message.Bind;
import com.julienviet.pgclient.codec.encoder.message.Describe;
import com.julienviet.pgclient.codec.encoder.message.Execute;
import com.julienviet.pgclient.codec.encoder.message.Parse;
import com.julienviet.pgclient.codec.encoder.message.Sync;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.TransactionIsolation;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class PreparedTxQueryCommand extends TxQueryCommandBase {

  private final Handler<AsyncResult<TransactionIsolation>> handler;

  public PreparedTxQueryCommand(Handler<AsyncResult<TransactionIsolation>> handler) {
    this.handler = handler;
  }

  @Override
  void exec(DbConnection conn, Handler<Void> handler) {
    doneHandler = handler;
    conn.writeMessage(new Parse("SHOW TRANSACTION ISOLATION LEVEL"));
    conn.writeMessage(new Bind());
    conn.writeMessage(new Describe());
    conn.writeMessage(new Execute().setRowCount(0));
    conn.writeMessage(Sync.INSTANCE);
  }

  @Override
  public void handleMessage(Message msg) {
    if (msg.getClass() == ReadyForQuery.class) {
      doneHandler.handle(null);
    } else if (msg.getClass() == ParameterDescription.class) {
    } else if (msg.getClass() == NoData.class) {
    } else if (msg.getClass() == ParseComplete.class) {
    } else if (msg.getClass() == BindComplete.class) {
    } else {
      super.handleMessage(msg);
    }
  }

  @Override
  void handleResult(TransactionIsolation isolation) {
    handler.handle(Future.succeededFuture(isolation));
  }

  @Override
  void fail(Throwable cause) {
    handler.handle(Future.failedFuture(cause));
  }
}

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.NoData;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.encoder.message.Bind;
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

class PreparedTxUpdateCommand extends TxUpdateCommandBase {

  final Handler<AsyncResult<Void>> handler;
  private TransactionIsolation isolation;

  PreparedTxUpdateCommand(TransactionIsolation isolation, Handler<AsyncResult<Void>> handler) {
    this.isolation = isolation;
    this.handler = handler;
  }

  @Override
  boolean exec(DbConnection conn) {
    switch (isolation) {
      case READ_COMMITTED:{
        conn.writeToChannel(new Parse("SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL READ COMMITTED"));
      }
      break;
      case REPEATABLE_READ:{
        conn.writeToChannel(new Parse("SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL REPEATABLE READ"));
      }
      break;
      case READ_UNCOMMITTED: {
        conn.writeToChannel(new Parse("SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL READ UNCOMMITTED"));
      }
      break;
      case SERIALIZABLE:{
        conn.writeToChannel(new Parse("SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL SERIALIZABLE"));
      }
      break;
    }
    conn.writeToChannel(new Bind());
    conn.writeToChannel(new Execute().setRowCount(1));
    conn.writeToChannel(Sync.INSTANCE);
    return true;
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.getClass() == ReadyForQuery.class) {
      return true;
    } else if (msg.getClass() == ParameterDescription.class) {
      return false;
    } else if (msg.getClass() == NoData.class) {
      return false;
    } else if (msg.getClass() == ParseComplete.class) {
      return false;
    } else if (msg.getClass() == BindComplete.class) {
      return false;
    } else {
      return super.handleMessage(msg);
    }
  }

  @Override
  void handleResult(Void result) {
    handler.handle(Future.succeededFuture(result));
  }

  @Override
  void fail(Throwable cause) {
    handler.handle(Future.failedFuture(cause));
  }
}

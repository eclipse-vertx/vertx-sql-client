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

import java.util.EnumMap;

import static io.vertx.ext.sql.TransactionIsolation.*;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

class PreparedTxUpdateCommand extends TxUpdateCommandBase {

  final Handler<AsyncResult<Void>> handler;
  final TransactionIsolation isolation;
  static final EnumMap<TransactionIsolation, String> txMap = new EnumMap<>(TransactionIsolation.class);

  PreparedTxUpdateCommand(TransactionIsolation isolation, Handler<AsyncResult<Void>> handler) {
    this.isolation = isolation;
    this.handler = handler;
    loadTxEnumMap();
  }

  @Override
  void exec(DbConnection conn) {
    conn.writeToChannel(new Parse(txMap.get(isolation)));
    conn.writeToChannel(new Bind());
    conn.writeToChannel(new Execute().setRowCount(1));
    conn.writeToChannel(Sync.INSTANCE);
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

  void loadTxEnumMap() {
    txMap.put(READ_COMMITTED, "SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL READ COMMITTED");
    txMap.put(REPEATABLE_READ, "SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL REPEATABLE READ");
    txMap.put(READ_UNCOMMITTED, "SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
    txMap.put(SERIALIZABLE, "SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL SERIALIZABLE");
  }
}

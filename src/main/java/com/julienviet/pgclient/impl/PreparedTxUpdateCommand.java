/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.NoData;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;
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
  final EnumMap<TransactionIsolation, String> txMap = new EnumMap<>(TransactionIsolation.class);

  PreparedTxUpdateCommand(TransactionIsolation isolation, Handler<AsyncResult<Void>> handler) {
    this.isolation = isolation;
    this.handler = handler;
    loadTxEnumMap();
  }

  @Override
  void exec(NetConnection conn) {
    conn.writeMessage(new Parse(txMap.get(isolation)));
    conn.writeMessage(new Bind());
    conn.writeMessage(new Execute().setRowCount(1));
    conn.writeMessage(Sync.INSTANCE);
  }

  @Override
  public void handleMessage(Message msg) {
    if (msg.getClass() == ParameterDescription.class) {
    } else if (msg.getClass() == NoData.class) {
    } else if (msg.getClass() == ParseComplete.class) {
    } else if (msg.getClass() == BindComplete.class) {
    } else {
      super.handleMessage(msg);
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

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

import com.julienviet.pgclient.PgException;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.DataRow;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import io.vertx.core.Handler;
import io.vertx.ext.sql.TransactionIsolation;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

abstract class TxQueryCommandBase extends CommandBase {

  private String value;
  protected Handler<Void> doneHandler;

  @Override
  public void handleMessage(Message msg) {
    if (msg.getClass() == RowDescription.class) {
    } else if (msg.getClass() == DataRow.class) {
      DataRow dataRow = (DataRow) msg;
      value = new String(dataRow.getValue(0), UTF_8);
    } else if (msg.getClass() == CommandComplete.class) {
      switch (value) {
        case "read committed": {
          handleResult(TransactionIsolation.READ_COMMITTED);
        }
        break;
        case "read uncommitted": {
          handleResult(TransactionIsolation.READ_UNCOMMITTED);
        }
        break;
        case "repeatable read": {
          handleResult(TransactionIsolation.REPEATABLE_READ);
        }
        break;
        case "serializable": {
          handleResult(TransactionIsolation.SERIALIZABLE);
        }
        break;
      }
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      doneHandler.handle(null);
      fail(new PgException(error));
    } else {
      super.handleMessage(msg);
    }
  }

  abstract void handleResult(TransactionIsolation isolation);

  abstract void fail(Throwable cause);

}

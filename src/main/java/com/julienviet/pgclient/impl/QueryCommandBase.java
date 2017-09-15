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
import com.julienviet.pgclient.codec.Column;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.DataRow;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.List;

import static com.julienviet.pgclient.codec.util.Util.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

abstract class QueryCommandBase extends CommandBase {

  protected final QueryResultHandler handler;
  private RowDescription rowDesc;
  protected Handler<Void> doneHandler;

  public QueryCommandBase(QueryResultHandler handler) {
    this.handler = handler;
  }

  @Override
  public void handleMessage(Message msg) {
    if (msg.getClass() == ReadyForQuery.class) {
      doneHandler.handle(null);
      handler.end();
    } else if (msg.getClass() == RowDescription.class) {
      rowDesc = (RowDescription) msg;
      Column[] columns = rowDesc.getColumns();
      List<String> columnNames = new ArrayList<>(columns.length);
      for (Column columnDesc : columns) {
        columnNames.add(columnDesc.getName());
      }
      handler.beginResult(columnNames);
    } else if (msg.getClass() == DataRow.class) {
      DataRow dataRow = (DataRow) msg;
      JsonArray row = new JsonArray();
      Column[] columns = rowDesc.getColumns();
      handleRow(columns, dataRow, row);
      handler.handleRow(row);
    } else if (msg.getClass() == CommandComplete.class) {
      rowDesc = null;
      handler.endResult(false);
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      doneHandler.handle(null);
      fail(new PgException(error));
    } else {
      super.handleMessage(msg);
    }
  }

  @Override
  void fail(Throwable cause) {
    handler.fail(cause);
  }
}

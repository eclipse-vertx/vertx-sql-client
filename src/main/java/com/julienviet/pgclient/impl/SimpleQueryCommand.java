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

import com.julienviet.pgclient.ResultSet;
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.decoder.InboundMessage;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import com.julienviet.pgclient.codec.encoder.message.Query;

import java.util.ArrayList;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

class SimpleQueryCommand extends QueryCommandBase {

  private final String sql;

  SimpleQueryCommand(String sql, QueryResultHandler handler) {
    super(handler);
    this.sql = sql;
  }

  @Override
  void exec(SocketConnection conn) {
    conn.writeMessage(new Query(sql));
  }

  @Override
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == RowDescription.class) {
      rowDesc = (RowDescription) msg;
      dataFormat = DataFormat.TEXT;
      resultSet = new ResultSet().setResults(new ArrayList<>()).setColumnNames(rowDesc.getColumnNames());
    } else {
      super.handleMessage(msg);
    }
  }

  public String getSql() {
    return sql;
  }
}

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

import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.decoder.DecodeContext;
import com.julienviet.pgclient.codec.decoder.InboundMessage;
import com.julienviet.pgclient.codec.decoder.ResultDecoder;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import com.julienviet.pgclient.codec.encoder.message.Query;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

class SimpleQueryCommand<T> extends QueryCommandBase<T> {

  private ResultDecoder<T> decoder;
  private final String sql;

  SimpleQueryCommand(String sql, ResultDecoder<T> decoder, QueryResultHandler<T> handler) {
    super(handler);
    this.sql = sql;
    this.decoder = decoder;
  }

  @Override
  void exec(SocketConnection conn) {
    conn.decodeQueue.add(new DecodeContext(true, null, DataFormat.TEXT, decoder));
    conn.writeMessage(new Query(sql));
  }

  @Override
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == RowDescription.class) {
      // Expected
    } else {
      super.handleMessage(msg);
    }
  }

  public String getSql() {
    return sql;
  }
}

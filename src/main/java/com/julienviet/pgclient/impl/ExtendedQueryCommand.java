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

import com.julienviet.pgclient.codec.decoder.InboundMessage;
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.NoData;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;
import com.julienviet.pgclient.codec.decoder.message.PortalSuspended;
import com.julienviet.pgclient.codec.encoder.message.Bind;
import com.julienviet.pgclient.codec.encoder.message.Describe;
import com.julienviet.pgclient.codec.encoder.message.Execute;
import com.julienviet.pgclient.codec.encoder.message.Parse;
import com.julienviet.pgclient.codec.encoder.message.Sync;

import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class ExtendedQueryCommand extends QueryCommandBase {


  final boolean parse;
  final String sql;
  final List<Object> params;
  final int fetch;
  final String stmt;
  private final String portal;
  private final boolean suspended;

  ExtendedQueryCommand(String sql,
                       List<Object> params,
                       QueryResultHandler handler) {
    this(true, sql, params, 0, "", "", false, handler);
  }

  ExtendedQueryCommand(boolean parse,
                       String sql,
                       List<Object> params,
                       int fetch,
                       String stmt,
                       String portal,
                       boolean suspended,
                       QueryResultHandler handler) {
    super(handler);
    this.parse = parse;
    this.sql = sql;
    this.params = params;
    this.fetch = fetch;
    this.stmt = stmt;
    this.portal = portal;
    this.suspended = suspended;
  }

  @Override
  void exec(SocketConnection conn) {
    boolean p;
    String s;
    if (conn.psCache != null) {
      String cached = conn.psCache.get(sql);
      if (cached == null) {
        p = true;
        s = UUID.randomUUID().toString();
        conn.psCache.put(sql, s);
      } else {
        p = false;
        s = cached;
      }
    } else {
      p = parse;
      s = stmt;
    }

    if (p) {
      conn.writeMessage(new Parse(sql).setStatement(s));
    }
    if (!suspended) {
      conn.writeMessage(new Bind().setParamValues(params).setPortal(portal).setStatement(s));
      conn.writeMessage(new Describe().setStatement(s));
    } else {
      // Needed for now, later see how to remove it
      conn.writeMessage(new Describe().setPortal(portal));
    }
    conn.writeMessage(new Execute().setPortal(portal).setRowCount(fetch));
    conn.writeMessage(Sync.INSTANCE);
  }

  @Override
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == PortalSuspended.class) {
      handler.endResult(true);
    } else if (msg.getClass() == ParameterDescription.class) {
    } else if (msg.getClass() == NoData.class) {
    } else if (msg.getClass() == ParseComplete.class) {
    } else if (msg.getClass() == BindComplete.class) {
    } else {
      super.handleMessage(msg);
    }
  }
}

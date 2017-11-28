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
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.NoData;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;
import com.julienviet.pgclient.codec.decoder.message.PortalSuspended;
import com.julienviet.pgclient.codec.encoder.message.Bind;
import com.julienviet.pgclient.codec.encoder.message.Execute;
import com.julienviet.pgclient.codec.encoder.message.Parse;
import com.julienviet.pgclient.codec.encoder.message.Sync;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class ExtendedQueryCommand extends QueryCommandBase {

  final PreparedStatement ps;
  final List<Object> params;
  final int fetch;
  private final String portal;
  private final boolean suspended;

  ExtendedQueryCommand(PreparedStatement ps,
                       List<Object> params,
                       QueryResultHandler handler) {
    this(ps, params, 0, "", false, handler);
  }

  ExtendedQueryCommand(PreparedStatement ps,
                       List<Object> params,
                       int fetch,
                       String portal,
                       boolean suspended,
                       QueryResultHandler handler) {
    super(handler);
    this.ps = ps;
    this.params = params;
    this.fetch = fetch;
    this.portal = portal;
    this.suspended = suspended;

    rowDesc = ps.rowDesc;
    resultSet = new ResultSet().setResults(new ArrayList<>()).setColumnNames(rowDesc.getColumnNames());
    dataFormat = DataFormat.BINARY;
  }

  @Override
  void exec(SocketConnection conn) {
    if (suspended) {
      conn.writeMessage(new Execute().setPortal(portal).setRowCount(fetch));
      conn.writeMessage(Sync.INSTANCE);
    } else if (ps.stmt.length() > 0) {
      conn.writeMessage(new Bind().setParamValues(params).setPortal(portal).setStatement(ps.stmt));
      conn.writeMessage(new Execute().setPortal(portal).setRowCount(fetch));
      conn.writeMessage(Sync.INSTANCE);
    } else {
      conn.writeMessage(new Parse(ps.sql).setStatement(""));
      conn.writeMessage(new Bind().setParamValues(params).setPortal(portal).setStatement(ps.stmt));
      conn.writeMessage(new Execute().setPortal(portal).setRowCount(fetch));
      conn.writeMessage(Sync.INSTANCE);
    }
  }

  @Override
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == PortalSuspended.class) {
      handler.result(resultSet, true);
    } else if (msg.getClass() == ParameterDescription.class) {
    } else if (msg.getClass() == NoData.class) {
    } else if (msg.getClass() == ParseComplete.class) {
    } else if (msg.getClass() == BindComplete.class) {
    } else {
      super.handleMessage(msg);
    }
  }
}

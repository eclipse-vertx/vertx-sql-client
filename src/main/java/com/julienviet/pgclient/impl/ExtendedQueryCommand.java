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

import com.julienviet.pgclient.PgResult;
import com.julienviet.pgclient.Tuple;
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.decoder.DecodeContext;
import com.julienviet.pgclient.codec.decoder.InboundMessage;
import com.julienviet.pgclient.codec.decoder.ResultDecoder;
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;
import com.julienviet.pgclient.codec.decoder.message.PortalSuspended;
import com.julienviet.pgclient.codec.encoder.message.Bind;
import com.julienviet.pgclient.codec.encoder.message.Execute;
import com.julienviet.pgclient.codec.encoder.message.Parse;
import com.julienviet.pgclient.codec.encoder.message.Sync;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class ExtendedQueryCommand<T> extends QueryCommandBase<T> {

  private final PreparedStatement ps;
  private final Iterator<Tuple> paramsIterator;
  private final int fetch;
  private final String portal;
  private final boolean suspended;
  private final ResultDecoder<T> decoder;

  ExtendedQueryCommand(PreparedStatement ps,
                       Tuple params,
                       ResultDecoder<T> decoder,
                       QueryResultHandler<T> handler) {
    this(ps, params, 0, null, false, decoder, handler);
  }

  ExtendedQueryCommand(PreparedStatement ps,
                       Iterator<Tuple> paramsIterator,
                       ResultDecoder<T> decoder,
                       QueryResultHandler<T> handler) {
    this(ps, paramsIterator, 0, null, false, decoder, handler);
  }

  ExtendedQueryCommand(PreparedStatement ps,
                       Tuple params,
                       int fetch,
                       String portal,
                       boolean suspended,
                       ResultDecoder<T> decoder,
                       QueryResultHandler<T> handler) {
    this(ps, Collections.singletonList(params).iterator(), fetch, portal, suspended, decoder, handler);
  }

  ExtendedQueryCommand(PreparedStatement ps,
                       Iterator<Tuple> paramsIterator,
                       int fetch,
                       String portal,
                       boolean suspended,
                       ResultDecoder<T> decoder,
                       QueryResultHandler<T> handler) {
    super(handler);
    this.ps = ps;
    this.paramsIterator = paramsIterator;
    this.fetch = fetch;
    this.portal = portal;
    this.suspended = suspended;
    this.decoder = decoder;
  }

  @Override
  void exec(SocketConnection conn) {
    conn.decodeQueue.add(new DecodeContext(false, ps.rowDesc, DataFormat.BINARY, decoder));
    if (suspended) {
      conn.writeMessage(new Execute().setPortal(portal).setRowCount(fetch));
      conn.writeMessage(Sync.INSTANCE);
    } else {
      if (ps.statement == null) {
        conn.writeMessage(new Parse(ps.sql).setStatement(""));
      }
      while (paramsIterator.hasNext()) {
        List<Object> params = (List<Object>) paramsIterator.next();
        conn.writeMessage(new Bind().setParamValues(params).setDataTypes(ps.paramDesc.getParamDataTypes()).setPortal(portal).setStatement(ps.statement));
        conn.writeMessage(new Execute().setPortal(portal).setRowCount(fetch));
      }
      conn.writeMessage(Sync.INSTANCE);
    }
  }

  @Override
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == ParseComplete.class) {
      // Response to Parse
    } else if (msg.getClass() == PortalSuspended.class) {
      PgResult<T> result = (PgResult<T>) ((PortalSuspended) msg).result();
      handler.handleResult(result);
      handler.handleSuspend();
    } else if (msg.getClass() == BindComplete.class) {
      // Response to Bind
    } else {
      super.handleMessage(msg);
    }
  }
}

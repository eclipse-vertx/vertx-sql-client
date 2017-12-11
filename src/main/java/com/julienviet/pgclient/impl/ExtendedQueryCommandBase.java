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
import com.julienviet.pgclient.impl.codec.DataFormat;
import com.julienviet.pgclient.impl.codec.decoder.InboundMessage;
import com.julienviet.pgclient.impl.codec.decoder.ResultDecoder;
import com.julienviet.pgclient.impl.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.impl.codec.decoder.message.ParseComplete;
import com.julienviet.pgclient.impl.codec.decoder.message.PortalSuspended;
import com.julienviet.pgclient.impl.codec.decoder.message.ReadyForQuery;
import io.vertx.core.Future;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
abstract class ExtendedQueryCommandBase<T> extends QueryCommandBase<T> {

  protected final PreparedStatement ps;
  protected final int fetch;
  protected final String portal;
  protected final boolean suspended;
  protected final ResultDecoder<T> decoder;
  private boolean susp;

  ExtendedQueryCommandBase(PreparedStatement ps,
                           int fetch,
                           String portal,
                           boolean suspended,
                           ResultDecoder<T> decoder,
                           QueryResultHandler<T> handler) {
    super(handler);
    this.ps = ps;
    this.fetch = fetch;
    this.portal = portal;
    this.suspended = suspended;
    this.decoder = decoder;
  }

  @Override
  String sql() {
    return ps.sql;
  }

  @Override
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == ParseComplete.class) {
      // Response to Parse
    } else if (msg.getClass() == PortalSuspended.class) {
      susp = true;
      PgResult<T> result = (PgResult<T>) ((PortalSuspended) msg).result();
      resultHandler.handleResult(result);
    } else if (msg.getClass() == BindComplete.class) {
      // Response to Bind
    } else if (msg.getClass() == ReadyForQuery.class) {
      super.handleMessage(msg);
      if (!completed) {
        completed = true;
        handler.handle(Future.succeededFuture(susp));
      }
    } else {
      super.handleMessage(msg);
    }
  }
}

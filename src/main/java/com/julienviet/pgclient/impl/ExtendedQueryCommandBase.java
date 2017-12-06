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
abstract class ExtendedQueryCommandBase<T> extends QueryCommandBase<T> {

  protected final PreparedStatement ps;
  protected final int fetch;
  protected final String portal;
  protected final boolean suspended;
  protected final ResultDecoder<T> decoder;

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
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == ParseComplete.class) {
      // Response to Parse
    } else if (msg.getClass() == PortalSuspended.class) {
      PgResult<T> result = (PgResult<T>) ((PortalSuspended) msg).result();
      handler.handleResult(result, true);
    } else if (msg.getClass() == BindComplete.class) {
      // Response to Bind
    } else {
      super.handleMessage(msg);
    }
  }
}

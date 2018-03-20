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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.impl.codec.decoder.InboundMessage;
import io.reactiverse.pgclient.impl.codec.decoder.ResultDecoder;
import io.reactiverse.pgclient.impl.codec.decoder.message.BindComplete;
import io.reactiverse.pgclient.impl.codec.decoder.message.ParseComplete;
import io.reactiverse.pgclient.impl.codec.decoder.message.PortalSuspended;

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
  String sql() {
    return ps.sql;
  }

  @Override
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == ParseComplete.class) {
      // Response to Parse
    } else if (msg.getClass() == PortalSuspended.class) {
      this.result = true;
      PgResult<T> result = (PgResult<T>) ((PortalSuspended) msg).result();
      resultHandler.handleResult(result);
    } else if (msg.getClass() == BindComplete.class) {
      // Response to Bind
    } else {
      super.handleMessage(msg);
    }
  }
}

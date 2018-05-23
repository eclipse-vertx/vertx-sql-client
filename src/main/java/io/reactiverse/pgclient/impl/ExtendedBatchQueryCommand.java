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

import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.impl.codec.decoder.ResultDecoder;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.reactiverse.pgclient.impl.codec.encoder.message.Bind;
import io.reactiverse.pgclient.impl.codec.encoder.message.Execute;
import io.reactiverse.pgclient.impl.codec.encoder.message.Parse;
import io.reactiverse.pgclient.impl.codec.encoder.message.Sync;

import java.util.Iterator;
import java.util.List;

public class ExtendedBatchQueryCommand<T> extends ExtendedQueryCommandBase<T> {

  private final Iterator<Tuple> paramsIterator;

  ExtendedBatchQueryCommand(PreparedStatement ps,
                            Iterator<Tuple> paramsIterator,
                            ResultDecoder<T> decoder,
                            QueryResultHandler<T> handler) {
    this(ps, paramsIterator, 0, null, false, decoder, handler);
  }

  ExtendedBatchQueryCommand(PreparedStatement ps,
                            Iterator<Tuple> paramsIterator,
                            int fetch,
                            String portal,
                            boolean suspended,
                            ResultDecoder<T> decoder,
                            QueryResultHandler<T> handler) {
    super(ps, fetch, portal, suspended, decoder, handler);
    this.paramsIterator = paramsIterator;
  }

  @Override
  void exec(MessageEncoder out) {
    if (suspended) {
      out.writeMessage(new Execute().setPortal(portal).setRowCount(fetch));
      out.writeMessage(Sync.INSTANCE);
    } else {
      if (ps.statement!= 0) {
        out.writeMessage(new Parse(ps.sql));
      }
      while (paramsIterator.hasNext()) {
        List<Object> params = (List<Object>) paramsIterator.next();
        out.writeMessage(new Bind(ps.statement, portal, params, ps.paramDesc.getParamDataTypes(), ps.columnDescs()));
        out.writeMessage(new Execute().setPortal(portal).setRowCount(fetch));
      }
      out.writeMessage(Sync.INSTANCE);
    }
  }
}

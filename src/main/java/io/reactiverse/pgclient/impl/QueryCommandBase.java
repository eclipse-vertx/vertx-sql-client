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

import io.reactiverse.pgclient.PgException;
import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.impl.codec.decoder.InboundMessage;
import io.reactiverse.pgclient.impl.codec.decoder.message.CommandComplete;
import io.reactiverse.pgclient.impl.codec.decoder.message.ErrorResponse;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

abstract class QueryCommandBase<T> extends CommandBase<Boolean> {

  protected final QueryResultHandler<T> resultHandler;

  public QueryCommandBase(QueryResultHandler<T> handler) {
    super(handler);
    this.resultHandler = handler;
  }

  abstract String sql();

  @Override
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == CommandComplete.class) {
      this.result = false;
      PgResult<T> result = (PgResult<T>) ((CommandComplete) msg).result();
      resultHandler.handleResult(result);
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      failure = new PgException(error);
    } else {
      super.handleMessage(msg);
    }
  }

  @Override
  void fail(Throwable cause) {
    handler.handle(CommandResponse.failure(cause));
  }
}
